package com.gatekeeper.controller;

import com.gatekeeper.dto.GatekeeperFlagRequest;
import com.gatekeeper.dto.GatekeeperFlagResponse;
import com.gatekeeper.service.GatekeeperFlagService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/flags")
public class GatekeeperManagementController {

    private final GatekeeperFlagService gatekeeperFlagService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GatekeeperFlagResponse createFlag(@Valid @RequestBody GatekeeperFlagRequest request) {
        return gatekeeperFlagService.createFlag(request);
    }

    @GetMapping
    public List<GatekeeperFlagResponse> listFlags() {
        return gatekeeperFlagService.getAllFlags();
    }

    @PutMapping("/{id}")
    public GatekeeperFlagResponse updateFlag(@PathVariable Long id, @Valid @RequestBody GatekeeperFlagRequest request) {
        return gatekeeperFlagService.updateFlag(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFlag(@PathVariable Long id) {
        gatekeeperFlagService.deleteFlag(id);
    }
}
