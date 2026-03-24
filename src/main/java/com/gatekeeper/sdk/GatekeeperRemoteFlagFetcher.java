package com.gatekeeper.sdk;

import com.gatekeeper.dto.GatekeeperFlagResponse;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class GatekeeperRemoteFlagFetcher {

    private final GatekeeperSdkRestClientFactory restClientFactory;

    public GatekeeperRemoteFlagFetcher(GatekeeperSdkRestClientFactory restClientFactory) {
        this.restClientFactory = restClientFactory;
    }

    public List<GatekeeperFlagResponse> fetchFlags(String baseUrl) {
        GatekeeperFlagResponse[] response = restClientFactory
                .create(baseUrl)
                .get()
                .uri("/api/flags")
                .retrieve()
                .body(GatekeeperFlagResponse[].class);

        return response == null ? List.of() : List.of(response);
    }
}
