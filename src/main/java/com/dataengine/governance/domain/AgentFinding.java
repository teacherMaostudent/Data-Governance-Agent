package com.dataengine.governance.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "agent_finding")
public class AgentFinding {
    @Id private UUID id;
    @Column(nullable = false) private UUID changeId;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private SkillType skillType;
    @Column(nullable = false, length = 2000) private String claim;
    @Column(nullable = false) private double confidence;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private FindingStatus status;
    @Column(nullable = false) private String provider;
    @Column(nullable = false) private String modelVersion;
    @Column(nullable = false) private Instant createdAt;
    private Instant verifiedAt;
    private String verifiedBy;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "finding_evidence_ref", joinColumns = @JoinColumn(name = "finding_id"))
    @Column(name = "evidence_ref")
    private List<String> evidenceRefs = new ArrayList<>();

    protected AgentFinding() {}
    public AgentFinding(UUID changeId, SkillType skillType, String claim, double confidence,
                        String provider, String modelVersion, List<String> evidenceRefs) {
        this.id = UUID.randomUUID(); this.changeId = changeId; this.skillType = skillType;
        this.claim = claim; this.confidence = confidence; this.provider = provider;
        this.modelVersion = modelVersion; this.evidenceRefs.addAll(evidenceRefs);
        this.status = FindingStatus.CANDIDATE; this.createdAt = Instant.now();
    }
    public void verify(String actor) { this.status = FindingStatus.VERIFIED; this.verifiedBy = actor; this.verifiedAt = Instant.now(); }
    public void reject(String actor) { this.status = FindingStatus.REJECTED; this.verifiedBy = actor; this.verifiedAt = Instant.now(); }
    public UUID getId() { return id; }
    public UUID getChangeId() { return changeId; }
    public SkillType getSkillType() { return skillType; }
    public String getClaim() { return claim; }
    public double getConfidence() { return confidence; }
    public FindingStatus getStatus() { return status; }
    public String getProvider() { return provider; }
    public String getModelVersion() { return modelVersion; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getVerifiedAt() { return verifiedAt; }
    public String getVerifiedBy() { return verifiedBy; }
    public List<String> getEvidenceRefs() { return List.copyOf(evidenceRefs); }
}
