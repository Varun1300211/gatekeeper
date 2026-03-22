package com.gatekeeper.controller;

import com.gatekeeper.dto.GatekeeperEvaluationResponse;
import com.gatekeeper.service.GatekeeperEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class GatekeeperEvaluationController {

    private final GatekeeperEvaluationService gatekeeperEvaluationService;

    @GetMapping("/evaluate")
    public GatekeeperEvaluationResponse evaluate(
            @RequestParam String flagKey,
            @RequestParam String userId,
            @RequestParam String environment) {
        boolean enabled = gatekeeperEvaluationService.evaluate(flagKey, userId, environment);

        return GatekeeperEvaluationResponse.builder()
                .flagKey(flagKey)
                .userId(userId)
                .environment(environment)
                .enabled(enabled)
                .build();
    }
}
