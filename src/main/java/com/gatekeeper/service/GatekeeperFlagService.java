package com.gatekeeper.service;

import com.gatekeeper.dto.GatekeeperFlagRequest;
import com.gatekeeper.dto.GatekeeperFlagResponse;
import com.gatekeeper.dto.FlagEvaluationRequest;
import com.gatekeeper.dto.FlagEvaluationResponse;
import com.gatekeeper.evaluation.FlagEvaluationEngine;
import com.gatekeeper.model.GatekeeperFlag;
import com.gatekeeper.repository.GatekeeperFlagRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static com.gatekeeper.config.CacheConfig.EVALUATION_CACHE;

@Service
@RequiredArgsConstructor
public class GatekeeperFlagService {

    private final GatekeeperFlagRepository gatekeeperFlagRepository;
    private final FlagEvaluationEngine flagEvaluationEngine;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public List<GatekeeperFlagResponse> getAllFlags() {
        return gatekeeperFlagRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public GatekeeperFlagResponse getFlag(Long id) {
        return gatekeeperFlagRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("Gatekeeper flag not found: " + id));
    }

    @Transactional
    @CacheEvict(cacheNames = EVALUATION_CACHE, allEntries = true)
    public GatekeeperFlagResponse createFlag(GatekeeperFlagRequest request) {
        GatekeeperFlag gatekeeperFlag = GatekeeperFlag.builder()
                .key(request.getKey())
                .name(request.getName())
                .description(request.getDescription())
                .enabled(request.isEnabled())
                .build();

        GatekeeperFlag savedFlag = gatekeeperFlagRepository.save(gatekeeperFlag);
        auditLogService.log(
                "GATEKEEPER_FLAG",
                savedFlag.getId(),
                "CREATED",
                "Created GateKeeper flag '" + savedFlag.getKey() + "' with enabled=" + savedFlag.isEnabled());
        return toResponse(savedFlag);
    }

    @Transactional
    @CacheEvict(cacheNames = EVALUATION_CACHE, allEntries = true)
    public GatekeeperFlagResponse updateFlag(Long id, GatekeeperFlagRequest request) {
        GatekeeperFlag gatekeeperFlag = gatekeeperFlagRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Gatekeeper flag not found: " + id));

        gatekeeperFlag.setKey(request.getKey());
        gatekeeperFlag.setName(request.getName());
        gatekeeperFlag.setDescription(request.getDescription());
        gatekeeperFlag.setEnabled(request.isEnabled());

        GatekeeperFlag savedFlag = gatekeeperFlagRepository.save(gatekeeperFlag);
        auditLogService.log(
                "GATEKEEPER_FLAG",
                savedFlag.getId(),
                "UPDATED",
                "Updated GateKeeper flag '" + savedFlag.getKey() + "' with enabled=" + savedFlag.isEnabled());
        return toResponse(savedFlag);
    }

    @Transactional
    @CacheEvict(cacheNames = EVALUATION_CACHE, allEntries = true)
    public void deleteFlag(Long id) {
        GatekeeperFlag gatekeeperFlag = gatekeeperFlagRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Gatekeeper flag not found: " + id));
        gatekeeperFlagRepository.deleteById(id);
        auditLogService.log(
                "GATEKEEPER_FLAG",
                id,
                "DELETED",
                "Deleted GateKeeper flag '" + gatekeeperFlag.getKey() + "'");
    }

    @Transactional(readOnly = true)
    public FlagEvaluationResponse evaluateFlag(FlagEvaluationRequest request) {
        GatekeeperFlag gatekeeperFlag = gatekeeperFlagRepository.findByKey(request.getFeatureKey())
                .orElseThrow(() -> new EntityNotFoundException("Gatekeeper flag not found: " + request.getFeatureKey()));

        return flagEvaluationEngine.evaluate(gatekeeperFlag, request);
    }

    private GatekeeperFlagResponse toResponse(GatekeeperFlag gatekeeperFlag) {
        return GatekeeperFlagResponse.builder()
                .id(gatekeeperFlag.getId())
                .key(gatekeeperFlag.getKey())
                .name(gatekeeperFlag.getName())
                .description(gatekeeperFlag.getDescription())
                .enabled(gatekeeperFlag.isEnabled())
                .rolloutPercentage(0)
                .targetedUsers(null)
                .targetedSegments(null)
                .createdAt(gatekeeperFlag.getCreatedAt())
                .updatedAt(gatekeeperFlag.getUpdatedAt())
                .build();
    }
}
