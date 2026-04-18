package com.gatekeeper.messaging;

import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("redis & !kafka")
@RequiredArgsConstructor
public class RedisConfigChangeMessagePublisher implements ConfigChangeMessagePublisher {

    private final StringRedisTemplate redisTemplate;
    private final ConfigChangeMessagingProperties properties;

    @Override
    public void publish(GatekeeperConfigChangedEvent event) {
        if (!properties.isEnabled()) {
            return;
        }

        Map<String, String> payload = new LinkedHashMap<>();
        payload.put("flagKey", nullToEmpty(event.flagKey()));
        payload.put("previousFlagKey", nullToEmpty(event.previousFlagKey()));
        payload.put("entityType", nullToEmpty(event.entityType()));
        payload.put("entityId", event.entityId() == null ? "" : event.entityId().toString());
        payload.put("action", nullToEmpty(event.action()));
        payload.put("occurredAt", event.occurredAt().toString());

        try {
            RecordId recordId = redisTemplate.opsForStream().add(properties.getStreamKey(), payload);
            log.info(
                    "Published config-change event to Redis Stream: stream={} recordId={} action={} flagKey={}",
                    properties.getStreamKey(),
                    recordId,
                    event.action(),
                    event.flagKey());
        } catch (Exception exception) {
            log.warn(
                    "Failed to publish config-change event to Redis Stream: action={} flagKey={} error={}",
                    event.action(),
                    event.flagKey(),
                    exception.getMessage());
        }
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
