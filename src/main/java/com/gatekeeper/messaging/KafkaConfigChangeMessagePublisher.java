package com.gatekeeper.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("kafka")
@RequiredArgsConstructor
public class KafkaConfigChangeMessagePublisher implements ConfigChangeMessagePublisher {

    private final KafkaTemplate<String, GatekeeperConfigChangedEvent> kafkaTemplate;
    private final ConfigChangeMessagingProperties properties;

    @Override
    public void publish(GatekeeperConfigChangedEvent event) {
        if (!properties.isEnabled()) {
            return;
        }

        kafkaTemplate.send(properties.getTopic(), event.flagKey(), event)
                .whenComplete((result, exception) -> {
                    if (exception != null) {
                        log.warn(
                                "Failed to publish config-change event to Kafka: topic={} action={} flagKey={} error={}",
                                properties.getTopic(),
                                event.action(),
                                event.flagKey(),
                                exception.getMessage());
                        return;
                    }

                    log.info(
                            "Published config-change event to Kafka: topic={} partition={} offset={} action={} flagKey={}",
                            result.getRecordMetadata().topic(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset(),
                            event.action(),
                            event.flagKey());
                });
    }
}
