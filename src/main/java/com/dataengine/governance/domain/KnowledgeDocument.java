package com.dataengine.governance.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "knowledge_document")
public class KnowledgeDocument {
    @Id private UUID id;
    @Column(nullable = false) private String title;
    @Column(nullable = false, length = 6000) private String content;
    @Column(nullable = false) private String tags;
    @Column(nullable = false) private String authority;
    @Column(nullable = false) private String version;
    @Column(nullable = false) private Instant indexedAt;

    protected KnowledgeDocument() {}
    public KnowledgeDocument(String title, String content, String tags, String authority, String version) {
        this.id = UUID.randomUUID(); this.title = title; this.content = content; this.tags = tags;
        this.authority = authority; this.version = version; this.indexedAt = Instant.now();
    }
    public UUID getId() { return id; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getTags() { return tags; }
    public String getAuthority() { return authority; }
    public String getVersion() { return version; }
    public Instant getIndexedAt() { return indexedAt; }
}
