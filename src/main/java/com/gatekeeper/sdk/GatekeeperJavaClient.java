package com.gatekeeper.sdk;

import com.gatekeeper.dto.GatekeeperEvaluationResponse;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Builder;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GatekeeperJavaClient {

    private final Map<String, CachedEvaluation> localCache = new ConcurrentHashMap<>();
    private final GatekeeperRemoteEvaluationFetcher remoteEvaluationFetcher;
    private final Clock clock;

    @Autowired
    public GatekeeperJavaClient(GatekeeperRemoteEvaluationFetcher remoteEvaluationFetcher) {
        this(remoteEvaluationFetcher, Clock.systemUTC());
    }

    GatekeeperJavaClient(GatekeeperRemoteEvaluationFetcher remoteEvaluationFetcher, Clock clock) {
        this.remoteEvaluationFetcher = remoteEvaluationFetcher;
        this.clock = clock;
    }

    public SdkEvaluationResult isEnabled(
            String baseUrl,
            String flagKey,
            String userId,
            String environment,
            Duration cacheTtl) {
        String cacheKey = cacheKey(baseUrl, flagKey, userId, environment);
        CachedEvaluation cachedEvaluation = localCache.get(cacheKey);
        LocalDateTime now = LocalDateTime.now(clock);

        if (cachedEvaluation != null && !cachedEvaluation.isExpiredAt(now)) {
            return SdkEvaluationResult.builder()
                    .baseUrl(baseUrl)
                    .flagKey(flagKey)
                    .userId(userId)
                    .environment(environment)
                    .enabled(cachedEvaluation.isEnabled())
                    .source(SdkEvaluationSource.LOCAL_CACHE)
                    .lastFetchedAt(cachedEvaluation.getLastFetchedAt())
                    .expiresAt(cachedEvaluation.getExpiresAt())
                    .build();
        }

        return refreshEvaluation(baseUrl, flagKey, userId, environment, cacheTtl);
    }

    public SdkEvaluationResult refreshEvaluation(
            String baseUrl,
            String flagKey,
            String userId,
            String environment,
            Duration cacheTtl) {
        GatekeeperEvaluationResponse response = remoteEvaluationFetcher.fetchEvaluation(baseUrl, flagKey, userId, environment);
        LocalDateTime fetchedAt = LocalDateTime.now(clock);
        CachedEvaluation cachedEvaluation = CachedEvaluation.builder()
                .baseUrl(baseUrl)
                .flagKey(flagKey)
                .userId(userId)
                .environment(environment)
                .enabled(response != null && response.isEnabled())
                .lastFetchedAt(fetchedAt)
                .expiresAt(fetchedAt.plus(cacheTtl))
                .build();

        localCache.put(cacheKey(baseUrl, flagKey, userId, environment), cachedEvaluation);
        return toResult(cachedEvaluation, SdkEvaluationSource.REMOTE_FETCH);
    }

    public List<CachedEvaluation> getLocalCacheEntries() {
        return localCache.values().stream()
                .sorted(Comparator
                        .comparing(CachedEvaluation::getBaseUrl)
                        .thenComparing(CachedEvaluation::getFlagKey)
                        .thenComparing(CachedEvaluation::getUserId)
                        .thenComparing(CachedEvaluation::getEnvironment))
                .toList();
    }

    public void clearLocalCache() {
        localCache.clear();
    }

    private SdkEvaluationResult toResult(CachedEvaluation cachedEvaluation, SdkEvaluationSource source) {
        return SdkEvaluationResult.builder()
                .baseUrl(cachedEvaluation.getBaseUrl())
                .flagKey(cachedEvaluation.getFlagKey())
                .userId(cachedEvaluation.getUserId())
                .environment(cachedEvaluation.getEnvironment())
                .enabled(cachedEvaluation.isEnabled())
                .source(source)
                .lastFetchedAt(cachedEvaluation.getLastFetchedAt())
                .expiresAt(cachedEvaluation.getExpiresAt())
                .build();
    }

    private String cacheKey(String baseUrl, String flagKey, String userId, String environment) {
        return baseUrl + "|" + flagKey + ":" + userId + ":" + environment;
    }

    @Getter
    @Builder
    public static class CachedEvaluation {
        private final String baseUrl;
        private final String flagKey;
        private final String userId;
        private final String environment;
        private final boolean enabled;
        private final LocalDateTime lastFetchedAt;
        private final LocalDateTime expiresAt;

        boolean isExpiredAt(LocalDateTime timestamp) {
            return expiresAt == null || !expiresAt.isAfter(timestamp);
        }
    }

    @Getter
    @Builder
    public static class SdkEvaluationResult {
        private final String baseUrl;
        private final String flagKey;
        private final String userId;
        private final String environment;
        private final boolean enabled;
        private final SdkEvaluationSource source;
        private final LocalDateTime lastFetchedAt;
        private final LocalDateTime expiresAt;
    }
}
