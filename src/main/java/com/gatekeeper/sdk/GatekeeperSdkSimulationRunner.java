package com.gatekeeper.sdk;

import jakarta.annotation.PreDestroy;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import com.gatekeeper.service.GatekeeperSdkService;

@Slf4j
@Component
@Profile("sdk-simulator")
@RequiredArgsConstructor
@EnableConfigurationProperties(GatekeeperSdkProperties.class)
public class GatekeeperSdkSimulationRunner implements CommandLineRunner {

    private final GatekeeperJavaClient gatekeeperJavaClient;
    private final GatekeeperSdkProperties gatekeeperSdkProperties;
    private final GatekeeperSdkService gatekeeperSdkService;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    @Override
    public void run(String... args) {
        log.info(
                "Starting GateKeeper SDK simulator against {} with {} configured target(s), pollInterval={}s, localCacheTtl={}s",
                gatekeeperSdkProperties.getBaseUrl(),
                gatekeeperSdkService.getConfiguredTargets().size(),
                gatekeeperSdkProperties.getPollIntervalSeconds(),
                gatekeeperSdkProperties.getLocalCacheTtlSeconds());

        scheduler.scheduleAtFixedRate(
                this::pollEvaluationSafely,
                0,
                gatekeeperSdkProperties.getPollIntervalSeconds(),
                TimeUnit.SECONDS);
    }

    private void pollEvaluationSafely() {
        try {
            Duration cacheTtl = Duration.ofSeconds(gatekeeperSdkProperties.getLocalCacheTtlSeconds());
            for (com.gatekeeper.dto.SdkTargetResponse target : gatekeeperSdkService.getConfiguredTargets()) {
                GatekeeperJavaClient.SdkEvaluationResult evaluationResult = gatekeeperJavaClient.isEnabled(
                        gatekeeperSdkProperties.getBaseUrl(),
                        target.getFlagKey(),
                        target.getUserId(),
                        target.getEnvironment(),
                        cacheTtl);

                log.info(
                        "SDK simulator evaluated target: key={} userId={} environment={} enabled={} source={} lastFetchedAt={} expiresAt={}",
                        target.getFlagKey(),
                        target.getUserId(),
                        target.getEnvironment(),
                        evaluationResult.isEnabled(),
                        evaluationResult.getSource(),
                        evaluationResult.getLastFetchedAt(),
                        evaluationResult.getExpiresAt());
            }
        } catch (Exception exception) {
            log.warn("SDK simulator failed to poll GateKeeper: {}", exception.getMessage());
        }
    }

    @PreDestroy
    void shutdown() {
        scheduler.shutdownNow();
    }
}
