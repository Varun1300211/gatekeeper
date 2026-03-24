package com.gatekeeper.controller;

import com.gatekeeper.dto.AuditLogFilterForm;
import com.gatekeeper.dto.GatekeeperEvaluationForm;
import com.gatekeeper.dto.GatekeeperEvaluationResponse;
import com.gatekeeper.dto.GatekeeperFlagRequest;
import com.gatekeeper.dto.PercentageRolloutRequest;
import com.gatekeeper.dto.RuleRequest;
import com.gatekeeper.dto.RuleStatusForm;
import com.gatekeeper.dto.RuleStatusUpdateRequest;
import com.gatekeeper.dto.SdkEvaluationForm;
import com.gatekeeper.dto.SdkTargetForm;
import com.gatekeeper.dto.UserTargetsForm;
import com.gatekeeper.model.RuleType;
import com.gatekeeper.service.AuditLogService;
import com.gatekeeper.service.GatekeeperEvaluationService;
import com.gatekeeper.service.GatekeeperFlagService;
import com.gatekeeper.service.GatekeeperMetricsService;
import com.gatekeeper.service.GatekeeperSdkService;
import com.gatekeeper.service.RuleManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class GatekeeperController {

    private final AuditLogService auditLogService;
    private final GatekeeperEvaluationService gatekeeperEvaluationService;
    private final GatekeeperFlagService gatekeeperFlagService;
    private final GatekeeperMetricsService gatekeeperMetricsService;
    private final GatekeeperSdkService gatekeeperSdkService;
    private final RuleManagementService ruleManagementService;

    @GetMapping("/")
    public String dashboard(Model model) {
        model.addAttribute("flags", gatekeeperFlagService.getAllFlags());
        return "flags/list";
    }

    @GetMapping("/flags")
    public String listFlags(Model model) {
        model.addAttribute("flags", gatekeeperFlagService.getAllFlags());
        return "flags/list";
    }

    @GetMapping("/metrics")
    public String metricsPage(Model model) {
        model.addAttribute("metrics", gatekeeperMetricsService.getMetrics());
        return "metrics/list";
    }

    @GetMapping("/sdk")
    public String sdkPage(Model model) {
        populateSdkPageModel(
                model,
                SdkEvaluationForm.builder()
                        .baseUrl(gatekeeperSdkService.defaultBaseUrl())
                        .environment("prod")
                        .build(),
                SdkTargetForm.builder().environment("prod").build(),
                null,
                null,
                null);
        return "sdk/status";
    }

    @PostMapping("/sdk/evaluate")
    public String sdkEvaluate(@ModelAttribute("sdkEvaluationForm") SdkEvaluationForm form, Model model) {
        String sdkMessage = null;
        com.gatekeeper.dto.SdkEvaluationResponse sdkEvaluationResult = null;
        try {
            sdkEvaluationResult = gatekeeperSdkService.evaluateWithSdk(
                    form.getBaseUrl(),
                    form.getFlagKey(),
                    form.getUserId(),
                    form.getEnvironment());
        } catch (Exception exception) {
            sdkMessage = "SDK evaluation failed. Check the base URL, credentials, and whether the target flag exists.";
        }
        populateSdkPageModel(
                model,
                form,
                SdkTargetForm.builder().environment("prod").build(),
                sdkEvaluationResult,
                null,
                sdkMessage);
        return "sdk/status";
    }

    @PostMapping("/sdk/refresh-configured")
    public String sdkRefreshConfigured(Model model) {
        String sdkMessage = null;
        var refreshResults = java.util.List.<com.gatekeeper.dto.SdkEvaluationResponse>of();
        try {
            refreshResults = gatekeeperSdkService.refreshConfiguredTargets();
        } catch (Exception exception) {
            sdkMessage = "Refreshing configured SDK targets failed. Check the main GateKeeper app URL and credentials.";
        }
        populateSdkPageModel(
                model,
                SdkEvaluationForm.builder()
                        .baseUrl(gatekeeperSdkService.defaultBaseUrl())
                        .environment("prod")
                        .build(),
                SdkTargetForm.builder().environment("prod").build(),
                null,
                refreshResults,
                sdkMessage);
        return "sdk/status";
    }

    @PostMapping("/sdk/targets")
    public String sdkAddTarget(@ModelAttribute("sdkTargetForm") SdkTargetForm form, Model model) {
        gatekeeperSdkService.addConfiguredTarget(form);
        populateSdkPageModel(
                model,
                SdkEvaluationForm.builder()
                        .baseUrl(gatekeeperSdkService.defaultBaseUrl())
                        .environment(form.getEnvironment())
                        .build(),
                SdkTargetForm.builder().environment("prod").build(),
                null,
                null,
                null);
        return "sdk/status";
    }

    @PostMapping("/sdk/targets/{id}/delete")
    public String sdkDeleteTarget(@PathVariable String id, Model model) {
        gatekeeperSdkService.removeConfiguredTarget(id);
        populateSdkPageModel(
                model,
                SdkEvaluationForm.builder()
                        .baseUrl(gatekeeperSdkService.defaultBaseUrl())
                        .environment("prod")
                        .build(),
                SdkTargetForm.builder().environment("prod").build(),
                null,
                null,
                null);
        return "sdk/status";
    }

    @PostMapping("/sdk/cache/clear")
    public String sdkClearLocalCache(Model model) {
        gatekeeperSdkService.clearLocalCache();
        populateSdkPageModel(
                model,
                SdkEvaluationForm.builder()
                        .baseUrl(gatekeeperSdkService.defaultBaseUrl())
                        .environment("prod")
                        .build(),
                SdkTargetForm.builder().environment("prod").build(),
                null,
                null,
                null);
        return "sdk/status";
    }

    private void populateSdkPageModel(
            Model model,
            SdkEvaluationForm sdkEvaluationForm,
            SdkTargetForm sdkTargetForm,
            com.gatekeeper.dto.SdkEvaluationResponse sdkEvaluationResult,
            java.util.List<com.gatekeeper.dto.SdkEvaluationResponse> sdkRefreshResults,
            String sdkMessage) {
        model.addAttribute("sdkStatus", gatekeeperSdkService.getStatus());
        model.addAttribute("availableSdkFlags", gatekeeperSdkService.getAvailableFlagKeys());
        model.addAttribute("availableSdkFlagsError", gatekeeperSdkService.getAvailableFlagKeysError());
        model.addAttribute("sdkEvaluationForm", sdkEvaluationForm);
        model.addAttribute("sdkTargetForm", sdkTargetForm);
        model.addAttribute("sdkEvaluationResult", sdkEvaluationResult);
        model.addAttribute("sdkRefreshResults", sdkRefreshResults);
        model.addAttribute("sdkMessage", sdkMessage);
    }

    @GetMapping("/audit-logs")
    public String auditLogsPage(
            @org.springframework.web.bind.annotation.RequestParam(required = false) String entityType,
            @org.springframework.web.bind.annotation.RequestParam(required = false) Long entityId,
            Model model) {
        AuditLogFilterForm filterForm = AuditLogFilterForm.builder()
                .entityType(entityType)
                .entityId(entityId)
                .build();

        model.addAttribute("filterForm", filterForm);
        model.addAttribute(
                "auditLogs",
                entityType != null && !entityType.isBlank() && entityId != null
                        ? auditLogService.getAuditLogs(entityType, entityId)
                        : auditLogService.getAuditLogs());
        return "audit/list";
    }

    @GetMapping("/evaluate")
    public String evaluatePage(Model model) {
        model.addAttribute("evaluationForm", GatekeeperEvaluationForm.builder()
                .environment("prod")
                .build());
        model.addAttribute("evaluationResult", null);
        return "evaluation/form";
    }

    @PostMapping("/evaluate")
    public String evaluateFlag(@ModelAttribute("evaluationForm") GatekeeperEvaluationForm form, Model model) {
        boolean enabled = gatekeeperEvaluationService.evaluate(
                form.getFlagKey(),
                form.getUserId(),
                form.getEnvironment());

        model.addAttribute("evaluationForm", form);
        model.addAttribute("evaluationResult", GatekeeperEvaluationResponse.builder()
                .flagKey(form.getFlagKey())
                .userId(form.getUserId())
                .environment(form.getEnvironment())
                .enabled(enabled)
                .build());
        return "evaluation/form";
    }

    @GetMapping("/flags/create")
    public String createFlagPage(Model model) {
        model.addAttribute("flagForm", new GatekeeperFlagRequest());
        return "flags/create";
    }

    @PostMapping("/flags/create")
    public String createFlag(@ModelAttribute("flagForm") GatekeeperFlagRequest request) {
        gatekeeperFlagService.createFlag(request);
        return "redirect:/flags";
    }

    @GetMapping("/flags/{id}")
    public String flagDetails(@PathVariable Long id, Model model) {
        model.addAttribute("flag", gatekeeperFlagService.getFlag(id));
        model.addAttribute("rules", ruleManagementService.getRulesForFlag(id));
        return "flags/details";
    }

    @PostMapping("/flags/{id}/edit")
    public String updateFlag(@PathVariable Long id, @ModelAttribute("flag") GatekeeperFlagRequest request) {
        gatekeeperFlagService.updateFlag(id, request);
        return "redirect:/flags/" + id;
    }

    @PostMapping("/flags/{id}/delete")
    public String deleteFlag(@PathVariable Long id) {
        gatekeeperFlagService.deleteFlag(id);
        return "redirect:/flags";
    }

    @GetMapping("/flags/{id}/rules")
    public String ruleManagementPage(@PathVariable Long id, Model model) {
        model.addAttribute("flag", gatekeeperFlagService.getFlag(id));
        model.addAttribute("rules", ruleManagementService.getRulesForFlag(id));
        model.addAttribute("availableEnvironments", ruleManagementService.getAvailableEnvironments());
        model.addAttribute("ruleTypes", RuleType.values());
        model.addAttribute("ruleForm", RuleRequest.builder().enabled(true).build());
        model.addAttribute("targetsForm", new UserTargetsForm());
        model.addAttribute("percentageForm", new PercentageRolloutRequest());
        model.addAttribute("statusForm", new RuleStatusForm());
        return "rules/manage";
    }

    @PostMapping("/flags/{id}/rules")
    public String addRule(@PathVariable Long id, @ModelAttribute("ruleForm") RuleRequest request) {
        ruleManagementService.addRuleToFlag(id, request);
        return "redirect:/flags/" + id + "/rules";
    }

    @PostMapping("/rules/{ruleId}/targets")
    public String addTargets(
            @PathVariable Long ruleId,
            @ModelAttribute("targetsForm") UserTargetsForm request,
            @org.springframework.web.bind.annotation.RequestParam Long flagId) {
        ruleManagementService.addUserTargetsFromCsv(ruleId, request.getUserIds() == null ? "" : request.getUserIds());
        return "redirect:/flags/" + flagId + "/rules";
    }

    @PostMapping("/rules/{ruleId}/percentage")
    public String updatePercentage(
            @PathVariable Long ruleId,
            @ModelAttribute("percentageForm") PercentageRolloutRequest request,
            @org.springframework.web.bind.annotation.RequestParam Long flagId) {
        ruleManagementService.setPercentageRollout(ruleId, request);
        return "redirect:/flags/" + flagId + "/rules";
    }

    @PostMapping("/rules/{ruleId}/status")
    public String updateRuleStatus(
            @PathVariable Long ruleId,
            @ModelAttribute("statusForm") RuleStatusForm request,
            @org.springframework.web.bind.annotation.RequestParam Long flagId) {
        ruleManagementService.updateRuleStatus(ruleId, RuleStatusUpdateRequest.builder()
                .enabled(request.isEnabled())
                .build());
        return "redirect:/flags/" + flagId + "/rules";
    }
}
