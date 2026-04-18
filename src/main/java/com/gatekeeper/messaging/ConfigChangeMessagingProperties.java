package com.gatekeeper.messaging;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "gatekeeper.messaging.config-events")
public class ConfigChangeMessagingProperties {

    private boolean enabled = true;
    private String streamKey = "gatekeeper:config-events";
    private String topic = "gatekeeper.config-events";
    private String consumerGroupId = "gatekeeper-sdk-simulator";
    private long pollIntervalMs = 2000;
    private int maxReadCount = 20;
}
