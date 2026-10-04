package com.dataengine.governance.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "evidence")
public class Evidence {
    @Id
    private UUID id;
    @Column(nullable = false)
    private UUID changeId;
    @Column(nullable = false)
    private String sourceType;
    @Column(nullable = false)
    private String sourceRef;
    @Column(nullable = false, length = 1000)
    private String summary;
    @Column(nullable = false)
    private String authorityLevel;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VerificationStatus verificationStatus;
    @Column(nullable = false)
    private Instant eventTime;

    protected Evidence() {}

    public Evidence(UUID changeId, String sourceType, String sourceRef, String summary,
                    String authorityLevel, VerificationStatus verificationStatus) {
        this.id = UUID.randomUUID();
        this.changeId = changeId;
        this.sourceType = sourceType;
        this.sourceRef = sourceRef;
        this.summary = summary;
        this.authorityLevel = authorityLevel;
        this.verificationStatus = verificationStatus;
        this.eventTime = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getChangeId() { return changeId; }
    public String getSourceType() { return sourceType; }
    public String getSourceRef() { return sourceRef; }
    public String getSummary() { return summary; }
    public String getAuthorityLevel() { return authorityLevel; }
    public VerificationStatus getVerificationStatus() { return verificationStatus; }
    public Instant getEventTime() { return eventTime; }
}
