package com.gatekeeper.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class GatekeeperMetricsServiceTest {

    private final GatekeeperMetricsService gatekeeperMetricsService = new GatekeeperMetricsService();

    @Test
    void aggregatesEvaluationCountsAndRatios() {
        gatekeeperMetricsService.recordEvaluation("checkout", true, true);
        gatekeeperMetricsService.recordEvaluation("checkout", false, false);
        gatekeeperMetricsService.recordEvaluation("checkout", true, false);

        var metrics = gatekeeperMetricsService.getMetrics("checkout");

        assertThat(metrics.getTotalEvaluations()).isEqualTo(3);
        assertThat(metrics.getEnabledEvaluations()).isEqualTo(2);
        assertThat(metrics.getDisabledEvaluations()).isEqualTo(1);
        assertThat(metrics.getCacheHits()).isEqualTo(1);
        assertThat(metrics.getCacheMisses()).isEqualTo(2);
        assertThat(metrics.getEnabledRatio()).isEqualTo(2.0 / 3.0);
    }
}
