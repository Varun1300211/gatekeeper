package com.gatekeeper.sdk;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "gatekeeper.sdk")
public class GatekeeperSdkProperties {

    private String baseUrl = "http://localhost:8080";
    private long pollIntervalSeconds = 15;
    private long localCacheTtlSeconds = 30;
    private String flagKey;
    private String userId;
    private String environment;
    private String username = "viewer";
    private String password = "viewer123";
    private List<Target> targets = List.of();

    public List<ResolvedTarget> getResolvedTargets() {
        if (targets != null && !targets.isEmpty()) {
            return targets.stream()
                    .map(target -> ResolvedTarget.builder()
                            .flagKey(target.getFlagKey())
                            .userId(target.getUserId())
                            .environment(target.getEnvironment())
                            .build())
                    .toList();
        }

        if (isBlank(flagKey) || isBlank(userId) || isBlank(environment)) {
            return List.of();
        }

        return List.of(ResolvedTarget.builder()
                .flagKey(flagKey)
                .userId(userId)
                .environment(environment)
                .build());
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    @Getter
    @Setter
    public static class Target {
        private String flagKey;
        private String userId;
        private String environment;
    }

    @Getter
    @Builder
    public static class ResolvedTarget {
        private final String flagKey;
        private final String userId;
        private final String environment;
    }
}
