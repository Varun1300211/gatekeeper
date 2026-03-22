package com.gatekeeper.repository;

import com.gatekeeper.model.GatekeeperFlag;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GatekeeperFlagRepository extends JpaRepository<GatekeeperFlag, Long> {

    Optional<GatekeeperFlag> findByKey(String key);
}
