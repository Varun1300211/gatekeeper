package com.gatekeeper.sdk;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@Component
public class GatekeeperSdkRestClientFactory {

    private final RestClient.Builder restClientBuilder;
    private final GatekeeperSdkProperties gatekeeperSdkProperties;

    public GatekeeperSdkRestClientFactory(
            RestClient.Builder restClientBuilder,
            GatekeeperSdkProperties gatekeeperSdkProperties) {
        this.restClientBuilder = restClientBuilder;
        this.gatekeeperSdkProperties = gatekeeperSdkProperties;
    }

    public RestClient create(String baseUrl) {
        RestClient.Builder builder = restClientBuilder.clone().baseUrl(baseUrl);

        if (StringUtils.hasText(gatekeeperSdkProperties.getUsername())
                && StringUtils.hasText(gatekeeperSdkProperties.getPassword())) {
            builder.defaultHeaders(headers -> headers.setBasicAuth(
                    gatekeeperSdkProperties.getUsername(),
                    gatekeeperSdkProperties.getPassword()));
        }

        return builder.build();
    }
}
