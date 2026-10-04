package com.dataengine.governance.repository;
import com.dataengine.governance.domain.KnowledgeDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
public interface KnowledgeDocumentRepository extends JpaRepository<KnowledgeDocument, UUID> {}
