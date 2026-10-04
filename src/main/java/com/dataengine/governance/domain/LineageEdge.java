package com.dataengine.governance.domain;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
@Entity @Table(name="lineage_edge")
public class LineageEdge {
 @Id private UUID id; @Column(nullable=false) private String sourceAsset; @Column(nullable=false) private String targetAsset;
 @Column(nullable=false) private String edgeType; private double confidence; @Enumerated(EnumType.STRING) @Column(nullable=false) private LineageStatus status;
 @Column(nullable=false) private String evidenceRef; @Column(nullable=false) private Instant verifiedAt; private String verifiedBy; private String verificationReason;
 protected LineageEdge(){} public LineageEdge(String s,String t,String e,double c,LineageStatus st,String ref){id=UUID.randomUUID();sourceAsset=s;targetAsset=t;edgeType=e;confidence=c;status=st;evidenceRef=ref;verifiedAt=Instant.now();}
 public void confirm(String actor,String reason){status=LineageStatus.CONFIRMED;verifiedBy=actor;verificationReason=reason;verifiedAt=Instant.now();} public void reject(String actor,String reason){status=LineageStatus.REJECTED;verifiedBy=actor;verificationReason=reason;verifiedAt=Instant.now();}
 public UUID getId(){return id;} public String getSourceAsset(){return sourceAsset;} public String getTargetAsset(){return targetAsset;} public String getEdgeType(){return edgeType;} public double getConfidence(){return confidence;} public LineageStatus getStatus(){return status;} public String getEvidenceRef(){return evidenceRef;} public Instant getVerifiedAt(){return verifiedAt;} public String getVerifiedBy(){return verifiedBy;} public String getVerificationReason(){return verificationReason;}
}
