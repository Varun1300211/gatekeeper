package com.gatekeeper.controller;

import com.gatekeeper.dto.GatekeeperEvaluationResponse;
import com.gatekeeper.service.GatekeeperEvaluationService;
import com.gatekeeper.service.GatekeeperMetricsService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import static com.gatekeeper.config.CacheConfig.EVALUATION_CACHE;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class GatekeeperEvaluationController {

    private final GatekeeperEvaluationService gatekeeperEvaluationService;
    private final GatekeeperMetricsService gatekeeperMetricsService;
    private final CacheManager cacheManager;

    @GetMapping("/evaluate")
    public GatekeeperEvaluationResponse evaluate(
            @RequestParam String flagKey,
            @RequestParam String userId,
            @RequestParam String environment) {
        boolean cacheHit = isCacheHit(flagKey, userId, environment);
        boolean enabled = gatekeeperEvaluationService.evaluate(flagKey, userId, environment);
        gatekeeperMetricsService.recordEvaluation(flagKey, enabled, cacheHit);

        return GatekeeperEvaluationResponse.builder()
                .flagKey(flagKey)
                .userId(userId)
                .environment(environment)
                .enabled(enabled)
                .build();
    }

    private boolean isCacheHit(String flagKey, String userId, String environment) {
        Cache cache = cacheManager.getCache(EVALUATION_CACHE);
        if (cache == null) {
            return false;
        }

        String cacheKey = flagKey + ":" + userId + ":" + environment;
        return cache.get(cacheKey) != null;
    }
}
