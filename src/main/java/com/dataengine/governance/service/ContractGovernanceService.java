package com.dataengine.governance.service;

import com.dataengine.governance.api.CreateContractRequest;
import com.dataengine.governance.domain.*;
import com.dataengine.governance.repository.DataContractRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service @Transactional
public class ContractGovernanceService {
 private final DataContractRepository contracts; private final SchemaCompatibilityService compatibility;
 public ContractGovernanceService(DataContractRepository c, SchemaCompatibilityService s){contracts=c;compatibility=s;}
 @Transactional(readOnly=true) public List<DataContract> versions(String asset){return contracts.findByAssetQualifiedNameOrderByUpdatedAtDesc(asset);}
 public DataContract propose(CreateContractRequest r){
  if(contracts.findByAssetQualifiedNameOrderByUpdatedAtDesc(r.assetQualifiedName()).stream().anyMatch(x->x.getVersion().equals(r.version()))) throw new GovernanceException("该资产的 Contract 版本已存在");
  return contracts.save(DataContract.draft(r.assetQualifiedName(),r.version(),r.schemaDefinition(),r.semanticDefinition(),r.qualityPolicy(),r.requireBackwardCompatibility(),r.owner()));
 }
 public DataContract publish(UUID id,String actor){
  DataContract next=require(id); if(next.getStatus()!=ContractStatus.DRAFT) throw new GovernanceException("仅草拟 Contract 可以发布");
  contracts.findByAssetQualifiedNameAndStatus(next.getAssetQualifiedName(),ContractStatus.ACTIVE).ifPresent(active->{
   if(next.isRequireBackwardCompatibility() && !compatibility.check(active.getSchemaDefinition(),next.getSchemaDefinition()).compatible()) throw new GovernanceException("Contract 不兼容，不能发布");
   active.retire(); contracts.save(active);
  });
  next.publish(actor); return contracts.save(next);
 }
 public DataContract retire(UUID id){DataContract c=require(id);if(c.getStatus()==ContractStatus.RETIRED)throw new GovernanceException("Contract 已退役");c.retire();return contracts.save(c);}
 private DataContract require(UUID id){return contracts.findById(id).orElseThrow(()->new GovernanceException("Data Contract 不存在"));}
}
