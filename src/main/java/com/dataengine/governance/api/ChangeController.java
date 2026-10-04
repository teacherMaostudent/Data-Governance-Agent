package com.dataengine.governance.api;

import com.dataengine.governance.domain.DataChange;
import com.dataengine.governance.domain.ExecutionReceipt;
import com.dataengine.governance.service.ChangeGovernanceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1")
public class ChangeController {
    private final ChangeGovernanceService service;

    public ChangeController(ChangeGovernanceService service) { this.service = service; }

    @GetMapping("/dashboard")
    public DashboardResponse dashboard() { return service.dashboard(); }

    @GetMapping("/data-changes")
    public List<DataChange> list() { return service.list(); }

    @PostMapping("/data-changes")
    @ResponseStatus(HttpStatus.CREATED)
    public DataChange create(@Valid @RequestBody CreateChangeRequest request) { return service.create(request); }

    @GetMapping("/data-changes/{id}")
    public ChangeDetailsResponse details(@PathVariable UUID id) { return service.details(id); }

    @PostMapping("/data-changes/{id}/analyze-impact")
    public DataChange analyze(@PathVariable UUID id, @Valid @RequestBody ActionRequest request) { return service.analyze(id, request); }

    @PostMapping("/data-changes/{id}/validate")
    public DataChange validate(@PathVariable UUID id, @Valid @RequestBody ActionRequest request) { return service.validate(id, request); }

    @PostMapping("/data-changes/{id}/submit")
    public DataChange submit(@PathVariable UUID id, @Valid @RequestBody ActionRequest request) { return service.submit(id, request); }

    @PostMapping("/data-changes/{id}/approve")
    public DataChange approve(@PathVariable UUID id, @Valid @RequestBody ActionRequest request) { return service.approve(id, request); }

    @PostMapping("/data-changes/{changeId}/approval-tasks/{taskId}/approve")
    public DataChange approveTask(@PathVariable UUID changeId, @PathVariable UUID taskId, @Valid @RequestBody ActionRequest request) { return service.approveTask(changeId, taskId, request); }

    @PostMapping("/data-changes/{changeId}/approval-tasks/{taskId}/reject")
    public DataChange rejectTask(@PathVariable UUID changeId, @PathVariable UUID taskId, @Valid @RequestBody ActionRequest request) { return service.rejectTask(changeId, taskId, request); }

    @PostMapping("/data-changes/{id}/shadow")
    public DataChange shadow(@PathVariable UUID id, @Valid @RequestBody ActionRequest request) { return service.shadow(id, request); }

    @PostMapping("/data-changes/{id}/canary")
    public DataChange canary(@PathVariable UUID id, @Valid @RequestBody ActionRequest request) { return service.canary(id, request); }

    @PostMapping("/data-changes/{id}/promote")
    public DataChange promote(@PathVariable UUID id, @Valid @RequestBody ActionRequest request) { return service.promote(id, request); }

    @PostMapping("/data-changes/{id}/complete")
    public DataChange complete(@PathVariable UUID id, @Valid @RequestBody ActionRequest request) { return service.complete(id, request); }

    @PostMapping("/data-changes/{id}/backfill")
    public ExecutionReceipt backfill(@PathVariable UUID id, @Valid @RequestBody ActionRequest request) { return service.backfill(id, request); }

    @PostMapping("/data-changes/{id}/recover")
    public DataChange recover(@PathVariable UUID id, @Valid @RequestBody ActionRequest request) { return service.recover(id, request); }
}
