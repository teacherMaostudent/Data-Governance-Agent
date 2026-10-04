package com.dataengine.governance.repository;

import com.dataengine.governance.domain.DataChange;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface DataChangeRepository extends JpaRepository<DataChange, UUID> {}
