package com.gatekeeper.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gatekeeper.config.CacheConfig;
import com.gatekeeper.model.Environment;
import com.gatekeeper.model.GatekeeperFlag;
import com.gatekeeper.model.FlagRule;
import com.gatekeeper.model.RuleType;
import com.gatekeeper.repository.EnvironmentRepository;
import com.gatekeeper.repository.GatekeeperFlagRepository;
import com.gatekeeper.repository.FlagRuleRepository;
import com.gatekeeper.repository.UserTargetRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig
@Import({GatekeeperEvaluationService.class, CacheConfig.class})
class GatekeeperEvaluationCachingTest {

    @MockBean
    private GatekeeperFlagRepository gatekeeperFlagRepository;

    @MockBean
    private EnvironmentRepository environmentRepository;

    @MockBean
    private FlagRuleRepository flagRuleRepository;

    @MockBean
    private UserTargetRepository userTargetRepository;

    @Autowired
    private GatekeeperEvaluationService gatekeeperEvaluationService;

    @Autowired
    private CacheManager cacheManager;

    @Test
    void cachesEvaluationResultForRepeatedRequests() {
        GatekeeperFlag flag = GatekeeperFlag.builder()
                .id(1L)
                .key("checkout")
                .name("Checkout")
                .enabled(true)
                .build();
        Environment environment = Environment.builder()
                .id(1L)
                .name("prod")
                .build();
        FlagRule globalRule = FlagRule.builder()
                .id(1L)
                .flag(flag)
                .environment(environment)
                .ruleType(RuleType.GLOBAL)
                .enabled(true)
                .build();

        when(gatekeeperFlagRepository.findByKey("checkout")).thenReturn(Optional.of(flag));
        when(environmentRepository.findByName("prod")).thenReturn(Optional.of(environment));
        when(flagRuleRepository.findByFlagAndEnvironment(flag, environment)).thenReturn(List.of(globalRule));

        boolean firstResult = gatekeeperEvaluationService.evaluate("checkout", "alice", "prod");
        boolean secondResult = gatekeeperEvaluationService.evaluate("checkout", "alice", "prod");

        assertThat(firstResult).isTrue();
        assertThat(secondResult).isTrue();
        assertThat(cacheManager.getCache(CacheConfig.EVALUATION_CACHE)).isNotNull();
        verify(gatekeeperFlagRepository, times(1)).findByKey("checkout");
        verify(environmentRepository, times(1)).findByName("prod");
        verify(flagRuleRepository, times(1)).findByFlagAndEnvironment(flag, environment);
    }
}
