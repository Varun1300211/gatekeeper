package com.gatekeeper.messaging;

import com.gatekeeper.sdk.GatekeeperJavaClient;
import jakarta.annotation.PreDestroy;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.connection.stream.StreamReadOptions;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("redis & !kafka")
@RequiredArgsConstructor
public class RedisConfigChangeStreamConsumer implements CommandLineRunner {

    private final StringRedisTemplate redisTemplate;
    private final GatekeeperJavaClient gatekeeperJavaClient;
    private final ConfigChangeMessagingProperties properties;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private volatile String lastSeenRecordId = "0-0";

    @Override
    public void run(String... args) {
        if (!properties.isEnabled()) {
            return;
        }

        log.info(
                "Starting Redis Stream config-change consumer: stream={} pollInterval={}ms maxReadCount={}",
                properties.getStreamKey(),
                properties.getPollIntervalMs(),
                properties.getMaxReadCount());

        scheduler.scheduleWithFixedDelay(
                this::pollEventsSafely,
                properties.getPollIntervalMs(),
                properties.getPollIntervalMs(),
                TimeUnit.MILLISECONDS);
    }

    @SuppressWarnings("unchecked")
    private void pollEventsSafely() {
        try {
            StreamOperations<String, String, String> streamOperations = redisTemplate.opsForStream();
            List<MapRecord<String, String, String>> records = streamOperations.read(
                    StreamReadOptions.empty().count(properties.getMaxReadCount()),
                    StreamOffset.create(properties.getStreamKey(), ReadOffset.from(lastSeenRecordId)));

            if (records == null || records.isEmpty()) {
                return;
            }

            for (MapRecord<String, String, String> record : records) {
                lastSeenRecordId = record.getId().getValue();
                handleRecord(record);
            }
        } catch (Exception exception) {
            log.warn("Failed to consume config-change events from Redis Stream: {}", exception.getMessage());
        }
    }

    private void handleRecord(MapRecord<String, String, String> record) {
        String action = field(record, "action");
        Set<String> flagKeys = new LinkedHashSet<>();
        addIfPresent(flagKeys, field(record, "flagKey"));
        addIfPresent(flagKeys, field(record, "previousFlagKey"));

        if (flagKeys.isEmpty()) {
            gatekeeperJavaClient.clearLocalCache();
            log.info(
                    "Config-change event cleared entire SDK local cache: recordId={} action={}",
                    record.getId(),
                    action);
            return;
        }

        int removedEntries = flagKeys.stream()
                .mapToInt(gatekeeperJavaClient::evictLocalCacheForFlag)
                .sum();
        log.info(
                "Config-change event invalidated SDK local cache: recordId={} action={} flagKeys={} removedEntries={}",
                record.getId(),
                action,
                flagKeys,
                removedEntries);
    }

    private String field(MapRecord<String, String, String> record, String name) {
        String value = record.getValue().get(name);
        return value == null ? "" : value;
    }

    private void addIfPresent(Set<String> values, String value) {
        if (value != null && !value.isBlank()) {
            values.add(value);
        }
    }

    @PreDestroy
    void shutdown() {
        scheduler.shutdownNow();
    }
}
