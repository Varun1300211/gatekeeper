package com.gatekeeper.messaging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("!redis & !kafka")
public class NoopConfigChangeMessagePublisher implements ConfigChangeMessagePublisher {

    @Override
    public void publish(GatekeeperConfigChangedEvent event) {
        log.debug(
                "Skipping config-change stream publish because redis profile is not active: action={} flagKey={}",
                event.action(),
                event.flagKey());
    }
}
