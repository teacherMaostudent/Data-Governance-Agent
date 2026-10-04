package com.dataengine.governance.repository;
import com.dataengine.governance.domain.GovernanceNotification; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface GovernanceNotificationRepository extends JpaRepository<GovernanceNotification,UUID>{List<GovernanceNotification> findByRecipientOrderByCreatedAtDesc(String recipient);}
