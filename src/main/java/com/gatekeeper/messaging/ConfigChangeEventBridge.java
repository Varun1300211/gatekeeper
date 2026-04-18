package com.gatekeeper.messaging;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ConfigChangeEventBridge {

    private final ConfigChangeMessagePublisher configChangeMessagePublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishAfterCommit(GatekeeperConfigChangedEvent event) {
        configChangeMessagePublisher.publish(event);
    }
}
