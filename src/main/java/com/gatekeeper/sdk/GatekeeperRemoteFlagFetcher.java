package com.gatekeeper.sdk;

import com.gatekeeper.dto.GatekeeperFlagResponse;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class GatekeeperRemoteFlagFetcher {

    private final RestClient.Builder restClientBuilder;

    public GatekeeperRemoteFlagFetcher(RestClient.Builder restClientBuilder) {
        this.restClientBuilder = restClientBuilder;
    }

    public List<GatekeeperFlagResponse> fetchFlags(String baseUrl) {
        GatekeeperFlagResponse[] response = restClientBuilder
                .baseUrl(baseUrl)
                .build()
                .get()
                .uri("/api/flags")
                .retrieve()
                .body(GatekeeperFlagResponse[].class);

        return response == null ? List.of() : List.of(response);
    }
}
