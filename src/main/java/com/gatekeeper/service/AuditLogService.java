package com.gatekeeper.service;

import com.gatekeeper.dto.AuditLogResponse;
import com.gatekeeper.model.AuditLog;
import com.gatekeeper.repository.AuditLogRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private static final String DEFAULT_ACTOR = "system";

    private final AuditLogRepository auditLogRepository;

    @Transactional
    public void log(String entityType, Long entityId, String action, String details) {
        auditLogRepository.save(AuditLog.builder()
                .entityType(entityType)
                .entityId(entityId)
                .action(action)
                .actor(DEFAULT_ACTOR)
                .details(details)
                .build());
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> getAuditLogs() {
        return auditLogRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> getAuditLogs(String entityType, Long entityId) {
        return auditLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType, entityId).stream()
                .map(this::toResponse)
                .toList();
    }

    private AuditLogResponse toResponse(AuditLog auditLog) {
        return AuditLogResponse.builder()
                .id(auditLog.getId())
                .entityType(auditLog.getEntityType())
                .entityId(auditLog.getEntityId())
                .action(auditLog.getAction())
                .actor(auditLog.getActor())
                .details(auditLog.getDetails())
                .createdAt(auditLog.getCreatedAt())
                .build();
    }
}
