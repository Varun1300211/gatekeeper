package com.gatekeeper.service;

import com.gatekeeper.dto.GatekeeperFlagRequest;
import com.gatekeeper.dto.GatekeeperFlagResponse;
import com.gatekeeper.dto.FlagEvaluationRequest;
import com.gatekeeper.dto.FlagEvaluationResponse;
import com.gatekeeper.evaluation.FlagEvaluationEngine;
import com.gatekeeper.messaging.GatekeeperConfigChangedEvent;
import com.gatekeeper.model.GatekeeperFlag;
import com.gatekeeper.repository.GatekeeperFlagRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.OptimisticLockException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static com.gatekeeper.config.CacheConfig.EVALUATION_CACHE;

@Service
@RequiredArgsConstructor
public class GatekeeperFlagService {

    private final GatekeeperFlagRepository gatekeeperFlagRepository;
    private final FlagEvaluationEngine flagEvaluationEngine;
    private final AuditLogService auditLogService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public List<GatekeeperFlagResponse> getAllFlags() {
        return gatekeeperFlagRepository.findAllByArchivedFalse().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public GatekeeperFlagResponse getFlag(Long id) {
        return gatekeeperFlagRepository.findByIdAndArchivedFalse(id)
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
                .killSwitchEnabled(request.isKillSwitchEnabled())
                .build();

        GatekeeperFlag savedFlag = gatekeeperFlagRepository.save(gatekeeperFlag);
        auditLogService.log(
                "GATEKEEPER_FLAG",
                savedFlag.getId(),
                "CREATED",
                "Created GateKeeper flag '" + savedFlag.getKey() + "' with enabled=" + savedFlag.isEnabled()
                        + " killSwitchEnabled=" + savedFlag.isKillSwitchEnabled());
        publishConfigChanged(savedFlag.getKey(), null, savedFlag.getId(), "FLAG_CREATED");
        return toResponse(savedFlag);
    }

    @Transactional
    @CacheEvict(cacheNames = EVALUATION_CACHE, allEntries = true)
    public GatekeeperFlagResponse updateFlag(Long id, GatekeeperFlagRequest request) {
        GatekeeperFlag gatekeeperFlag = gatekeeperFlagRepository.findByIdAndArchivedFalse(id)
                .orElseThrow(() -> new EntityNotFoundException("Gatekeeper flag not found: " + id));

        if (request.getVersion() != null && !request.getVersion().equals(gatekeeperFlag.getVersion())) {
            throw new OptimisticLockException("Gatekeeper flag was updated by another request");
        }

        String previousFlagKey = gatekeeperFlag.getKey();
        gatekeeperFlag.setKey(request.getKey());
        gatekeeperFlag.setName(request.getName());
        gatekeeperFlag.setDescription(request.getDescription());
        gatekeeperFlag.setEnabled(request.isEnabled());
        gatekeeperFlag.setKillSwitchEnabled(request.isKillSwitchEnabled());

        GatekeeperFlag savedFlag = gatekeeperFlagRepository.save(gatekeeperFlag);
        auditLogService.log(
                "GATEKEEPER_FLAG",
                savedFlag.getId(),
                "UPDATED",
                "Updated GateKeeper flag '" + savedFlag.getKey() + "' with enabled=" + savedFlag.isEnabled()
                        + " killSwitchEnabled=" + savedFlag.isKillSwitchEnabled());
        publishConfigChanged(savedFlag.getKey(), previousFlagKey, savedFlag.getId(), "FLAG_UPDATED");
        return toResponse(savedFlag);
    }

    @Transactional
    @CacheEvict(cacheNames = EVALUATION_CACHE, allEntries = true)
    public void deleteFlag(Long id) {
        GatekeeperFlag gatekeeperFlag = gatekeeperFlagRepository.findByIdAndArchivedFalse(id)
                .orElseThrow(() -> new EntityNotFoundException("Gatekeeper flag not found: " + id));
        gatekeeperFlag.setArchived(true);
        gatekeeperFlagRepository.save(gatekeeperFlag);
        auditLogService.log(
                "GATEKEEPER_FLAG",
                id,
                "ARCHIVED",
                "Archived GateKeeper flag '" + gatekeeperFlag.getKey() + "'");
        publishConfigChanged(gatekeeperFlag.getKey(), null, gatekeeperFlag.getId(), "FLAG_ARCHIVED");
    }

    @Transactional(readOnly = true)
    public FlagEvaluationResponse evaluateFlag(FlagEvaluationRequest request) {
        GatekeeperFlag gatekeeperFlag = gatekeeperFlagRepository.findByKeyAndArchivedFalse(request.getFeatureKey())
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
                .killSwitchEnabled(gatekeeperFlag.isKillSwitchEnabled())
                .archived(gatekeeperFlag.isArchived())
                .version(gatekeeperFlag.getVersion())
                .rolloutPercentage(0)
                .targetedUsers(null)
                .targetedSegments(null)
                .createdAt(gatekeeperFlag.getCreatedAt())
                .updatedAt(gatekeeperFlag.getUpdatedAt())
                .build();
    }

    private void publishConfigChanged(String flagKey, String previousFlagKey, Long entityId, String action) {
        eventPublisher.publishEvent(GatekeeperConfigChangedEvent.of(
                flagKey,
                previousFlagKey,
                "GATEKEEPER_FLAG",
                entityId,
                action));
    }
}
