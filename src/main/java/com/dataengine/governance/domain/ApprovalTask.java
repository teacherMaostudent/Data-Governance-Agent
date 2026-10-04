package com.dataengine.governance.domain;
import jakarta.persistence.*;
import java.time.*; import java.util.UUID;
@Entity @Table(name="approval_task")
public class ApprovalTask {
 @Id private UUID id; @Column(nullable=false) private UUID changeId; @Column(nullable=false) private String approvalStage; @Column(nullable=false) private String approverRole; @Column(nullable=false) private String assignee;
 @Enumerated(EnumType.STRING) @Column(nullable=false) private ApprovalStatus status; @Column(nullable=false) private Instant dueAt; private Instant decidedAt; private String decidedBy;
 protected ApprovalTask(){} public ApprovalTask(UUID c,String st,String role,String a,Instant due){id=UUID.randomUUID();changeId=c;approvalStage=st;approverRole=role;assignee=a;dueAt=due;status=ApprovalStatus.PENDING;}
 public void approve(String actor){status=ApprovalStatus.APPROVED;decidedBy=actor;decidedAt=Instant.now();} public void reject(String actor){status=ApprovalStatus.REJECTED;decidedBy=actor;decidedAt=Instant.now();}
 public UUID getId(){return id;} public UUID getChangeId(){return changeId;} public String getApprovalStage(){return approvalStage;} public String getApproverRole(){return approverRole;} public String getAssignee(){return assignee;} public ApprovalStatus getStatus(){return status;} public Instant getDueAt(){return dueAt;} public Instant getDecidedAt(){return decidedAt;} public String getDecidedBy(){return decidedBy;}
}
