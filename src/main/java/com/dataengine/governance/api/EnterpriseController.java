package com.dataengine.governance.api;
import com.dataengine.governance.domain.*; import com.dataengine.governance.repository.ApprovalTaskRepository; import com.dataengine.governance.service.*; import jakarta.validation.Valid; import org.springframework.web.bind.annotation.*; import java.util.*;
@RestController @RequestMapping("/v1/enterprise") public class EnterpriseController {
 private final CatalogService catalog; private final ApprovalTaskRepository approvals; private final LineageGovernanceService lineage;
 public EnterpriseController(CatalogService c,ApprovalTaskRepository a,LineageGovernanceService l){catalog=c;approvals=a;lineage=l;}
 @GetMapping("/assets") public List<DataAsset> assets(){return catalog.assets();}
 @GetMapping("/lineage") public List<LineageEdge> lineage(){return catalog.edges();}
 @GetMapping("/contracts") public List<DataContract> contracts(){return catalog.contracts();}
 @GetMapping("/approvals/{changeId}") public List<ApprovalTask> approvals(@PathVariable UUID changeId){return approvals.findByChangeIdOrderByDueAt(changeId);}
 @PostMapping("/lineage/{id}/confirm") public LineageEdge confirm(@PathVariable UUID id,@Valid @RequestBody ActionRequest r){return lineage.confirm(id,r.actor(),r.reason());}
 @PostMapping("/lineage/{id}/reject") public LineageEdge reject(@PathVariable UUID id,@Valid @RequestBody ActionRequest r){return lineage.reject(id,r.actor(),r.reason());}
}
