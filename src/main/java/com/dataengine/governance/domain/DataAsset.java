package com.dataengine.governance.domain;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
@Entity @Table(name="data_asset")
public class DataAsset {
 @Id private UUID id; @Column(nullable=false,unique=true) private String qualifiedName;
 @Enumerated(EnumType.STRING) @Column(nullable=false) private AssetType type;
 @Column(nullable=false) private String owner; @Column(nullable=false) private String tier;
 @Column(nullable=false) private String classification; @Column(nullable=false) private Instant updatedAt;
 protected DataAsset(){} public DataAsset(String n,AssetType t,String o,String tier,String c){id=UUID.randomUUID();qualifiedName=n;type=t;owner=o;this.tier=tier;classification=c;updatedAt=Instant.now();}
 public UUID getId(){return id;} public String getQualifiedName(){return qualifiedName;} public AssetType getType(){return type;} public String getOwner(){return owner;} public String getTier(){return tier;} public String getClassification(){return classification;} public Instant getUpdatedAt(){return updatedAt;}
}
