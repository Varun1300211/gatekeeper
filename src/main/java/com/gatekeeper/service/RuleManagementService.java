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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static com.gatekeeper.config.CacheConfig.EVALUATION_CACHE;

@Service
@RequiredArgsConstructor
public class RuleManagementService {

    private final GatekeeperFlagRepository gatekeeperFlagRepository;
    private final EnvironmentRepository environmentRepository;
    private final FlagRuleRepository flagRuleRepository;
    private final UserTargetRepository userTargetRepository;
    private final AuditLogService auditLogService;

    @Transactional
    @CacheEvict(cacheNames = EVALUATION_CACHE, allEntries = true)
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

        FlagRule savedRule = flagRuleRepository.save(rule);
        auditLogService.log(
                "FLAG_RULE",
                savedRule.getId(),
                "CREATED",
                "Added " + savedRule.getRuleType() + " rule for flag '" + flag.getKey()
                        + "' in environment '" + environment.getName() + "'");
        return toResponse(savedRule);
    }

    @Transactional
    @CacheEvict(cacheNames = EVALUATION_CACHE, allEntries = true)
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
        auditLogService.log(
                "FLAG_RULE",
                rule.getId(),
                "USER_TARGETS_UPDATED",
                "Added user targets " + request.getUserIds() + " to rule " + rule.getId());
        return toResponse(findRule(ruleId));
    }

    @Transactional
    @CacheEvict(cacheNames = EVALUATION_CACHE, allEntries = true)
    public RuleResponse setPercentageRollout(Long ruleId, PercentageRolloutRequest request) {
        FlagRule rule = findRule(ruleId);
        rule.setPercentage(request.getPercentage());
        FlagRule savedRule = flagRuleRepository.save(rule);
        auditLogService.log(
                "FLAG_RULE",
                savedRule.getId(),
                "PERCENTAGE_UPDATED",
                "Set percentage rollout to " + request.getPercentage() + " for rule " + savedRule.getId());
        return toResponse(savedRule);
    }

    @Transactional
    @CacheEvict(cacheNames = EVALUATION_CACHE, allEntries = true)
    public RuleResponse updateRuleStatus(Long ruleId, RuleStatusUpdateRequest request) {
        FlagRule rule = findRule(ruleId);
        rule.setEnabled(request.isEnabled());
        FlagRule savedRule = flagRuleRepository.save(rule);
        auditLogService.log(
                "FLAG_RULE",
                savedRule.getId(),
                "STATUS_UPDATED",
                "Set rule " + savedRule.getId() + " enabled=" + request.isEnabled());
        return toResponse(savedRule);
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
