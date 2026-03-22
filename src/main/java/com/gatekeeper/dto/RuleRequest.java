package com.gatekeeper.dto;

import com.gatekeeper.model.RuleType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class RuleRequest {

    @NotBlank
    private String environment;

    @NotNull
    private RuleType ruleType;

    @Min(0)
    @Max(100)
    private Integer percentage;

    @Builder.Default
    private boolean enabled = true;
}
