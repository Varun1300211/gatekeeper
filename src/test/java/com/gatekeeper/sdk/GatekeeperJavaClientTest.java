package com.gatekeeper.sdk;

import static org.assertj.core.api.Assertions.assertThat;

import com.gatekeeper.dto.GatekeeperEvaluationResponse;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayDeque;
import java.util.Deque;
import org.junit.jupiter.api.Test;

class GatekeeperJavaClientTest {

    @Test
    void returnsLocalCacheHitBeforeTtlExpires() {
        MutableClock clock = new MutableClock(Instant.parse("2026-03-23T12:00:00Z"));
        FakeRemoteEvaluationFetcher fetcher = new FakeRemoteEvaluationFetcher();
        fetcher.enqueue(true);
        GatekeeperJavaClient client = new GatekeeperJavaClient(fetcher, clock);

        var firstResult = client.isEnabled(
                "http://localhost:8080",
                "checkout",
                "alice",
                "prod",
                Duration.ofSeconds(30));

        clock.advance(Duration.ofSeconds(10));

        var secondResult = client.isEnabled(
                "http://localhost:8080",
                "checkout",
                "alice",
                "prod",
                Duration.ofSeconds(30));

        assertThat(firstResult.getSource()).isEqualTo(SdkEvaluationSource.REMOTE_FETCH);
        assertThat(secondResult.getSource()).isEqualTo(SdkEvaluationSource.LOCAL_CACHE);
        assertThat(secondResult.isEnabled()).isTrue();
        assertThat(fetcher.getCallCount()).isEqualTo(1);
    }

    @Test
    void refreshesFromRemoteAfterTtlExpires() {
        MutableClock clock = new MutableClock(Instant.parse("2026-03-23T12:00:00Z"));
        FakeRemoteEvaluationFetcher fetcher = new FakeRemoteEvaluationFetcher();
        fetcher.enqueue(true);
        fetcher.enqueue(false);
        GatekeeperJavaClient client = new GatekeeperJavaClient(fetcher, clock);

        var firstResult = client.isEnabled(
                "http://localhost:8080",
                "checkout",
                "alice",
                "prod",
                Duration.ofSeconds(30));

        clock.advance(Duration.ofSeconds(31));

        var secondResult = client.isEnabled(
                "http://localhost:8080",
                "checkout",
                "alice",
                "prod",
                Duration.ofSeconds(30));

        assertThat(firstResult.getSource()).isEqualTo(SdkEvaluationSource.REMOTE_FETCH);
        assertThat(secondResult.getSource()).isEqualTo(SdkEvaluationSource.REMOTE_FETCH);
        assertThat(secondResult.isEnabled()).isFalse();
        assertThat(client.getLocalCacheEntries()).hasSize(1);
        assertThat(client.getLocalCacheEntries().get(0).getExpiresAt())
                .isEqualTo(LocalDateTime.ofInstant(clock.instant(), ZoneOffset.UTC).plusSeconds(30));
        assertThat(fetcher.getCallCount()).isEqualTo(2);
    }

    private static final class FakeRemoteEvaluationFetcher extends GatekeeperRemoteEvaluationFetcher {
        private final Deque<Boolean> enabledResponses = new ArrayDeque<>();
        private int callCount;

        private FakeRemoteEvaluationFetcher() {
            super(null);
        }

        private void enqueue(boolean enabled) {
            enabledResponses.add(enabled);
        }

        private int getCallCount() {
            return callCount;
        }

        @Override
        public GatekeeperEvaluationResponse fetchEvaluation(String baseUrl, String flagKey, String userId, String environment) {
            callCount++;
            boolean enabled = enabledResponses.isEmpty() ? false : enabledResponses.removeFirst();
            return GatekeeperEvaluationResponse.builder()
                    .flagKey(flagKey)
                    .userId(userId)
                    .environment(environment)
                    .enabled(enabled)
                    .build();
        }
    }

    private static final class MutableClock extends Clock {
        private Instant instant;

        private MutableClock(Instant instant) {
            this.instant = instant;
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return instant;
        }

        private void advance(Duration duration) {
            instant = instant.plus(duration);
        }
    }
}
