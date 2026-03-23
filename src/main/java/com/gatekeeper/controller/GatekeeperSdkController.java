package com.gatekeeper.controller;

import com.gatekeeper.dto.SdkEvaluationResponse;
import com.gatekeeper.dto.SdkStatusResponse;
import com.gatekeeper.dto.SdkTargetForm;
import com.gatekeeper.dto.SdkTargetResponse;
import com.gatekeeper.service.GatekeeperSdkService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sdk")
public class GatekeeperSdkController {

    private final GatekeeperSdkService gatekeeperSdkService;

    @GetMapping("/status")
    public SdkStatusResponse status() {
        return gatekeeperSdkService.getStatus();
    }

    @GetMapping("/available-flags")
    public List<String> getAvailableFlags() {
        return gatekeeperSdkService.getAvailableFlagKeys();
    }

    @GetMapping("/evaluate")
    public SdkEvaluationResponse evaluate(
            @RequestParam(required = false) String baseUrl,
            @RequestParam String flagKey,
            @RequestParam String userId,
            @RequestParam String environment) {
        return gatekeeperSdkService.evaluateWithSdk(baseUrl, flagKey, userId, environment);
    }

    @PostMapping("/refresh-configured")
    public List<SdkEvaluationResponse> refreshConfiguredTargets() {
        return gatekeeperSdkService.refreshConfiguredTargets();
    }

    @PostMapping("/targets")
    public SdkTargetResponse addConfiguredTarget(@org.springframework.web.bind.annotation.RequestBody SdkTargetForm form) {
        return gatekeeperSdkService.addConfiguredTarget(form);
    }

    @PostMapping("/targets/{id}/delete")
    public SdkStatusResponse removeConfiguredTarget(@org.springframework.web.bind.annotation.PathVariable String id) {
        gatekeeperSdkService.removeConfiguredTarget(id);
        return gatekeeperSdkService.getStatus();
    }

    @PostMapping("/cache/clear")
    public SdkStatusResponse clearLocalCache() {
        gatekeeperSdkService.clearLocalCache();
        return gatekeeperSdkService.getStatus();
    }
}
