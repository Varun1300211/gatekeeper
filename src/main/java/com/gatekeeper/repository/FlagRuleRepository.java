package com.gatekeeper.repository;

import com.gatekeeper.model.Environment;
import com.gatekeeper.model.GatekeeperFlag;
import com.gatekeeper.model.FlagRule;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FlagRuleRepository extends JpaRepository<FlagRule, Long> {

    List<FlagRule> findByFlag(GatekeeperFlag flag);

    List<FlagRule> findByEnvironment(Environment environment);

    List<FlagRule> findByFlagAndEnvironment(GatekeeperFlag flag, Environment environment);
}
