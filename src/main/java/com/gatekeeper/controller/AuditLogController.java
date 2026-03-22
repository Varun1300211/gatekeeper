package com.gatekeeper.controller;

import com.gatekeeper.dto.AuditLogResponse;
import com.gatekeeper.service.AuditLogService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/audit-logs")
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    public List<AuditLogResponse> getAuditLogs(
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) Long entityId) {
        if (entityType != null && entityId != null) {
            return auditLogService.getAuditLogs(entityType, entityId);
        }
        return auditLogService.getAuditLogs();
    }
}
