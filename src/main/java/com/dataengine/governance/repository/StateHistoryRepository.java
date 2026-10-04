package com.dataengine.governance.repository;

import com.dataengine.governance.domain.StateHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface StateHistoryRepository extends JpaRepository<StateHistory, UUID> {
    List<StateHistory> findByChangeIdOrderByOccurredAtDesc(UUID changeId);
}
