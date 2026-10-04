package com.dataengine.governance.repository;
import com.dataengine.governance.domain.DataContract; import com.dataengine.governance.domain.ContractStatus; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface DataContractRepository extends JpaRepository<DataContract,UUID>{ Optional<DataContract> findByAssetQualifiedNameAndStatus(String assetQualifiedName, ContractStatus status); List<DataContract> findByAssetQualifiedNameOrderByUpdatedAtDesc(String assetQualifiedName); }
