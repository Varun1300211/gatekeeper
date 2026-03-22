package com.gatekeeper.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
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
public class GatekeeperFlagRequest {

    @NotBlank
    private String key;

    @NotBlank
    private String name;

    private String description;

    private boolean enabled;

    @Min(0)
    @Max(100)
    private Integer rolloutPercentage;

    private String targetedUsers;

    private String targetedSegments;
}
