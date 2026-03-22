package com.gatekeeper.evaluation;

import com.gatekeeper.dto.FlagEvaluationRequest;
import com.gatekeeper.dto.FlagEvaluationResponse;
import com.gatekeeper.model.FlagRule;
import com.gatekeeper.model.GatekeeperFlag;
import com.gatekeeper.model.RuleType;
import org.springframework.stereotype.Component;

@Component
public class FlagEvaluationEngine {

    public FlagEvaluationResponse evaluate(GatekeeperFlag gatekeeperFlag, FlagEvaluationRequest request) {
        if (!gatekeeperFlag.isEnabled()) {
            return disabled(request, "Flag is globally disabled");
        }

        for (FlagRule rule : gatekeeperFlag.getRules()) {
            if (!rule.isEnabled()) {
                continue;
            }

            if (rule.getRuleType() == RuleType.GLOBAL) {
                return enabled(request, "Matched global rule");
            }

            if (rule.getRuleType() == RuleType.USER_TARGET
                    && rule.getUserTargets().stream().anyMatch(target -> target.getUserId().equals(request.getUserId()))) {
                return enabled(request, "Matched user target rule");
            }

            if (rule.getRuleType() == RuleType.PERCENTAGE && matchesPercentage(rule, request.getUserId())) {
                return enabled(request, "Matched percentage rollout rule");
            }
        }

        return disabled(request, "User did not meet evaluation criteria");
    }

    private boolean matchesPercentage(FlagRule rule, String userId) {
        int percentage = rule.getPercentage() == null ? 0 : rule.getPercentage();
        int bucket = Math.floorMod(userId.hashCode(), 100);
        return bucket < percentage;
    }

    private FlagEvaluationResponse enabled(FlagEvaluationRequest request, String reason) {
        return FlagEvaluationResponse.builder()
                .featureKey(request.getFeatureKey())
                .userId(request.getUserId())
                .enabled(true)
                .reason(reason)
                .build();
    }

    private FlagEvaluationResponse disabled(FlagEvaluationRequest request, String reason) {
        return FlagEvaluationResponse.builder()
                .featureKey(request.getFeatureKey())
                .userId(request.getUserId())
                .enabled(false)
                .reason(reason)
                .build();
    }
}
