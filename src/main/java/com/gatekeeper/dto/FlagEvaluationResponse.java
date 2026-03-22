package com.gatekeeper.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlagEvaluationResponse {

    private String featureKey;
    private String userId;
    private boolean enabled;
    private String reason;
}
