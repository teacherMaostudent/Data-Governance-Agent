package com.dataengine.governance.api;

import com.dataengine.governance.domain.*;
import com.dataengine.governance.service.GovernanceAgentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/v1/intelligence")
public class IntelligenceController {
    private final GovernanceAgentService service;
    public IntelligenceController(GovernanceAgentService service) { this.service = service; }

    @GetMapping("/overview")
    public IntelligenceOverviewResponse overview() { return service.overview(); }

    @PostMapping("/data-changes/{id}/analyze")
    public List<AgentFinding> analyze(@PathVariable UUID id) { return service.analyze(id); }

    @PostMapping("/data-changes/{id}/skills/{skill}")
    public AgentFinding analyzeSkill(@PathVariable UUID id, @PathVariable SkillType skill) { return service.analyze(id, skill); }

    @PostMapping("/findings/{id}/verify")
    public AgentFinding verify(@PathVariable UUID id, @Valid @RequestBody ActionRequest request) { return service.verify(id, request); }

    @PostMapping("/findings/{id}/reject")
    public AgentFinding reject(@PathVariable UUID id, @Valid @RequestBody ActionRequest request) { return service.reject(id, request); }

    @PostMapping("/replay")
    public ReplayResponse replay() { return service.replay(); }
}
