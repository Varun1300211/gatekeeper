package com.gatekeeper.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponse {

    private Long id;
    private String entityType;
    private Long entityId;
    private String action;
    private String actor;
    private String details;
    private LocalDateTime createdAt;
}
