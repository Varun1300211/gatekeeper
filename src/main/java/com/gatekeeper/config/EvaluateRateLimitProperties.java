package com.gatekeeper.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "gatekeeper.rate-limit.evaluate")
public class EvaluateRateLimitProperties {

    private boolean enabled = true;
    private long requestsPerMinute = 60;
}
