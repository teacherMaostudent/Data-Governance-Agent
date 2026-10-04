package com.dataengine.governance.api;
import com.dataengine.governance.domain.DataContract; import com.dataengine.governance.service.ContractGovernanceService;
import jakarta.validation.Valid; import org.springframework.http.HttpStatus; import org.springframework.web.bind.annotation.*; import java.util.*;
@RestController @RequestMapping("/v1/contracts") public class ContractController {
 private final ContractGovernanceService service; public ContractController(ContractGovernanceService s){service=s;}
 @GetMapping("/{asset}") public List<DataContract> versions(@PathVariable String asset){return service.versions(asset);}
 @PostMapping @ResponseStatus(HttpStatus.CREATED) public DataContract propose(@Valid @RequestBody CreateContractRequest request){return service.propose(request);}
 @PostMapping("/{id}/publish") public DataContract publish(@PathVariable UUID id,@Valid @RequestBody ActionRequest action){return service.publish(id,action.actor());}
 @PostMapping("/{id}/retire") public DataContract retire(@PathVariable UUID id){return service.retire(id);}
}
