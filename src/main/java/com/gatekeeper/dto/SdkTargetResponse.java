package com.gatekeeper.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SdkTargetResponse {

    private final String id;
    private final String flagKey;
    private final String userId;
    private final String environment;
}
