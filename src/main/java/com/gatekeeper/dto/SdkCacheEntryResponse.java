package com.gatekeeper.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SdkCacheEntryResponse {

    private final String baseUrl;
    private final String flagKey;
    private final String userId;
    private final String environment;
    private final boolean enabled;
    private final LocalDateTime lastFetchedAt;
    private final LocalDateTime expiresAt;
    private final boolean expired;
}
