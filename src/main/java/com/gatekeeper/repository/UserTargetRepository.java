package com.gatekeeper.repository;

import com.gatekeeper.model.FlagRule;
import com.gatekeeper.model.UserTarget;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserTargetRepository extends JpaRepository<UserTarget, Long> {

    List<UserTarget> findByFlagRule(FlagRule flagRule);

    List<UserTarget> findByUserId(String userId);
}
