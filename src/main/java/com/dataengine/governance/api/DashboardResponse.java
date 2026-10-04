package com.dataengine.governance.api;

import java.util.Map;

public record DashboardResponse(
        long totalChanges,
        long activeChanges,
        long blockedChanges,
        long completedChanges,
        int evidenceCoverage,
        Map<String, Long> byRisk,
        Map<String, Long> byStatus
) {}
