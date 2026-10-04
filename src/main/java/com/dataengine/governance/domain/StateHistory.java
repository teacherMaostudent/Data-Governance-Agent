package com.dataengine.governance.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "change_state_history")
public class StateHistory {
    @Id
    private UUID id;
    @Column(nullable = false)
    private UUID changeId;
    @Enumerated(EnumType.STRING)
    private ChangeStatus fromStatus;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChangeStatus toStatus;
    @Column(nullable = false)
    private String actor;
    @Column(nullable = false, length = 500)
    private String reason;
    @Column(nullable = false)
    private Instant occurredAt;

    protected StateHistory() {}

    public StateHistory(UUID changeId, ChangeStatus fromStatus, ChangeStatus toStatus, String actor, String reason) {
        this.id = UUID.randomUUID(); this.changeId = changeId; this.fromStatus = fromStatus;
        this.toStatus = toStatus; this.actor = actor; this.reason = reason; this.occurredAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getChangeId() { return changeId; }
    public ChangeStatus getFromStatus() { return fromStatus; }
    public ChangeStatus getToStatus() { return toStatus; }
    public String getActor() { return actor; }
    public String getReason() { return reason; }
    public Instant getOccurredAt() { return occurredAt; }
}
