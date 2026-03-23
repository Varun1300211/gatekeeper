package com.gatekeeper.dto;

import com.gatekeeper.sdk.SdkEvaluationSource;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SdkEvaluationResponse {

    private final String baseUrl;
    private final String flagKey;
    private final String userId;
    private final String environment;
    private final boolean enabled;
    private final SdkEvaluationSource source;
    private final LocalDateTime lastFetchedAt;
    private final LocalDateTime expiresAt;
}
