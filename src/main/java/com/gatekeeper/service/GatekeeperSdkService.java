package com.gatekeeper.service;

import com.gatekeeper.dto.GatekeeperFlagResponse;
import com.gatekeeper.dto.SdkCacheEntryResponse;
import com.gatekeeper.dto.SdkEvaluationResponse;
import com.gatekeeper.dto.SdkStatusResponse;
import com.gatekeeper.dto.SdkTargetForm;
import com.gatekeeper.dto.SdkTargetResponse;
import com.gatekeeper.sdk.GatekeeperJavaClient;
import com.gatekeeper.sdk.GatekeeperRemoteFlagFetcher;
import com.gatekeeper.sdk.GatekeeperSdkProperties;
import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GatekeeperSdkService {

    private final GatekeeperJavaClient gatekeeperJavaClient;
    private final GatekeeperRemoteFlagFetcher gatekeeperRemoteFlagFetcher;
    private final GatekeeperSdkProperties gatekeeperSdkProperties;

    private final Map<String, RuntimeSdkTarget> runtimeTargets = new ConcurrentHashMap<>();

    @PostConstruct
    void initializeConfiguredTargets() {
        gatekeeperSdkProperties.getResolvedTargets().forEach(target -> addConfiguredTargetInternal(
                target.getFlagKey(),
                target.getUserId(),
                target.getEnvironment()));
    }

    public SdkStatusResponse getStatus() {
        return SdkStatusResponse.builder()
                .baseUrl(gatekeeperSdkProperties.getBaseUrl())
                .pollIntervalSeconds(gatekeeperSdkProperties.getPollIntervalSeconds())
                .localCacheTtlSeconds(gatekeeperSdkProperties.getLocalCacheTtlSeconds())
                .configuredTargets(getConfiguredTargets())
                .localCacheEntries(getLocalCacheEntries())
                .build();
    }

    public SdkEvaluationResponse evaluateWithSdk(String baseUrl, String flagKey, String userId, String environment) {
        return toResponse(gatekeeperJavaClient.isEnabled(
                resolveBaseUrl(baseUrl),
                flagKey,
                userId,
                environment,
                Duration.ofSeconds(gatekeeperSdkProperties.getLocalCacheTtlSeconds())));
    }

    public List<SdkEvaluationResponse> refreshConfiguredTargets() {
        Duration cacheTtl = Duration.ofSeconds(gatekeeperSdkProperties.getLocalCacheTtlSeconds());
        return runtimeTargets.values().stream()
                .sorted(Comparator.comparing(RuntimeSdkTarget::getFlagKey)
                        .thenComparing(RuntimeSdkTarget::getUserId)
                        .thenComparing(RuntimeSdkTarget::getEnvironment))
                .map(target -> gatekeeperJavaClient.refreshEvaluation(
                        gatekeeperSdkProperties.getBaseUrl(),
                        target.getFlagKey(),
                        target.getUserId(),
                        target.getEnvironment(),
                        cacheTtl))
                .map(this::toResponse)
                .toList();
    }

    public void clearLocalCache() {
        gatekeeperJavaClient.clearLocalCache();
    }

    public List<SdkTargetResponse> getConfiguredTargets() {
        return runtimeTargets.values().stream()
                .sorted(Comparator.comparing(RuntimeSdkTarget::getFlagKey)
                        .thenComparing(RuntimeSdkTarget::getUserId)
                        .thenComparing(RuntimeSdkTarget::getEnvironment))
                .map(target -> SdkTargetResponse.builder()
                        .id(target.getId())
                        .flagKey(target.getFlagKey())
                        .userId(target.getUserId())
                        .environment(target.getEnvironment())
                        .build())
                .toList();
    }

    public List<String> getAvailableFlagKeys() {
        try {
            return gatekeeperRemoteFlagFetcher.fetchFlags(gatekeeperSdkProperties.getBaseUrl()).stream()
                    .map(GatekeeperFlagResponse::getKey)
                    .sorted(String::compareToIgnoreCase)
                    .toList();
        } catch (Exception exception) {
            return List.of();
        }
    }

    public SdkTargetResponse addConfiguredTarget(SdkTargetForm form) {
        RuntimeSdkTarget target = addConfiguredTargetInternal(
                form.getFlagKey(),
                form.getUserId(),
                form.getEnvironment());
        return SdkTargetResponse.builder()
                .id(target.getId())
                .flagKey(target.getFlagKey())
                .userId(target.getUserId())
                .environment(target.getEnvironment())
                .build();
    }

    public void removeConfiguredTarget(String id) {
        runtimeTargets.remove(id);
    }

    public List<SdkCacheEntryResponse> getLocalCacheEntries() {
        LocalDateTime now = LocalDateTime.now();
        return gatekeeperJavaClient.getLocalCacheEntries().stream()
                .map(entry -> SdkCacheEntryResponse.builder()
                        .baseUrl(entry.getBaseUrl())
                        .flagKey(entry.getFlagKey())
                        .userId(entry.getUserId())
                        .environment(entry.getEnvironment())
                        .enabled(entry.isEnabled())
                        .lastFetchedAt(entry.getLastFetchedAt())
                        .expiresAt(entry.getExpiresAt())
                        .expired(entry.getExpiresAt() == null || !entry.getExpiresAt().isAfter(now))
                        .build())
                .toList();
    }

    public String defaultBaseUrl() {
        return gatekeeperSdkProperties.getBaseUrl();
    }

    private RuntimeSdkTarget addConfiguredTargetInternal(String flagKey, String userId, String environment) {
        validateTarget(flagKey, userId, environment);

        RuntimeSdkTarget existing = runtimeTargets.values().stream()
                .filter(target -> target.getFlagKey().equals(flagKey)
                        && target.getUserId().equals(userId)
                        && target.getEnvironment().equals(environment))
                .findFirst()
                .orElse(null);
        if (existing != null) {
            return existing;
        }

        RuntimeSdkTarget target = RuntimeSdkTarget.builder()
                .id(UUID.randomUUID().toString())
                .flagKey(flagKey)
                .userId(userId)
                .environment(environment)
                .build();
        runtimeTargets.put(target.getId(), target);
        return target;
    }

    private void validateTarget(String flagKey, String userId, String environment) {
        if (flagKey == null || flagKey.isBlank()) {
            throw new IllegalArgumentException("Flag key is required");
        }
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("User ID is required");
        }
        if (environment == null || environment.isBlank()) {
            throw new IllegalArgumentException("Environment is required");
        }
    }

    private String resolveBaseUrl(String baseUrl) {
        return baseUrl == null || baseUrl.isBlank() ? gatekeeperSdkProperties.getBaseUrl() : baseUrl;
    }

    private SdkEvaluationResponse toResponse(GatekeeperJavaClient.SdkEvaluationResult result) {
        return SdkEvaluationResponse.builder()
                .baseUrl(result.getBaseUrl())
                .flagKey(result.getFlagKey())
                .userId(result.getUserId())
                .environment(result.getEnvironment())
                .enabled(result.isEnabled())
                .source(result.getSource())
                .lastFetchedAt(result.getLastFetchedAt())
                .expiresAt(result.getExpiresAt())
                .build();
    }

    @Getter
    @Builder
    private static class RuntimeSdkTarget {
        private final String id;
        private final String flagKey;
        private final String userId;
        private final String environment;
    }
}
