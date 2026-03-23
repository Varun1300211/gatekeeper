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
public class GatekeeperEvaluationForm {

    private String flagKey;
    private String userId;
    private String environment;
}
