package com.dataengine.governance.repository;

import com.dataengine.governance.domain.ReleaseBundle;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface ReleaseBundleRepository extends JpaRepository<ReleaseBundle, UUID> {
    Optional<ReleaseBundle> findByChangeId(UUID changeId);
}
