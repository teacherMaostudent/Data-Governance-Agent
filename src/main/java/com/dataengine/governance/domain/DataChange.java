package com.dataengine.governance.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "data_change")
public class DataChange {
    @Id
    private UUID id;
    @Column(nullable = false, length = 160)
    private String title;
    @Column(nullable = false, length = 4000)
    private String description;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChangeType type;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChangeStatus status;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RiskLevel riskLevel;
    private int riskScore;
    @Column(nullable = false)
    private String owner;
    @Column(nullable = false)
    private String targetEnvironment;
    @Column(nullable = false)
    private String assetQualifiedName;
    @Column(nullable = false)
    private String assetTier;
    @Column(length = 4000)
    private String beforeSchema;
    @Column(length = 4000)
    private String afterSchema;
    private int consumerCount;
    private boolean destructive;
    private boolean recoveryPlanPresent;
    private boolean approved;
    @Enumerated(EnumType.STRING)
    private GateStatus contractGate;
    @Enumerated(EnumType.STRING)
    private GateStatus qualityGate;
    @Enumerated(EnumType.STRING)
    private GateStatus shadowGate;
    @Enumerated(EnumType.STRING)
    private GateStatus canaryGate;
    private Instant createdAt;
    private Instant updatedAt;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "change_confirmed_impact", joinColumns = @JoinColumn(name = "change_id"))
    @Column(name = "asset_name")
    private List<String> confirmedImpacts = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "change_candidate_impact", joinColumns = @JoinColumn(name = "change_id"))
    @Column(name = "asset_name")
    private List<String> candidateImpacts = new ArrayList<>();

    protected DataChange() {}

    public DataChange(String title, String description, ChangeType type, String owner,
                      String targetEnvironment, String assetQualifiedName, String assetTier,
                      String beforeSchema, String afterSchema, int consumerCount,
                      boolean destructive, boolean recoveryPlanPresent) {
        this.id = UUID.randomUUID();
        this.title = title;
        this.description = description;
        this.type = type;
        this.owner = owner;
        this.targetEnvironment = targetEnvironment;
        this.assetQualifiedName = assetQualifiedName;
        this.assetTier = assetTier;
        this.beforeSchema = beforeSchema;
        this.afterSchema = afterSchema;
        this.consumerCount = consumerCount;
        this.destructive = destructive;
        this.recoveryPlanPresent = recoveryPlanPresent;
        this.status = ChangeStatus.DRAFT;
        this.riskLevel = RiskLevel.LOW;
        this.contractGate = GateStatus.PENDING;
        this.qualityGate = GateStatus.PENDING;
        this.shadowGate = GateStatus.PENDING;
        this.canaryGate = GateStatus.PENDING;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public void transitionTo(ChangeStatus next) { this.status = next; touch(); }
    public void setRisk(int score, RiskLevel level) { this.riskScore = score; this.riskLevel = level; touch(); }
    public void setImpacts(List<String> confirmed, List<String> candidate) {
        this.confirmedImpacts.clear(); this.confirmedImpacts.addAll(confirmed);
        this.candidateImpacts.clear(); this.candidateImpacts.addAll(candidate); touch();
    }
    public void setContractGate(GateStatus value) { this.contractGate = value; touch(); }
    public void setQualityGate(GateStatus value) { this.qualityGate = value; touch(); }
    public void setShadowGate(GateStatus value) { this.shadowGate = value; touch(); }
    public void setCanaryGate(GateStatus value) { this.canaryGate = value; touch(); }
    public void approve() { this.approved = true; touch(); }
    private void touch() { this.updatedAt = Instant.now(); }

    public UUID getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public ChangeType getType() { return type; }
    public ChangeStatus getStatus() { return status; }
    public RiskLevel getRiskLevel() { return riskLevel; }
    public int getRiskScore() { return riskScore; }
    public String getOwner() { return owner; }
    public String getTargetEnvironment() { return targetEnvironment; }
    public String getAssetQualifiedName() { return assetQualifiedName; }
    public String getAssetTier() { return assetTier; }
    public String getBeforeSchema() { return beforeSchema; }
    public String getAfterSchema() { return afterSchema; }
    public int getConsumerCount() { return consumerCount; }
    public boolean isDestructive() { return destructive; }
    public boolean isRecoveryPlanPresent() { return recoveryPlanPresent; }
    public boolean isApproved() { return approved; }
    public GateStatus getContractGate() { return contractGate; }
    public GateStatus getQualityGate() { return qualityGate; }
    public GateStatus getShadowGate() { return shadowGate; }
    public GateStatus getCanaryGate() { return canaryGate; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public List<String> getConfirmedImpacts() { return List.copyOf(confirmedImpacts); }
    public List<String> getCandidateImpacts() { return List.copyOf(candidateImpacts); }
}
