package com.gatekeeper.dto;

import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SdkEvaluationForm {

    private String baseUrl;
    private String flagKey;
    private String userId;
    private String environment;
}
