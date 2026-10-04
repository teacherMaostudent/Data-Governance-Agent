package com.dataengine.governance.api;

import com.dataengine.governance.domain.DataChange;
import com.dataengine.governance.domain.Evidence;
import com.dataengine.governance.domain.StateHistory;
import com.dataengine.governance.domain.ReleaseBundle;
import com.dataengine.governance.domain.ExecutionReceipt;
import com.dataengine.governance.domain.ApprovalTask;
import java.util.List;

public record ChangeDetailsResponse(
        DataChange change,
        List<Evidence> evidence,
        List<StateHistory> history,
        ReleaseBundle releaseBundle,
        List<ExecutionReceipt> executionReceipts,
        List<ApprovalTask> approvalTasks,
        List<String> nextActions,
        String policyDecision
) {}
