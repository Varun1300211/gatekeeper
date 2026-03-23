package com.gatekeeper.controller;

import com.gatekeeper.dto.FlagMetricsResponse;
import com.gatekeeper.service.GatekeeperMetricsService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/metrics")
public class GatekeeperMetricsController {

    private final GatekeeperMetricsService gatekeeperMetricsService;

    @GetMapping("/flags")
    public Object getFlagMetrics(@RequestParam(required = false) String flagKey) {
        if (flagKey != null && !flagKey.isBlank()) {
            return gatekeeperMetricsService.getMetrics(flagKey);
        }
        List<FlagMetricsResponse> metrics = gatekeeperMetricsService.getMetrics();
        return metrics;
    }
}
