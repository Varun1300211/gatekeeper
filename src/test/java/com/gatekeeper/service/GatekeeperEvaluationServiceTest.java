package com.gatekeeper.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.gatekeeper.model.Environment;
import com.gatekeeper.model.GatekeeperFlag;
import com.gatekeeper.model.FlagRule;
import com.gatekeeper.model.RuleType;
import com.gatekeeper.model.UserTarget;
import com.gatekeeper.repository.EnvironmentRepository;
import com.gatekeeper.repository.GatekeeperFlagRepository;
import com.gatekeeper.repository.FlagRuleRepository;
import com.gatekeeper.repository.UserTargetRepository;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GatekeeperEvaluationServiceTest {

    @Mock
    private GatekeeperFlagRepository gatekeeperFlagRepository;

    @Mock
    private EnvironmentRepository environmentRepository;

    @Mock
    private FlagRuleRepository flagRuleRepository;

    @Mock
    private UserTargetRepository userTargetRepository;

    @InjectMocks
    private GatekeeperEvaluationService gatekeeperEvaluationService;

    @Test
    void returnsFalseWhenFlagIsDisabled() {
        GatekeeperFlag flag = GatekeeperFlag.builder()
                .id(1L)
                .key("checkout")
                .name("Checkout")
                .enabled(false)
                .build();

        when(gatekeeperFlagRepository.findByKey("checkout")).thenReturn(Optional.of(flag));

        boolean result = gatekeeperEvaluationService.evaluate("checkout", "alice", "prod");

        assertThat(result).isFalse();
    }

    @Test
    void returnsTrueWhenGlobalRuleIsEnabled() {
        GatekeeperFlag flag = enabledFlag();
        Environment environment = environment("prod");
        FlagRule globalRule = rule(flag, environment, RuleType.GLOBAL, null, true);

        when(gatekeeperFlagRepository.findByKey("checkout")).thenReturn(Optional.of(flag));
        when(environmentRepository.findByName("prod")).thenReturn(Optional.of(environment));
        when(flagRuleRepository.findByFlagAndEnvironment(flag, environment)).thenReturn(List.of(globalRule));

        boolean result = gatekeeperEvaluationService.evaluate("checkout", "alice", "prod");

        assertThat(result).isTrue();
    }

    @Test
    void returnsTrueWhenUserMatchesTargetedRule() {
        GatekeeperFlag flag = enabledFlag();
        Environment environment = environment("prod");
        FlagRule userTargetRule = rule(flag, environment, RuleType.USER_TARGET, null, true);

        when(gatekeeperFlagRepository.findByKey("checkout")).thenReturn(Optional.of(flag));
        when(environmentRepository.findByName("prod")).thenReturn(Optional.of(environment));
        when(flagRuleRepository.findByFlagAndEnvironment(flag, environment)).thenReturn(List.of(userTargetRule));
        when(userTargetRepository.findByFlagRule(userTargetRule)).thenReturn(List.of(
                UserTarget.builder().id(1L).flagRule(userTargetRule).userId("alice").build()
        ));

        boolean result = gatekeeperEvaluationService.evaluate("checkout", "alice", "prod");

        assertThat(result).isTrue();
    }

    @Test
    void percentageRolloutUsesDeterministicBucket() {
        GatekeeperFlag flag = enabledFlag();
        Environment environment = environment("prod");
        FlagRule percentageRule = rule(flag, environment, RuleType.PERCENTAGE, 35, true);
        String flagKey = "checkout";
        String userId = "alice";
        String environmentName = "prod";

        when(gatekeeperFlagRepository.findByKey(flagKey)).thenReturn(Optional.of(flag));
        when(environmentRepository.findByName(environmentName)).thenReturn(Optional.of(environment));
        when(flagRuleRepository.findByFlagAndEnvironment(flag, environment)).thenReturn(List.of(percentageRule));

        boolean result = gatekeeperEvaluationService.evaluate(flagKey, userId, environmentName);

        assertThat(result).isEqualTo(bucket(flagKey, userId, environmentName) < 35);
    }

    @Test
    void sameUserAlwaysGetsSameResult() {
        GatekeeperFlag flag = enabledFlag();
        Environment environment = environment("prod");
        FlagRule percentageRule = rule(flag, environment, RuleType.PERCENTAGE, 50, true);
        String flagKey = "checkout";
        String userId = "consistent-user";
        String environmentName = "prod";

        when(gatekeeperFlagRepository.findByKey(flagKey)).thenReturn(Optional.of(flag));
        when(environmentRepository.findByName(environmentName)).thenReturn(Optional.of(environment));
        when(flagRuleRepository.findByFlagAndEnvironment(flag, environment)).thenReturn(List.of(percentageRule));

        boolean firstResult = gatekeeperEvaluationService.evaluate(flagKey, userId, environmentName);
        boolean secondResult = gatekeeperEvaluationService.evaluate(flagKey, userId, environmentName);

        assertThat(firstResult).isEqualTo(secondResult);
    }

    private GatekeeperFlag enabledFlag() {
        return GatekeeperFlag.builder()
                .id(1L)
                .key("checkout")
                .name("Checkout")
                .enabled(true)
                .build();
    }

    private Environment environment(String name) {
        return Environment.builder()
                .id(1L)
                .name(name)
                .build();
    }

    private FlagRule rule(
            GatekeeperFlag flag,
            Environment environment,
            RuleType ruleType,
            Integer percentage,
            boolean enabled) {
        return FlagRule.builder()
                .id(1L)
                .flag(flag)
                .environment(environment)
                .ruleType(ruleType)
                .percentage(percentage)
                .enabled(enabled)
                .build();
    }

    private int bucket(String flagKey, String userId, String environmentName) {
        String input = flagKey + ":" + userId + ":" + environmentName;
        int hash = java.util.Arrays.hashCode(input.getBytes(StandardCharsets.UTF_8));
        return Math.floorMod(hash, 100);
    }
}
