package com.dataengine.governance.api;

import java.util.List;

public record RuntimeSummaryResponse(
        long totalExecutions,
        long successfulExecutions,
        double successRate,
        long p95DurationMs,
        int attemptBudget,
        List<ConnectorStatus> connectors,
        List<SloStatus> slos
) {
    public record ConnectorStatus(String name, String mode, String status, String capability) {}
    public record SloStatus(String name, String target, String actual, String status) {}
}
