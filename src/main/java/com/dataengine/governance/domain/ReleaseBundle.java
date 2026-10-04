package com.dataengine.governance.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "release_bundle", uniqueConstraints = @UniqueConstraint(columnNames = "changeId"))
public class ReleaseBundle {
    @Id private UUID id;
    @Column(nullable = false) private UUID changeId;
    @Column(nullable = false, unique = true, length = 64) private String digest;
    @Column(nullable = false, length = 8000) private String manifest;
    @Column(nullable = false) private String createdBy;
    @Column(nullable = false) private Instant createdAt;

    protected ReleaseBundle() {}
    public ReleaseBundle(UUID changeId, String digest, String manifest, String createdBy) {
        this.id = UUID.randomUUID(); this.changeId = changeId; this.digest = digest;
        this.manifest = manifest; this.createdBy = createdBy; this.createdAt = Instant.now();
    }
    public UUID getId() { return id; }
    public UUID getChangeId() { return changeId; }
    public String getDigest() { return digest; }
    public String getManifest() { return manifest; }
    public String getCreatedBy() { return createdBy; }
    public Instant getCreatedAt() { return createdAt; }
}
