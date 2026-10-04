package com.dataengine.governance.repository;
import com.dataengine.governance.domain.EvaluationCase;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
public interface EvaluationCaseRepository extends JpaRepository<EvaluationCase, UUID> { boolean existsByName(String name); }
