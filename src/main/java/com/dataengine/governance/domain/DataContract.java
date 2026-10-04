package com.dataengine.governance.domain;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
@Entity @Table(name="data_contract")
public class DataContract {
 @Id private UUID id; @Column(nullable=false) private String assetQualifiedName; @Column(nullable=false) private String version;
 @Column(nullable=false,length=4000) private String schemaDefinition; @Column(nullable=false,length=2000) private String semanticDefinition;
 @Column(nullable=false,length=2000) private String qualityPolicy; @Column(nullable=false) private boolean requireBackwardCompatibility; @Column(nullable=false) private Instant updatedAt; @Enumerated(EnumType.STRING) @Column(nullable=false) private ContractStatus status; @Column(nullable=false) private String owner; private String publishedBy;
 protected DataContract(){} public DataContract(String a,String v,String s,String sem,String q,boolean b){id=UUID.randomUUID();assetQualifiedName=a;version=v;schemaDefinition=s;semanticDefinition=sem;qualityPolicy=q;requireBackwardCompatibility=b;owner="catalog.owner";status=ContractStatus.ACTIVE;updatedAt=Instant.now();}
 private DataContract(String a,String v,String s,String sem,String q,boolean b,String o){id=UUID.randomUUID();assetQualifiedName=a;version=v;schemaDefinition=s;semanticDefinition=sem;qualityPolicy=q;requireBackwardCompatibility=b;owner=o;status=ContractStatus.DRAFT;updatedAt=Instant.now();}
 public static DataContract draft(String a,String v,String s,String sem,String q,boolean b,String o){return new DataContract(a,v,s,sem,q,b,o);} public void publish(String actor){status=ContractStatus.ACTIVE;publishedBy=actor;updatedAt=Instant.now();} public void retire(){status=ContractStatus.RETIRED;updatedAt=Instant.now();}
 public UUID getId(){return id;} public String getAssetQualifiedName(){return assetQualifiedName;} public String getVersion(){return version;} public String getSchemaDefinition(){return schemaDefinition;} public String getSemanticDefinition(){return semanticDefinition;} public String getQualityPolicy(){return qualityPolicy;} public boolean isRequireBackwardCompatibility(){return requireBackwardCompatibility;} public Instant getUpdatedAt(){return updatedAt;} public ContractStatus getStatus(){return status;} public String getOwner(){return owner;} public String getPublishedBy(){return publishedBy;}
}
