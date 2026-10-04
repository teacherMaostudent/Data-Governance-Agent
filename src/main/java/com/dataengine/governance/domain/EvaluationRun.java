package com.dataengine.governance.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "evaluation_run")
public class EvaluationRun {
    @Id private UUID id;
    @Column(nullable = false) private UUID caseId;
    @Column(nullable = false) private boolean passed;
    @Column(nullable = false) private double score;
    @Column(nullable = false, length = 2000) private String output;
    @Column(nullable = false) private String provider;
    @Column(nullable = false) private Instant ranAt;

    protected EvaluationRun() {}
    public EvaluationRun(UUID caseId, boolean passed, double score, String output, String provider) {
        this.id = UUID.randomUUID(); this.caseId = caseId; this.passed = passed; this.score = score;
        this.output = output; this.provider = provider; this.ranAt = Instant.now();
    }
    public UUID getId() { return id; }
    public UUID getCaseId() { return caseId; }
    public boolean isPassed() { return passed; }
    public double getScore() { return score; }
    public String getOutput() { return output; }
    public String getProvider() { return provider; }
    public Instant getRanAt() { return ranAt; }
}
