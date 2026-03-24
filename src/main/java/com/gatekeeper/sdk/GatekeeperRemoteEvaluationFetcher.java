package com.gatekeeper.sdk;

import com.gatekeeper.dto.GatekeeperEvaluationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GatekeeperRemoteEvaluationFetcher {

    private final GatekeeperSdkRestClientFactory restClientFactory;

    public GatekeeperEvaluationResponse fetchEvaluation(String baseUrl, String flagKey, String userId, String environment) {
        return restClientFactory
                .create(baseUrl)
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/evaluate")
                        .queryParam("flagKey", flagKey)
                        .queryParam("userId", userId)
                        .queryParam("environment", environment)
                        .build())
                .retrieve()
                .body(GatekeeperEvaluationResponse.class);
    }
}
