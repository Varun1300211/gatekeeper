package com.gatekeeper.controller;

import com.gatekeeper.dto.PercentageRolloutRequest;
import com.gatekeeper.dto.RuleRequest;
import com.gatekeeper.dto.RuleResponse;
import com.gatekeeper.dto.RuleStatusUpdateRequest;
import com.gatekeeper.dto.UserTargetsRequest;
import com.gatekeeper.service.RuleManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class RuleManagementController {

    private final RuleManagementService ruleManagementService;

    @PostMapping("/flags/{flagId}/rules")
    @ResponseStatus(HttpStatus.CREATED)
    public RuleResponse addRuleToFlag(@PathVariable Long flagId, @Valid @RequestBody RuleRequest request) {
        return ruleManagementService.addRuleToFlag(flagId, request);
    }

    @PostMapping("/rules/{ruleId}/targets")
    public RuleResponse addUserTargets(@PathVariable Long ruleId, @Valid @RequestBody UserTargetsRequest request) {
        return ruleManagementService.addUserTargets(ruleId, request);
    }

    @PutMapping("/rules/{ruleId}/percentage")
    public RuleResponse setPercentageRollout(
            @PathVariable Long ruleId,
            @Valid @RequestBody PercentageRolloutRequest request) {
        return ruleManagementService.setPercentageRollout(ruleId, request);
    }

    @PatchMapping("/rules/{ruleId}/status")
    public RuleResponse updateRuleStatus(
            @PathVariable Long ruleId,
            @Valid @RequestBody RuleStatusUpdateRequest request) {
        return ruleManagementService.updateRuleStatus(ruleId, request);
    }
}
