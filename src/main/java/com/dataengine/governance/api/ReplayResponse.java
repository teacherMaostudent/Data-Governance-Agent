package com.dataengine.governance.api;
import com.dataengine.governance.domain.EvaluationRun;
import java.util.List;
public record ReplayResponse(int total, int passed, double passRate, List<EvaluationRun> runs) {}
