package com.dataengine.governance.api;
import java.util.Map;
public record GovernanceReportResponse(long totalChanges,long waitingApprovals,long blockedChanges,long activeContracts,long draftContracts,long openQualityFindings,long confirmedLineageEdges,long candidateLineageEdges,Map<String,Long> changesByRisk,Map<String,Long> changesByStatus) {}
