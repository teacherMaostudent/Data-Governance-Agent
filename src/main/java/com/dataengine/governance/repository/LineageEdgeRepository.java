package com.dataengine.governance.repository;
import com.dataengine.governance.domain.*; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface LineageEdgeRepository extends JpaRepository<LineageEdge,UUID>{ List<LineageEdge> findBySourceAsset(String sourceAsset); }
