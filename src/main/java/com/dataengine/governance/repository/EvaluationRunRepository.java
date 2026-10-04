package com.dataengine.governance.repository;
import com.dataengine.governance.domain.EvaluationRun;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface EvaluationRunRepository extends JpaRepository<EvaluationRun, UUID> {
    List<EvaluationRun> findTop20ByOrderByRanAtDesc();
}
