package com.dataengine.governance.service;

import com.dataengine.governance.domain.*;
import org.springframework.stereotype.Component;
import java.util.Locale;

@Component
public class LocalDeterministicIntelligenceProvider implements GovernanceIntelligenceProvider {
    @Override public String name() { return "LOCAL_DETERMINISTIC"; }
    @Override public String modelVersion() { return "governance-rules-3.0"; }

    @Override
    public String reason(SkillType type, String input, DataChange change) {
        String lower = input.toLowerCase(Locale.ROOT);
        return switch (type) {
            case REQUIREMENT -> "需求候选：明确目标资产、业务口径、验收窗口与责任人；" +
                    (lower.contains("金额") || lower.contains("gmv") ? "金额精度和含税语义必须显式声明" : "补充上下游兼容范围");
            case IMPACT -> "影响候选：除正式血缘外，重点核查离线导出、月报与外部消费者；候选依赖必须经 Evidence Verification";
            case QUALITY -> "质量建议：校验行数、空值率、唯一性、金额守恒、业务指标差异与 P95 延迟，并设置 Shadow 观察窗口";
            case INCIDENT -> "事故候选：按 Trace ID 关联 Connector Receipt，检查重复执行、版本漂移和水位线；恢复采用停止、恢复、清理、Replay、重算验证";
        };
    }
}
