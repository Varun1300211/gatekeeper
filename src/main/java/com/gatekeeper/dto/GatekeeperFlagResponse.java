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
public class GatekeeperFlagResponse {

    private Long id;
    private String key;
    private String name;
    private String description;
    private boolean enabled;
    private boolean killSwitchEnabled;
    private boolean archived;
    private Long version;
    private Integer rolloutPercentage;
    private String targetedUsers;
    private String targetedSegments;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
