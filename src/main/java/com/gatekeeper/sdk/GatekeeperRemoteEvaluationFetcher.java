package com.gatekeeper.sdk;

import com.gatekeeper.dto.GatekeeperEvaluationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class GatekeeperRemoteEvaluationFetcher {

    private final RestClient.Builder restClientBuilder;

    public GatekeeperEvaluationResponse fetchEvaluation(String baseUrl, String flagKey, String userId, String environment) {
        return restClientBuilder
                .baseUrl(baseUrl)
                .build()
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
