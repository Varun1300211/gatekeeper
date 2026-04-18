package com.gatekeeper.messaging;

public interface ConfigChangeMessagePublisher {

    void publish(GatekeeperConfigChangedEvent event);
}
