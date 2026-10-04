package com.dataengine.governance.repository;

import com.dataengine.governance.domain.Evidence;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface EvidenceRepository extends JpaRepository<Evidence, UUID> {
    List<Evidence> findByChangeIdOrderByEventTimeDesc(UUID changeId);
}
