package com.gatekeeper.service;

import com.gatekeeper.dto.FlagMetricsResponse;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;
import org.springframework.stereotype.Service;

@Service
public class GatekeeperMetricsService {

    private final Map<String, FlagMetricCounter> flagMetrics = new ConcurrentHashMap<>();

    public void recordEvaluation(String flagKey, boolean enabled, boolean cacheHit) {
        FlagMetricCounter counter = flagMetrics.computeIfAbsent(flagKey, ignored -> new FlagMetricCounter());
        counter.totalEvaluations.increment();

        if (enabled) {
            counter.enabledEvaluations.increment();
        } else {
            counter.disabledEvaluations.increment();
        }

        if (cacheHit) {
            counter.cacheHits.increment();
        } else {
            counter.cacheMisses.increment();
        }
    }

    public List<FlagMetricsResponse> getMetrics() {
        return flagMetrics.entrySet().stream()
                .map(entry -> toResponse(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(FlagMetricsResponse::getFlagKey))
                .toList();
    }

    public FlagMetricsResponse getMetrics(String flagKey) {
        FlagMetricCounter counter = flagMetrics.get(flagKey);
        if (counter == null) {
            return FlagMetricsResponse.builder()
                    .flagKey(flagKey)
                    .totalEvaluations(0)
                    .enabledEvaluations(0)
                    .disabledEvaluations(0)
                    .cacheHits(0)
                    .cacheMisses(0)
                    .enabledRatio(0)
                    .build();
        }
        return toResponse(flagKey, counter);
    }

    private FlagMetricsResponse toResponse(String flagKey, FlagMetricCounter counter) {
        long total = counter.totalEvaluations.sum();
        long enabled = counter.enabledEvaluations.sum();
        long disabled = counter.disabledEvaluations.sum();
        long hits = counter.cacheHits.sum();
        long misses = counter.cacheMisses.sum();

        return FlagMetricsResponse.builder()
                .flagKey(flagKey)
                .totalEvaluations(total)
                .enabledEvaluations(enabled)
                .disabledEvaluations(disabled)
                .cacheHits(hits)
                .cacheMisses(misses)
                .enabledRatio(total == 0 ? 0 : (double) enabled / total)
                .build();
    }

    private static final class FlagMetricCounter {
        private final LongAdder totalEvaluations = new LongAdder();
        private final LongAdder enabledEvaluations = new LongAdder();
        private final LongAdder disabledEvaluations = new LongAdder();
        private final LongAdder cacheHits = new LongAdder();
        private final LongAdder cacheMisses = new LongAdder();
    }
}
