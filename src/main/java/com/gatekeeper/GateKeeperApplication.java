package com.gatekeeper;

import com.gatekeeper.config.EvaluateRateLimitProperties;
import com.gatekeeper.sdk.GatekeeperSdkProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({
        GatekeeperSdkProperties.class,
        EvaluateRateLimitProperties.class
})
public class GateKeeperApplication {

    public static void main(String[] args) {
        SpringApplication.run(GateKeeperApplication.class, args);
    }
}
