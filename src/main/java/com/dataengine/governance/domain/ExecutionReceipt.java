package com.dataengine.governance.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "execution_receipt", uniqueConstraints = @UniqueConstraint(columnNames = "idempotencyKey"))
public class ExecutionReceipt {
    @Id private UUID id;
    @Column(nullable = false) private UUID changeId;
    @Column(nullable = false) private String action;
    @Column(nullable = false, length = 64) private String idempotencyKey;
    @Column(nullable = false) private String connector;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private ExecutionStatus status;
    @Column(nullable = false) private int attempt;
    @Column(nullable = false, length = 1000) private String result;
    @Column(nullable = false) private String resourceVersion;
    @Column(nullable = false) private String traceId;
    @Column(nullable = false) private long durationMs;
    @Column(nullable = false) private Instant startedAt;
    @Column(nullable = false) private Instant completedAt;

    protected ExecutionReceipt() {}
    public ExecutionReceipt(UUID changeId, String action, String idempotencyKey, String connector,
                            ExecutionStatus status, int attempt, String result, String resourceVersion,
                            String traceId, long durationMs, Instant startedAt) {
        this.id = UUID.randomUUID(); this.changeId = changeId; this.action = action;
        this.idempotencyKey = idempotencyKey; this.connector = connector; this.status = status;
        this.attempt = attempt; this.result = result; this.resourceVersion = resourceVersion;
        this.traceId = traceId; this.durationMs = durationMs; this.startedAt = startedAt;
        this.completedAt = Instant.now();
    }
    public UUID getId() { return id; }
    public UUID getChangeId() { return changeId; }
    public String getAction() { return action; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public String getConnector() { return connector; }
    public ExecutionStatus getStatus() { return status; }
    public int getAttempt() { return attempt; }
    public String getResult() { return result; }
    public String getResourceVersion() { return resourceVersion; }
    public String getTraceId() { return traceId; }
    public long getDurationMs() { return durationMs; }
    public Instant getStartedAt() { return startedAt; }
    public Instant getCompletedAt() { return completedAt; }
}
