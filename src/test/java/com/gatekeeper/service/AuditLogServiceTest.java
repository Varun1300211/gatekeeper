package com.gatekeeper.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gatekeeper.model.AuditLog;
import com.gatekeeper.repository.AuditLogRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditLogService auditLogService;

    @Test
    void returnsAuditLogsInResponseFormat() {
        AuditLog auditLog = AuditLog.builder()
                .id(1L)
                .entityType("GATEKEEPER_FLAG")
                .entityId(10L)
                .action("CREATED")
                .actor("system")
                .details("Created GateKeeper flag 'checkout'")
                .createdAt(LocalDateTime.of(2026, 3, 22, 1, 0))
                .build();

        when(auditLogRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(auditLog));

        var logs = auditLogService.getAuditLogs();

        assertThat(logs).hasSize(1);
        assertThat(logs.get(0).getEntityType()).isEqualTo("GATEKEEPER_FLAG");
        assertThat(logs.get(0).getAction()).isEqualTo("CREATED");
    }

    @Test
    void savesAuditLogWithDefaultActor() {
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        auditLogService.log("FLAG_RULE", 5L, "STATUS_UPDATED", "Set rule 5 enabled=true");

        ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(auditLogCaptor.capture());
        assertThat(auditLogCaptor.getValue().getActor()).isEqualTo("system");
    }
}
