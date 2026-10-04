package com.dataengine.governance.repository;

import com.dataengine.governance.domain.ExecutionReceipt;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface ExecutionReceiptRepository extends JpaRepository<ExecutionReceipt, UUID> {
    Optional<ExecutionReceipt> findByIdempotencyKey(String idempotencyKey);
    List<ExecutionReceipt> findByChangeIdOrderByStartedAtDesc(UUID changeId);
    long countByChangeIdAndAction(UUID changeId, String action);
}
