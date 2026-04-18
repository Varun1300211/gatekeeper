package com.gatekeeper.messaging;

import com.gatekeeper.sdk.GatekeeperJavaClient;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("kafka")
@RequiredArgsConstructor
public class KafkaConfigChangeEventConsumer {

    private final GatekeeperJavaClient gatekeeperJavaClient;

    @KafkaListener(
            topics = "${gatekeeper.messaging.config-events.topic:gatekeeper.config-events}",
            groupId = "${gatekeeper.messaging.config-events.consumer-group-id:gatekeeper-sdk-simulator}")
    public void handleConfigChange(
            GatekeeperConfigChangedEvent event,
            @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String key) {
        if (event == null) {
            return;
        }

        Set<String> flagKeys = new LinkedHashSet<>();
        addIfPresent(flagKeys, event.flagKey());
        addIfPresent(flagKeys, event.previousFlagKey());

        if (flagKeys.isEmpty()) {
            gatekeeperJavaClient.clearLocalCache();
            log.info(
                    "Kafka config-change event cleared entire SDK local cache: key={} action={}",
                    key,
                    event.action());
            return;
        }

        int removedEntries = flagKeys.stream()
                .mapToInt(gatekeeperJavaClient::evictLocalCacheForFlag)
                .sum();
        log.info(
                "Kafka config-change event invalidated SDK local cache: key={} action={} flagKeys={} removedEntries={}",
                key,
                event.action(),
                flagKeys,
                removedEntries);
    }

    private void addIfPresent(Set<String> values, String value) {
        if (value != null && !value.isBlank()) {
            values.add(value);
        }
    }
}
