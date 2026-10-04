package com.dataengine.governance.repository;
import com.dataengine.governance.domain.QualityResult; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface QualityResultRepository extends JpaRepository<QualityResult,UUID>{List<QualityResult> findByChangeIdOrderByCreatedAtDesc(UUID changeId);}
