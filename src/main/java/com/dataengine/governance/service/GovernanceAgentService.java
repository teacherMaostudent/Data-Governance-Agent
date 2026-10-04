package com.dataengine.governance.service;

import com.dataengine.governance.api.*;
import com.dataengine.governance.domain.*;
import com.dataengine.governance.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
@Transactional
public class GovernanceAgentService {
    private final DataChangeRepository changes;
    private final KnowledgeDocumentRepository documents;
    private final AgentFindingRepository findings;
    private final EvaluationCaseRepository cases;
    private final EvaluationRunRepository runs;
    private final EvidenceRepository evidence;
    private final GovernanceIntelligenceProvider provider;

    public GovernanceAgentService(DataChangeRepository changes, KnowledgeDocumentRepository documents,
                                  AgentFindingRepository findings, EvaluationCaseRepository cases,
                                  EvaluationRunRepository runs, EvidenceRepository evidence,
                                  GovernanceIntelligenceProvider provider) {
        this.changes = changes; this.documents = documents; this.findings = findings;
        this.cases = cases; this.runs = runs; this.evidence = evidence; this.provider = provider;
    }

    public List<AgentFinding> analyze(UUID changeId) {
        DataChange change = changes.findById(changeId).orElseThrow(() -> new GovernanceException("Data Change 不存在"));
        return Arrays.stream(SkillType.values()).map(type -> runSkill(change, type)).toList();
    }

    public AgentFinding analyze(UUID changeId, SkillType skill) {
        DataChange change = changes.findById(changeId).orElseThrow(() -> new GovernanceException("Data Change 不存在"));
        return runSkill(change, skill);
    }

    public AgentFinding verify(UUID findingId, ActionRequest request) {
        AgentFinding finding = findings.findById(findingId).orElseThrow(() -> new GovernanceException("Candidate Finding 不存在"));
        if (finding.getStatus() != FindingStatus.CANDIDATE) throw new GovernanceException("Finding 已经完成验证");
        if (finding.getEvidenceRefs().isEmpty()) throw new GovernanceException("没有证据引用，不能转为正式事实");
        finding.verify(request.actor());
        evidence.save(new Evidence(finding.getChangeId(), "AGENT_VERIFIED", finding.getId().toString(),
                finding.getClaim(), "MEDIUM", VerificationStatus.VERIFIED));
        return findings.save(finding);
    }

    public AgentFinding reject(UUID findingId, ActionRequest request) {
        AgentFinding finding = findings.findById(findingId).orElseThrow(() -> new GovernanceException("Candidate Finding 不存在"));
        if (finding.getStatus() != FindingStatus.CANDIDATE) throw new GovernanceException("Finding 已经完成验证");
        finding.reject(request.actor());
        return findings.save(finding);
    }

    public ReplayResponse replay() {
        List<EvaluationRun> result = cases.findAll().stream().map(c -> {
            String output = provider.reason(c.getSkillType(), c.getInput(), null);
            List<String> expected = Arrays.stream(c.getExpectedSignals().split(",")).map(String::trim).filter(x -> !x.isBlank()).toList();
            long matched = expected.stream().filter(output::contains).count();
            double score = expected.isEmpty() ? 1 : matched * 1.0 / expected.size();
            boolean passed = c.isBadCase() ? score == 0 : score >= .5;
            return runs.save(new EvaluationRun(c.getId(), passed, score, output, provider.name()));
        }).toList();
        int passed = (int) result.stream().filter(EvaluationRun::isPassed).count();
        return new ReplayResponse(result.size(), passed,
                result.isEmpty() ? 100 : Math.round(passed * 1000.0 / result.size()) / 10.0, result);
    }

    @Transactional(readOnly = true)
    public IntelligenceOverviewResponse overview() {
        List<EvaluationRun> recent = runs.findTop20ByOrderByRanAtDesc();
        long passed = recent.stream().filter(EvaluationRun::isPassed).count();
        return new IntelligenceOverviewResponse(provider.name(), provider.modelVersion(), List.of(
                new IntelligenceOverviewResponse.SkillCard("REQUIREMENT", "需求解析", "提取范围、约束与验收信号", "仅生成候选结构化建议"),
                new IntelligenceOverviewResponse.SkillCard("IMPACT", "长尾影响", "补充正式血缘之外的候选依赖", "不得写入 confirmed lineage"),
                new IntelligenceOverviewResponse.SkillCard("QUALITY", "质量规则", "建议对账维度、阈值和观察窗口", "不得自行放行门禁"),
                new IntelligenceOverviewResponse.SkillCard("INCIDENT", "事故归因", "关联执行回执、恢复步骤和故障模式", "不得触发生产副作用")
        ), documents.findAll(), findings.findAllByOrderByCreatedAtDesc(),
                new IntelligenceOverviewResponse.EvaluationSummary(cases.count(), runs.count(), passed,
                        recent.isEmpty() ? 0 : Math.round(passed * 1000.0 / recent.size()) / 10.0,
                        findings.countByStatus(FindingStatus.CANDIDATE), findings.countByStatus(FindingStatus.VERIFIED), recent));
    }

    private AgentFinding runSkill(DataChange change, SkillType type) {
        String input = change.getTitle() + "；" + change.getDescription() + "；资产=" + change.getAssetQualifiedName();
        List<KnowledgeDocument> refs = retrieve(input, type);
        String claim = provider.reason(type, input, change);
        double confidence = Math.min(.92, .58 + refs.size() * .08 + (change.getConfirmedImpacts().isEmpty() ? 0 : .08));
        return findings.save(new AgentFinding(change.getId(), type, claim, confidence, provider.name(), provider.modelVersion(),
                refs.stream().map(x -> x.getTitle() + "@" + x.getVersion()).toList()));
    }

    private List<KnowledgeDocument> retrieve(String input, SkillType type) {
        String lower = input.toLowerCase(Locale.ROOT);
        return documents.findAll().stream().filter(d -> {
            String corpus = (d.getTags() + " " + d.getTitle() + " " + d.getContent()).toLowerCase(Locale.ROOT);
            return corpus.contains(type.name().toLowerCase(Locale.ROOT)) || Arrays.stream(lower.split("[^a-z0-9\u4e00-\u9fa5]+"))
                    .filter(x -> x.length() >= 2).anyMatch(corpus::contains);
        }).limit(3).toList();
    }

}
