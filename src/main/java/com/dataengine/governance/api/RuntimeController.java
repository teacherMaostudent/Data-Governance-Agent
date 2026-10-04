package com.dataengine.governance.api;

import com.dataengine.governance.domain.ExecutionReceipt;
import com.dataengine.governance.domain.ReleaseBundle;
import com.dataengine.governance.service.ExecutionRuntimeService;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/v1/runtime")
public class RuntimeController {
    private final ExecutionRuntimeService runtime;
    public RuntimeController(ExecutionRuntimeService runtime) { this.runtime = runtime; }

    @GetMapping("/summary")
    public RuntimeSummaryResponse summary() { return runtime.summary(); }

    @GetMapping("/data-changes/{id}/release-bundle")
    public ReleaseBundle bundle(@PathVariable UUID id) {
        return runtime.bundle(id).orElseThrow(() -> new NoSuchElementException("Release Bundle 不存在"));
    }

    @GetMapping("/data-changes/{id}/receipts")
    public List<ExecutionReceipt> receipts(@PathVariable UUID id) { return runtime.receipts(id); }
}
