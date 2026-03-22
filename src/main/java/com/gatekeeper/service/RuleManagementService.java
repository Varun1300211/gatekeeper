package com.gatekeeper.service;

import com.gatekeeper.dto.PercentageRolloutRequest;
import com.gatekeeper.dto.RuleRequest;
import com.gatekeeper.dto.RuleResponse;
import com.gatekeeper.dto.RuleStatusUpdateRequest;
import com.gatekeeper.dto.UserTargetsRequest;
import com.gatekeeper.model.Environment;
import com.gatekeeper.model.GatekeeperFlag;
import com.gatekeeper.model.FlagRule;
import com.gatekeeper.model.RuleType;
import com.gatekeeper.model.UserTarget;
import com.gatekeeper.repository.EnvironmentRepository;
import com.gatekeeper.repository.GatekeeperFlagRepository;
import com.gatekeeper.repository.FlagRuleRepository;
import com.gatekeeper.repository.UserTargetRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RuleManagementService {

    private final GatekeeperFlagRepository gatekeeperFlagRepository;
    private final EnvironmentRepository environmentRepository;
    private final FlagRuleRepository flagRuleRepository;
    private final UserTargetRepository userTargetRepository;

    @Transactional
    public RuleResponse addRuleToFlag(Long flagId, RuleRequest request) {
        GatekeeperFlag flag = gatekeeperFlagRepository.findById(flagId)
                .orElseThrow(() -> new EntityNotFoundException("Gatekeeper flag not found: " + flagId));

        Environment environment = findEnvironment(request.getEnvironment());

        FlagRule rule = FlagRule.builder()
                .flag(flag)
                .environment(environment)
                .ruleType(request.getRuleType())
                .percentage(resolveInitialPercentage(request))
                .enabled(request.isEnabled())
                .build();

        return toResponse(flagRuleRepository.save(rule));
    }

    @Transactional
    public RuleResponse addUserTargets(Long ruleId, UserTargetsRequest request) {
        FlagRule rule = findRule(ruleId);

        List<UserTarget> targets = request.getUserIds().stream()
                .distinct()
                .map(userId -> UserTarget.builder()
                        .flagRule(rule)
                        .userId(userId)
                        .build())
                .toList();

        userTargetRepository.saveAll(targets);
        return toResponse(findRule(ruleId));
    }

    @Transactional
    public RuleResponse setPercentageRollout(Long ruleId, PercentageRolloutRequest request) {
        FlagRule rule = findRule(ruleId);
        rule.setPercentage(request.getPercentage());
        return toResponse(flagRuleRepository.save(rule));
    }

    @Transactional
    public RuleResponse updateRuleStatus(Long ruleId, RuleStatusUpdateRequest request) {
        FlagRule rule = findRule(ruleId);
        rule.setEnabled(request.isEnabled());
        return toResponse(flagRuleRepository.save(rule));
    }

    @Transactional(readOnly = true)
    public List<RuleResponse> getRulesForFlag(Long flagId) {
        GatekeeperFlag flag = gatekeeperFlagRepository.findById(flagId)
                .orElseThrow(() -> new EntityNotFoundException("Gatekeeper flag not found: " + flagId));

        return flagRuleRepository.findByFlag(flag).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<String> getAvailableEnvironments() {
        return environmentRepository.findAll().stream()
                .map(Environment::getName)
                .sorted(String::compareToIgnoreCase)
                .toList();
    }

    @Transactional
    public RuleResponse addUserTargetsFromCsv(Long ruleId, String userIdsCsv) {
        List<String> userIds = List.of(userIdsCsv.split(",")).stream()
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .distinct()
                .toList();

        return addUserTargets(ruleId, UserTargetsRequest.builder().userIds(userIds).build());
    }

    private Environment findEnvironment(String name) {
        return environmentRepository.findByName(name)
                .orElseThrow(() -> new EntityNotFoundException("Environment not found: " + name));
    }

    private FlagRule findRule(Long ruleId) {
        return flagRuleRepository.findById(ruleId)
                .orElseThrow(() -> new EntityNotFoundException("Flag rule not found: " + ruleId));
    }

    private Integer resolveInitialPercentage(RuleRequest request) {
        if (request.getRuleType() != RuleType.PERCENTAGE) {
            return null;
        }
        return request.getPercentage() == null ? 0 : request.getPercentage();
    }

    private RuleResponse toResponse(FlagRule rule) {
        List<String> userTargets = userTargetRepository.findByFlagRule(rule).stream()
                .map(UserTarget::getUserId)
                .toList();

        return RuleResponse.builder()
                .id(rule.getId())
                .flagId(rule.getFlag().getId())
                .flagKey(rule.getFlag().getKey())
                .environment(rule.getEnvironment().getName())
                .ruleType(rule.getRuleType())
                .percentage(rule.getPercentage())
                .enabled(rule.isEnabled())
                .userTargets(userTargets)
                .build();
    }
}
