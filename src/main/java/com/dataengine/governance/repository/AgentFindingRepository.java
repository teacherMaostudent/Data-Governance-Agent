package com.dataengine.governance.repository;
import com.dataengine.governance.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface AgentFindingRepository extends JpaRepository<AgentFinding, UUID> {
    List<AgentFinding> findAllByOrderByCreatedAtDesc();
    List<AgentFinding> findByChangeIdOrderByCreatedAtDesc(UUID changeId);
    long countByStatus(FindingStatus status);
}
