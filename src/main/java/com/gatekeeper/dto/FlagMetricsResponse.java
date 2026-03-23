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
public class FlagMetricsResponse {

    private String flagKey;
    private long totalEvaluations;
    private long enabledEvaluations;
    private long disabledEvaluations;
    private long cacheHits;
    private long cacheMisses;
    private double enabledRatio;
}
