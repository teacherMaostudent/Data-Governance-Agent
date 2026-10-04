package com.dataengine.governance.api;

import com.dataengine.governance.domain.*;
import java.util.List;

public record IntelligenceOverviewResponse(
        String provider,
        String modelVersion,
        List<SkillCard> skills,
        List<KnowledgeDocument> documents,
        List<AgentFinding> findings,
        EvaluationSummary evaluation
) {
    public record SkillCard(String type, String name, String description, String boundary) {}
    public record EvaluationSummary(long cases, long runs, long passed, double passRate,
                                    long candidateFindings, long verifiedFindings,
                                    List<EvaluationRun> recentRuns) {}
}
