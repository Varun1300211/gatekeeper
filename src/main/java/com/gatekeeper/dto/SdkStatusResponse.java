package com.gatekeeper.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SdkStatusResponse {

    private final String baseUrl;
    private final String username;
    private final long pollIntervalSeconds;
    private final long localCacheTtlSeconds;
    private final List<SdkTargetResponse> configuredTargets;
    private final List<SdkCacheEntryResponse> localCacheEntries;
}
