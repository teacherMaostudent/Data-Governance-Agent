package com.dataengine.governance.service;

import com.dataengine.governance.api.*;
import com.dataengine.governance.domain.*;
import com.dataengine.governance.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class ChangeGovernanceService {
    private final DataChangeRepository changes;
    private final EvidenceRepository evidence;
    private final StateHistoryRepository history;
    private final RiskEngine riskEngine;
    private final ImpactAnalysisService impactAnalysis;
    private final ExecutionRuntimeService runtime;
    private final SchemaCompatibilityService compatibility;
    private final ApprovalTaskRepository approvalTasks;
    private final NotificationService notifications;

    public ChangeGovernanceService(DataChangeRepository changes, EvidenceRepository evidence,
                                   StateHistoryRepository history, RiskEngine riskEngine,
                                   ImpactAnalysisService impactAnalysis, ExecutionRuntimeService runtime,
                                   SchemaCompatibilityService compatibility, ApprovalTaskRepository approvalTasks, NotificationService notifications) {
        this.changes = changes; this.evidence = evidence; this.history = history;
        this.riskEngine = riskEngine; this.impactAnalysis = impactAnalysis; this.runtime = runtime;
        this.compatibility = compatibility; this.approvalTasks = approvalTasks; this.notifications = notifications;
    }

    public DataChange create(CreateChangeRequest request) {
        DataChange change = new DataChange(request.title(), request.description(), request.type(), request.owner(),
                request.targetEnvironment().toUpperCase(), request.assetQualifiedName(), request.assetTier(),
                request.beforeSchema(), request.afterSchema(), request.consumerCount(), request.destructive(),
                request.recoveryPlanPresent());
        DataChange saved = changes.save(change);
        history.save(new StateHistory(saved.getId(), null, ChangeStatus.DRAFT, request.owner(), "创建数据变更"));
        evidence.save(new Evidence(saved.getId(), "HUMAN", "change-request", "变更需求和验收上下文已建档",
                "MEDIUM", VerificationStatus.VERIFIED));
        return saved;
    }

    @Transactional(readOnly = true)
    public List<DataChange> list() {
        return changes.findAll().stream().sorted(Comparator.comparing(DataChange::getUpdatedAt).reversed()).toList();
    }

    @Transactional(readOnly = true)
    public ChangeDetailsResponse details(UUID id) {
        DataChange change = require(id);
        return new ChangeDetailsResponse(change, evidence.findByChangeIdOrderByEventTimeDesc(id),
                history.findByChangeIdOrderByOccurredAtDesc(id), runtime.bundle(id).orElse(null), runtime.receipts(id), approvalTasks.findByChangeIdOrderByDueAt(id),
                nextActions(change), policyDecision(change));
    }

    public DataChange analyze(UUID id, ActionRequest action) {
        DataChange change = require(id);
        requireStatus(change, ChangeStatus.DRAFT);
        move(change, ChangeStatus.DISCOVERING, action.actor(), "开始资产发现与确定性影响分析");
        ImpactAnalysisService.ImpactResult result = impactAnalysis.analyze(change);
        change.setImpacts(result.confirmed(), result.candidate());
        RiskEngine.RiskAssessment risk = riskEngine.assess(change);
        change.setRisk(risk.score(), risk.level());
        evidence.save(new Evidence(id, "LINEAGE", "demo-lineage-snapshot",
                "确认 " + result.confirmed().size() + " 个影响对象；保留 " + result.candidate().size() + " 个候选对象",
                "HIGH", VerificationStatus.VERIFIED));
        evidence.save(new Evidence(id, "AGENT", "semantic-impact-skill",
                "候选长尾依赖仅用于提示，不参与确定性放行", "LOW", VerificationStatus.UNVERIFIED));
        move(change, ChangeStatus.IMPACT_ANALYZED, action.actor(), action.safeReason("影响分析和风险聚合完成"));
        return change;
    }

    public DataChange validate(UUID id, ActionRequest action) {
        DataChange change = require(id);
        requireStatus(change, ChangeStatus.IMPACT_ANALYZED);
        move(change, ChangeStatus.VALIDATING, action.actor(), "执行 Contract、Schema 与 Quality 门禁");
        SchemaCompatibilityService.CompatibilityResult result = compatibility.check(change.getBeforeSchema(), change.getAfterSchema());
        GateStatus contract = result.compatible() ? GateStatus.PASS : GateStatus.BLOCK;
        GateStatus quality = change.isDestructive() ? GateStatus.WARN : GateStatus.PASS;
        change.setContractGate(contract);
        change.setQualityGate(quality);
        evidence.save(new Evidence(id, "RULE", "contract-validator@v2",
                "Contract=" + contract + "，Quality=" + quality + "；" + String.join("；", result.breakingChanges()), "HIGH",
                contract == GateStatus.BLOCK ? VerificationStatus.REJECTED : VerificationStatus.VERIFIED));
        if (contract == GateStatus.BLOCK || quality == GateStatus.BLOCK) {
            move(change, ChangeStatus.VALIDATION_FAILED, action.actor(), "发布门禁阻断");
        }
        return change;
    }

    public DataChange submit(UUID id, ActionRequest action) {
        DataChange change = require(id);
        if (!Set.of(ChangeStatus.VALIDATING, ChangeStatus.APPROVAL_REJECTED).contains(change.getStatus())) {
            throw new GovernanceException("当前状态 " + change.getStatus() + " 不能提交审批");
        }
        if (change.getContractGate() == GateStatus.BLOCK || change.getQualityGate() == GateStatus.BLOCK) {
            throw new GovernanceException("存在 BLOCK 门禁，不能提交审批");
        }
        if ((change.getRiskLevel() == RiskLevel.HIGH || change.getRiskLevel() == RiskLevel.CRITICAL)
                && !change.isRecoveryPlanPresent()) {
            throw new GovernanceException("高风险变更必须具备 Recovery Plan");
        }
        move(change, ChangeStatus.WAITING_APPROVAL, action.actor(), action.safeReason(change.getStatus() == ChangeStatus.APPROVAL_REJECTED ? "修改后重新提交审批" : "策略要求 Owner 审批"));
        approvalTasks.save(new ApprovalTask(id, "OWNER", "DATA_OWNER", change.getOwner(), java.time.Instant.now().plus(java.time.Duration.ofHours(24))));
        notifications.notify(change.getOwner(), "APPROVAL", "待审批：" + change.getTitle(), "请确认影响范围、业务语义和恢复计划", id);
        if (change.getRiskLevel() == RiskLevel.HIGH || change.getRiskLevel() == RiskLevel.CRITICAL)
            approvalTasks.save(new ApprovalTask(id, "RISK", "GOVERNANCE_ADMIN", "governance.admin", java.time.Instant.now().plus(java.time.Duration.ofHours(24))));
        if (change.getRiskLevel() == RiskLevel.HIGH || change.getRiskLevel() == RiskLevel.CRITICAL) notifications.notify("governance.admin", "APPROVAL", "高风险待审批：" + change.getTitle(), "请完成风险审查与恢复方案确认", id);
        return change;
    }

    public DataChange approve(UUID id, ActionRequest action) {
        DataChange change = require(id);
        requireStatus(change, ChangeStatus.WAITING_APPROVAL);
        List<ApprovalTask> pending = approvalTasks.findByChangeIdAndStatus(id, ApprovalStatus.PENDING);
        for (ApprovalTask task : pending) task.approve(action.actor());
        change.approve();
        ReleaseBundle bundle = runtime.freeze(change, action.actor());
        evidence.save(new Evidence(id, "HUMAN", "approval-task", "审批通过并冻结发布上下文",
                "HIGH", VerificationStatus.VERIFIED));
        evidence.save(new Evidence(id, "BUNDLE", bundle.getDigest(), "不可变 Release Bundle 已冻结",
                "HIGH", VerificationStatus.VERIFIED));
        move(change, ChangeStatus.APPROVED, action.actor(), action.safeReason("审批通过"));
        return change;
    }

    public DataChange approveTask(UUID changeId, UUID taskId, ActionRequest action) {
        DataChange change = require(changeId);
        requireStatus(change, ChangeStatus.WAITING_APPROVAL);
        ApprovalTask task = requireApprovalTask(changeId, taskId);
        if (task.getStatus() != ApprovalStatus.PENDING) throw new GovernanceException("该审批任务已处理");
        task.approve(action.actor());
        evidence.save(new Evidence(changeId, "HUMAN", "approval-task:" + taskId,
                task.getApprovalStage() + " 审批通过：" + action.safeReason("人工确认"), "HIGH", VerificationStatus.VERIFIED));
        if (approvalTasks.findByChangeIdAndStatus(changeId, ApprovalStatus.PENDING).isEmpty()) completeApproval(change, action);
        return change;
    }

    public DataChange rejectTask(UUID changeId, UUID taskId, ActionRequest action) {
        DataChange change = require(changeId);
        requireStatus(change, ChangeStatus.WAITING_APPROVAL);
        ApprovalTask task = requireApprovalTask(changeId, taskId);
        if (task.getStatus() != ApprovalStatus.PENDING) throw new GovernanceException("该审批任务已处理");
        task.reject(action.actor());
        evidence.save(new Evidence(changeId, "HUMAN", "approval-task:" + taskId,
                task.getApprovalStage() + " 审批驳回：" + action.safeReason("需要修改后重提"), "HIGH", VerificationStatus.VERIFIED));
        move(change, ChangeStatus.APPROVAL_REJECTED, action.actor(), "审批驳回，等待修改后重新提交");
        notifications.notify(change.getOwner(), "REWORK", "变更已退回：" + change.getTitle(), action.safeReason("请修订后重新提交"), changeId);
        return change;
    }

    private ApprovalTask requireApprovalTask(UUID changeId, UUID taskId) {
        return approvalTasks.findByIdAndChangeId(taskId, changeId)
                .orElseThrow(() -> new GovernanceException("审批任务不存在或不属于该变更"));
    }

    private void completeApproval(DataChange change, ActionRequest action) {
        change.approve();
        ReleaseBundle bundle = runtime.freeze(change, action.actor());
        evidence.save(new Evidence(change.getId(), "HUMAN", "approval-task", "全部审批任务通过并冻结发布上下文",
                "HIGH", VerificationStatus.VERIFIED));
        evidence.save(new Evidence(change.getId(), "BUNDLE", bundle.getDigest(), "不可变 Release Bundle 已冻结",
                "HIGH", VerificationStatus.VERIFIED));
        move(change, ChangeStatus.APPROVED, action.actor(), action.safeReason("全部审批通过"));
        notifications.notify(change.getOwner(), "RELEASE", "变更已批准：" + change.getTitle(), "Release Bundle 已冻结，可进入 Shadow 对账", change.getId());
    }

    public DataChange shadow(UUID id, ActionRequest action) {
        DataChange change = require(id);
        requireStatus(change, ChangeStatus.APPROVED);
        move(change, ChangeStatus.SHADOWING, action.actor(), "进入完整业务窗口 Shadow 对账");
        ExecutionReceipt receipt = runtime.execute(change, "SHADOW", action.actor());
        change.setShadowGate(GateStatus.PASS);
        evidence.save(new Evidence(id, "RECEIPT", receipt.getTraceId(), receipt.getResult(),
                "HIGH", VerificationStatus.VERIFIED));
        return change;
    }

    public DataChange canary(UUID id, ActionRequest action) {
        DataChange change = require(id);
        requireStatus(change, ChangeStatus.SHADOWING);
        if (change.getShadowGate() != GateStatus.PASS) throw new GovernanceException("Shadow 未通过");
        move(change, ChangeStatus.CANARY, action.actor(), "进入 5% 分区 Canary");
        ExecutionReceipt receipt = runtime.execute(change, "CANARY", action.actor());
        change.setCanaryGate(GateStatus.PASS);
        evidence.save(new Evidence(id, "RECEIPT", receipt.getTraceId(), receipt.getResult(),
                "HIGH", VerificationStatus.VERIFIED));
        return change;
    }

    public DataChange promote(UUID id, ActionRequest action) {
        DataChange change = require(id);
        requireStatus(change, ChangeStatus.CANARY);
        if (change.getCanaryGate() != GateStatus.PASS || !change.isApproved()) {
            throw new GovernanceException("Canary 或审批条件未满足");
        }
        move(change, ChangeStatus.PROMOTING, action.actor(), "提升至 100% 流量");
        ExecutionReceipt receipt = runtime.execute(change, "PROMOTE", action.actor());
        evidence.save(new Evidence(id, "RECEIPT", receipt.getTraceId(), receipt.getResult(),
                "HIGH", VerificationStatus.VERIFIED));
        move(change, ChangeStatus.PRODUCTION, action.actor(), action.safeReason("生产提升完成，进入观察窗口"));
        return change;
    }

    public DataChange complete(UUID id, ActionRequest action) {
        DataChange change = require(id);
        requireStatus(change, ChangeStatus.PRODUCTION);
        move(change, ChangeStatus.COMPLETED, action.actor(), action.safeReason("生产观察窗口通过"));
        return change;
    }

    public DataChange recover(UUID id, ActionRequest action) {
        DataChange change = require(id);
        if (!Set.of(ChangeStatus.PRODUCTION, ChangeStatus.VALIDATION_FAILED, ChangeStatus.SHADOW_FAILED,
                ChangeStatus.CANARY_FAILED, ChangeStatus.ROLLBACK_REQUIRED).contains(change.getStatus())) {
            throw new GovernanceException("当前状态不允许触发恢复");
        }
        if (!change.isRecoveryPlanPresent()) throw new GovernanceException("缺少 Recovery Plan");
        move(change, ChangeStatus.ROLLING_BACK, action.actor(), action.safeReason("执行补偿 DAG"));
        ExecutionReceipt receipt = runtime.execute(change, "RECOVERY", action.actor());
        evidence.save(new Evidence(id, "RECEIPT", receipt.getTraceId(), receipt.getResult(),
                "HIGH", VerificationStatus.VERIFIED));
        move(change, ChangeStatus.RECOVERED, action.actor(), "恢复完成");
        return change;
    }

    public ExecutionReceipt backfill(UUID id, ActionRequest action) {
        DataChange change = require(id);
        if (!Set.of(ChangeStatus.PRODUCTION, ChangeStatus.COMPLETED).contains(change.getStatus())) {
            throw new GovernanceException("仅生产观察或已完成的变更允许执行 Backfill");
        }
        ExecutionReceipt receipt = runtime.execute(change, "BACKFILL", action.actor());
        evidence.save(new Evidence(id, "RECEIPT", receipt.getTraceId(), receipt.getResult(),
                "HIGH", VerificationStatus.VERIFIED));
        return receipt;
    }

    @Transactional(readOnly = true)
    public DashboardResponse dashboard() {
        List<DataChange> all = changes.findAll();
        Set<ChangeStatus> terminal = Set.of(ChangeStatus.COMPLETED, ChangeStatus.RECOVERED, ChangeStatus.CANCELLED, ChangeStatus.EXPIRED);
        long blocked = all.stream().filter(c -> c.getStatus().name().endsWith("FAILED") || c.getRiskLevel() == RiskLevel.CRITICAL).count();
        long withEvidence = all.stream().filter(c -> !evidence.findByChangeIdOrderByEventTimeDesc(c.getId()).isEmpty()).count();
        Map<String, Long> byRisk = all.stream().map(c -> c.getRiskLevel().name())
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        Map<String, Long> byStatus = all.stream().map(c -> c.getStatus().name())
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        return new DashboardResponse(all.size(), all.stream().filter(c -> !terminal.contains(c.getStatus())).count(), blocked,
                all.stream().filter(c -> terminal.contains(c.getStatus())).count(),
                all.isEmpty() ? 100 : (int) Math.round(withEvidence * 100.0 / all.size()), byRisk, byStatus);
    }

    private DataChange require(UUID id) {
        return changes.findById(id).orElseThrow(() -> new GovernanceException("Data Change 不存在"));
    }
    private void requireStatus(DataChange change, ChangeStatus required) {
        if (change.getStatus() != required) throw new GovernanceException("当前状态 " + change.getStatus() + " 不能执行此操作，需要 " + required);
    }
    private void move(DataChange change, ChangeStatus to, String actor, String reason) {
        ChangeStatus from = change.getStatus(); change.transitionTo(to); changes.save(change);
        history.save(new StateHistory(change.getId(), from, to, actor, reason));
    }
    private List<String> nextActions(DataChange c) {
        return switch (c.getStatus()) {
            case DRAFT -> List.of("analyze-impact");
            case IMPACT_ANALYZED -> List.of("validate");
            case VALIDATING -> List.of("submit");
            case WAITING_APPROVAL -> List.of();
            case APPROVAL_REJECTED -> List.of("submit");
            case APPROVED -> List.of("shadow");
            case SHADOWING -> List.of("canary");
            case CANARY -> List.of("promote");
            case PRODUCTION -> c.isRecoveryPlanPresent() ? List.of("backfill", "complete", "recover") : List.of("backfill", "complete");
            case COMPLETED -> List.of("backfill");
            case VALIDATION_FAILED, SHADOW_FAILED, CANARY_FAILED, ROLLBACK_REQUIRED -> c.isRecoveryPlanPresent() ? List.of("recover") : List.of();
            default -> List.of();
        };
    }
    private String policyDecision(DataChange c) {
        if (c.getRiskLevel() == RiskLevel.CRITICAL) return "BLOCK_AUTOMATION · REQUIRE_MIGRATION · REQUIRE_RECOVERY_PLAN";
        if (c.getRiskLevel() == RiskLevel.HIGH) return "REQUIRE_APPROVAL · REQUIRE_SHADOW · REQUIRE_CANARY · REQUIRE_RECOVERY_PLAN";
        if (c.getRiskLevel() == RiskLevel.MEDIUM) return "REQUIRE_APPROVAL · REQUIRE_SHADOW";
        return "ALLOW_AFTER_VALIDATION";
    }
}
