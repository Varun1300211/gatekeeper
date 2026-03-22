package com.gatekeeper.dto;

import jakarta.validation.constraints.NotBlank;
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
public class FlagEvaluationRequest {

    @NotBlank
    private String featureKey;

    @NotBlank
    private String userId;

    private String segment;
}
