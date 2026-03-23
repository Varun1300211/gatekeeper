package com.gatekeeper.repository;

import com.gatekeeper.model.GatekeeperFlag;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GatekeeperFlagRepository extends JpaRepository<GatekeeperFlag, Long> {

    List<GatekeeperFlag> findAllByArchivedFalse();

    Optional<GatekeeperFlag> findByIdAndArchivedFalse(Long id);

    Optional<GatekeeperFlag> findByKeyAndArchivedFalse(String key);
}
