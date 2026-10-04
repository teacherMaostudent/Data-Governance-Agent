package com.dataengine.governance.repository;
import com.dataengine.governance.domain.DataAsset; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface DataAssetRepository extends JpaRepository<DataAsset,UUID>{ Optional<DataAsset> findByQualifiedName(String qualifiedName); }
