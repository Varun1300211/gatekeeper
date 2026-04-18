package com.gatekeeper.messaging;

import java.time.Instant;

public record GatekeeperConfigChangedEvent(
        String flagKey,
        String previousFlagKey,
        String entityType,
        Long entityId,
        String action,
        Instant occurredAt) {

    public static GatekeeperConfigChangedEvent of(
            String flagKey,
            String entityType,
            Long entityId,
            String action) {
        return of(flagKey, null, entityType, entityId, action);
    }

    public static GatekeeperConfigChangedEvent of(
            String flagKey,
            String previousFlagKey,
            String entityType,
            Long entityId,
            String action) {
        return new GatekeeperConfigChangedEvent(
                flagKey,
                previousFlagKey,
                entityType,
                entityId,
                action,
                Instant.now());
    }
}
