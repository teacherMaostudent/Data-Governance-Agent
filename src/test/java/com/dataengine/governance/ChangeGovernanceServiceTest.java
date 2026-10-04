package com.dataengine.governance;

import com.dataengine.governance.api.ActionRequest;
import com.dataengine.governance.api.CreateChangeRequest;
import com.dataengine.governance.api.CreateContractRequest;
import com.dataengine.governance.api.CreateQualityRuleRequest;
import com.dataengine.governance.domain.*;
import com.dataengine.governance.service.ChangeGovernanceService;
import com.dataengine.governance.service.GovernanceException;
import com.dataengine.governance.service.GovernanceAgentService;
import com.dataengine.governance.service.CatalogService;
import com.dataengine.governance.service.SchemaCompatibilityService;
import com.dataengine.governance.service.ContractGovernanceService;
import com.dataengine.governance.service.QualityGovernanceService;
import com.dataengine.governance.service.NotificationService;
import com.dataengine.governance.service.LineageGovernanceService;
import com.dataengine.governance.service.GovernanceReportService;
import com.dataengine.governance.repository.LineageEdgeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest(properties = "spring.main.lazy-initialization=false")
@ActiveProfiles("test")
class ChangeGovernanceServiceTest {
    @Autowired ChangeGovernanceService service;
    @Autowired GovernanceAgentService agent;
    @Autowired CatalogService catalog;
    @Autowired SchemaCompatibilityService compatibility;
    @Autowired ContractGovernanceService contractGovernance;
    @Autowired QualityGovernanceService qualityGovernance;
    @Autowired NotificationService notifications;
    @Autowired LineageGovernanceService lineageGovernance;
    @Autowired LineageEdgeRepository lineageEdges;
    @Autowired GovernanceReportService reports;

    private CreateChangeRequest request(boolean destructive, boolean recoveryPlan) {
        return new CreateChangeRequest("测试变更", "验证治理状态机", ChangeType.SCHEMA, "tester", "PROD",
                "warehouse.dwd_test", "P0", "amount decimal(10,2)", "amount decimal(18,2)",
                8, destructive, recoveryPlan);
    }

    @Test
    void completeHappyPathPreservesEvidenceAndHistory() {
        ActionRequest action = new ActionRequest("tester", "test");
        DataChange change = service.create(request(false, true));
        service.analyze(change.getId(), action);
        service.validate(change.getId(), action);
        service.submit(change.getId(), action);
        service.approve(change.getId(), action);
        service.shadow(change.getId(), action);
        service.canary(change.getId(), action);
        service.promote(change.getId(), action);
        service.complete(change.getId(), action);

        var details = service.details(change.getId());
        assertThat(details.change().getStatus()).isEqualTo(ChangeStatus.COMPLETED);
        assertThat(details.evidence()).hasSizeGreaterThanOrEqualTo(7);
        assertThat(details.history()).hasSizeGreaterThanOrEqualTo(10);
        assertThat(details.releaseBundle()).isNotNull();
        assertThat(details.releaseBundle().getDigest()).hasSize(64);
        assertThat(details.executionReceipts()).extracting("action")
                .contains("SHADOW", "CANARY", "PROMOTE");
    }

    @Test
    void highRiskChangeWithoutRecoveryPlanCannotBeSubmitted() {
        ActionRequest action = new ActionRequest("tester", "test");
        DataChange change = service.create(request(true, false));
        service.analyze(change.getId(), action);
        service.validate(change.getId(), action);

        assertThatThrownBy(() -> service.submit(change.getId(), action))
                .isInstanceOf(GovernanceException.class)
                .hasMessageContaining("Recovery Plan");
    }

    @Test
    void invalidTransitionIsRejected() {
        DataChange change = service.create(request(false, true));
        assertThatThrownBy(() -> service.approve(change.getId(), new ActionRequest("tester", "skip")))
                .isInstanceOf(GovernanceException.class)
                .hasMessageContaining("不能执行");
    }

    @Test
    void backfillIsIdempotentAndProducesOneReceipt() {
        ActionRequest action = new ActionRequest("tester", "phase2");
        DataChange change = service.create(request(false, true));
        service.analyze(change.getId(), action); service.validate(change.getId(), action);
        service.submit(change.getId(), action); service.approve(change.getId(), action);
        service.shadow(change.getId(), action); service.canary(change.getId(), action);
        service.promote(change.getId(), action); service.complete(change.getId(), action);

        var first = service.backfill(change.getId(), action);
        var second = service.backfill(change.getId(), action);
        assertThat(second.getId()).isEqualTo(first.getId());
        assertThat(service.details(change.getId()).executionReceipts())
                .filteredOn(x -> x.getAction().equals("BACKFILL")).hasSize(1);
    }

    @Test
    void agentFindingRemainsCandidateUntilHumanVerification() {
        DataChange change = service.create(request(false, true));
        int evidenceBefore = service.details(change.getId()).evidence().size();
        var generated = agent.analyze(change.getId());

        assertThat(generated).hasSize(4).allMatch(x -> x.getStatus() == FindingStatus.CANDIDATE);
        assertThat(service.details(change.getId()).evidence()).hasSize(evidenceBefore);

        var verified = agent.verify(generated.getFirst().getId(), new ActionRequest("reviewer", "evidence checked"));
        assertThat(verified.getStatus()).isEqualTo(FindingStatus.VERIFIED);
        assertThat(service.details(change.getId()).evidence()).hasSize(evidenceBefore + 1);
    }

    @Test
    void goldenAndBadCasesPassReplayRegression() {
        var replay = agent.replay();
        assertThat(replay.total()).isGreaterThanOrEqualTo(6);
        assertThat(replay.passed()).isEqualTo(replay.total());
        assertThat(replay.passRate()).isEqualTo(100.0);
    }

    @Test
    void catalogProvidesConfirmedAndCandidateFieldLineage() {
        assertThat(catalog.confirmedDownstream("warehouse.dwd_order"))
                .containsExactly("warehouse.dws_sales_daily", "metric.gmv", "bi.finance_overview");
        assertThat(catalog.candidateDownstream("warehouse.dwd_order"))
                .containsExactly("external.monthly_export");
    }

    @Test
    void compatibilityBlocksUnsafeSchemaNarrowing() {
        var result = compatibility.check("amount decimal(18,2) nullable", "amount decimal(12,2) not null");
        assertThat(result.compatible()).isFalse();
        assertThat(result.breakingChanges()).contains("检测到精度收窄", "检测到 Nullable 收紧");
    }

    @Test
    void individualApprovalTasksRequireAllApprovalsAndSupportRework() {
        ActionRequest action = new ActionRequest("tester", "review");
        DataChange change = service.create(request(true, true));
        service.analyze(change.getId(), action); service.validate(change.getId(), action); service.submit(change.getId(), action);
        var tasks = service.details(change.getId()).approvalTasks();
        assertThat(tasks).hasSize(2).allMatch(t -> t.getStatus() == ApprovalStatus.PENDING);

        service.approveTask(change.getId(), tasks.getFirst().getId(), action);
        assertThat(service.details(change.getId()).change().getStatus()).isEqualTo(ChangeStatus.WAITING_APPROVAL);
        service.rejectTask(change.getId(), tasks.getLast().getId(), new ActionRequest("governance.admin", "补充财务校验说明"));
        assertThat(service.details(change.getId()).change().getStatus()).isEqualTo(ChangeStatus.APPROVAL_REJECTED);

        service.submit(change.getId(), new ActionRequest("tester", "已补充说明"));
        var resubmitted = service.details(change.getId()).approvalTasks().stream()
                .filter(t -> t.getStatus() == ApprovalStatus.PENDING).toList();
        service.approveTask(change.getId(), resubmitted.getFirst().getId(), action);
        service.approveTask(change.getId(), resubmitted.getLast().getId(), action);
        assertThat(service.details(change.getId()).change().getStatus()).isEqualTo(ChangeStatus.APPROVED);
        assertThat(service.details(change.getId()).releaseBundle()).isNotNull();
    }

    @Test
    void contractsAreVersionedAndOnlyCompatibleDraftCanReplaceActiveContract() {
        var first = contractGovernance.propose(new CreateContractRequest("warehouse.contract_test", "v1",
                "amount decimal(10,2) nullable", "金额", "null_rate", true, "contract.owner"));
        contractGovernance.publish(first.getId(), "contract.owner");
        var second = contractGovernance.propose(new CreateContractRequest("warehouse.contract_test", "v2",
                "amount decimal(18,2) nullable", "金额", "null_rate", true, "contract.owner"));
        contractGovernance.publish(second.getId(), "contract.owner");
        assertThat(contractGovernance.versions("warehouse.contract_test"))
                .extracting(DataContract::getStatus).containsExactlyInAnyOrder(ContractStatus.ACTIVE, ContractStatus.RETIRED);

        var incompatible = contractGovernance.propose(new CreateContractRequest("warehouse.contract_test", "v3",
                "amount decimal(8,2) not null", "金额", "null_rate", true, "contract.owner"));
        assertThatThrownBy(() -> contractGovernance.publish(incompatible.getId(), "contract.owner"))
                .isInstanceOf(GovernanceException.class).hasMessageContaining("不兼容");
    }

    @Test
    void qualityRuleBlocksThenWaiverClosesTheGateWithAuditableDisposition() {
        DataChange change = service.create(request(false, true));
        var rule = qualityGovernance.create("warehouse.dwd_test", "空值率上限", "null_rate", 0.01, GateStatus.BLOCK, "quality.owner");
        var failed = qualityGovernance.evaluate(change.getId(), rule.getId(), 0.03, "quality.bot");
        assertThat(failed.getStatus()).isEqualTo(QualityResultStatus.OPEN);
        assertThat(service.details(change.getId()).change().getQualityGate()).isEqualTo(GateStatus.BLOCK);

        qualityGovernance.waive(failed.getId(), "quality.owner", "已确认仅为历史脏数据，不影响本次字段扩容");
        assertThat(qualityGovernance.results(change.getId()).getFirst().getStatus()).isEqualTo(QualityResultStatus.WAIVED);
        assertThat(service.details(change.getId()).change().getQualityGate()).isEqualTo(GateStatus.PASS);
    }

    @Test
    void approvalLifecycleCreatesReadableBusinessNotifications() {
        ActionRequest action = new ActionRequest("tester", "review");
        DataChange change = service.create(request(true, true));
        service.analyze(change.getId(), action); service.validate(change.getId(), action); service.submit(change.getId(), action);
        assertThat(notifications.inbox("tester")).anyMatch(n -> n.getCategory().equals("APPROVAL"));
        var task = service.details(change.getId()).approvalTasks().getFirst();
        service.rejectTask(change.getId(), task.getId(), new ActionRequest("governance.admin", "需补充影响说明"));
        assertThat(notifications.inbox("tester")).anyMatch(n -> n.getCategory().equals("REWORK"));
    }

    @Test
    void candidateLineageRequiresReasonedHumanDispositionBeforeItAffectsImpact() {
        var candidate = lineageEdges.save(new LineageEdge("warehouse.dwd_order", "external.reviewed_export",
                "INFERRED", .72, LineageStatus.CANDIDATE, "agent:test"));
        lineageGovernance.confirm(candidate.getId(), "lineage.owner", "已核对月度导出任务依赖订单宽表");
        assertThat(catalog.confirmedDownstream("warehouse.dwd_order")).contains("external.reviewed_export");
        assertThat(lineageEdges.findById(candidate.getId()).orElseThrow().getVerifiedBy()).isEqualTo("lineage.owner");
    }

    @Test
    void governanceReportAggregatesBusinessBacklogAcrossDomains() {
        DataChange change = service.create(request(false, true));
        var report = reports.overview();
        assertThat(report.totalChanges()).isGreaterThanOrEqualTo(1);
        assertThat(report.changesByRisk()).containsKey(change.getRiskLevel().name());
        assertThat(report.confirmedLineageEdges()).isGreaterThanOrEqualTo(3);
    }
}
