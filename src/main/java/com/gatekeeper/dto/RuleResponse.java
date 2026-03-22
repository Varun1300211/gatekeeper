package com.gatekeeper.dto;

import com.gatekeeper.model.RuleType;
import java.util.List;
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
public class RuleResponse {

    private Long id;
    private Long flagId;
    private String flagKey;
    private String environment;
    private RuleType ruleType;
    private Integer percentage;
    private boolean enabled;
    private List<String> userTargets;
}
