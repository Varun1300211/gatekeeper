package com.gatekeeper.controller;

import com.gatekeeper.dto.GatekeeperFlagRequest;
import com.gatekeeper.dto.PercentageRolloutRequest;
import com.gatekeeper.dto.RuleRequest;
import com.gatekeeper.dto.RuleStatusForm;
import com.gatekeeper.dto.RuleStatusUpdateRequest;
import com.gatekeeper.dto.UserTargetsForm;
import com.gatekeeper.model.RuleType;
import com.gatekeeper.service.GatekeeperFlagService;
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

    private final GatekeeperFlagService gatekeeperFlagService;
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
