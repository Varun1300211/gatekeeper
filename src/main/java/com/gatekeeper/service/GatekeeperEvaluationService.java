package com.gatekeeper.service;

import com.gatekeeper.model.Environment;
import com.gatekeeper.model.GatekeeperFlag;
import com.gatekeeper.model.FlagRule;
import com.gatekeeper.model.RuleType;
import com.gatekeeper.repository.EnvironmentRepository;
import com.gatekeeper.repository.GatekeeperFlagRepository;
import com.gatekeeper.repository.FlagRuleRepository;
import com.gatekeeper.repository.UserTargetRepository;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GatekeeperEvaluationService {

    private final GatekeeperFlagRepository gatekeeperFlagRepository;
    private final EnvironmentRepository environmentRepository;
    private final FlagRuleRepository flagRuleRepository;
    private final UserTargetRepository userTargetRepository;

    @Transactional(readOnly = true)
    public boolean evaluate(String flagKey, String userId, String environmentName) {
        GatekeeperFlag flag = gatekeeperFlagRepository.findByKey(flagKey)
                .orElse(null);
        if (flag == null || !flag.isEnabled()) {
            return false;
        }

        Environment environment = environmentRepository.findByName(environmentName)
                .orElse(null);
        if (environment == null) {
            return false;
        }

        List<FlagRule> rules = flagRuleRepository.findByFlagAndEnvironment(flag, environment);
        if (rules.isEmpty()) {
            return false;
        }

        if (hasEnabledRule(rules, RuleType.GLOBAL)) {
            return true;
        }

        if (matchesUserTarget(rules, userId)) {
            return true;
        }

        return matchesPercentage(rules, flagKey, userId, environmentName);
    }

    private boolean hasEnabledRule(List<FlagRule> rules, RuleType ruleType) {
        return rules.stream()
                .anyMatch(rule -> rule.isEnabled() && rule.getRuleType() == ruleType);
    }

    private boolean matchesUserTarget(List<FlagRule> rules, String userId) {
        return rules.stream()
                .filter(rule -> rule.isEnabled() && rule.getRuleType() == RuleType.USER_TARGET)
                .anyMatch(rule -> userTargetRepository.findByFlagRule(rule).stream()
                        .anyMatch(target -> target.getUserId().equals(userId)));
    }

    private boolean matchesPercentage(List<FlagRule> rules, String flagKey, String userId, String environmentName) {
        return rules.stream()
                .filter(rule -> rule.isEnabled() && rule.getRuleType() == RuleType.PERCENTAGE)
                .anyMatch(rule -> bucket(flagKey, userId, environmentName) < normalizedPercentage(rule.getPercentage()));
    }

    private int normalizedPercentage(Integer percentage) {
        if (percentage == null) {
            return 0;
        }
        return Math.max(0, Math.min(percentage, 100));
    }

    private int bucket(String flagKey, String userId, String environmentName) {
        String input = flagKey + ":" + userId + ":" + environmentName;
        int hash = java.util.Arrays.hashCode(input.getBytes(StandardCharsets.UTF_8));
        return Math.floorMod(hash, 100);
    }
}
