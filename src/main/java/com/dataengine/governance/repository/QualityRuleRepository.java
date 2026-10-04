package com.dataengine.governance.repository;
import com.dataengine.governance.domain.QualityRule; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface QualityRuleRepository extends JpaRepository<QualityRule,UUID>{List<QualityRule> findByAssetQualifiedNameAndEnabledTrue(String asset);}
