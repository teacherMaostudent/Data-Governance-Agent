package com.dataengine.governance.repository;
import com.dataengine.governance.domain.*; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface ApprovalTaskRepository extends JpaRepository<ApprovalTask,UUID>{ List<ApprovalTask> findByChangeIdOrderByDueAt(UUID changeId); List<ApprovalTask> findByChangeIdAndStatus(UUID changeId, ApprovalStatus status); Optional<ApprovalTask> findByIdAndChangeId(UUID id, UUID changeId); }
