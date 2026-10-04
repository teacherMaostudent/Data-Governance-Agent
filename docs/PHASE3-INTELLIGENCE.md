# Phase 3 智能治理说明

## 本地优先

默认 Provider 为 `LOCAL_DETERMINISTIC`，用确定性规则和词法检索提供可重复的智能治理结果，不需要联网或 API Key。`GovernanceIntelligenceProvider` 是唯一的推理替换接口，可接 Ollama、企业内网模型或其他模型服务。

## 四类 Skill

- Requirement：提取资产范围、业务口径、验收信号和责任边界。
- Impact：发现正式血缘之外的长尾候选依赖。
- Quality：建议 Shadow/Canary 对账维度、阈值与观察窗口。
- Incident：关联 Trace ID、Execution Receipt、故障模式和 Recovery 步骤。

## 不可越过的边界

Agent 输出只保存为 `AgentFinding(CANDIDATE)`，不能修改 Data Change 状态、confirmed lineage 或发布门禁。候选结论必须带版本化知识引用，并由人员执行 Verify 或 Reject。Verify 后平台生成独立 `AGENT_VERIFIED` Evidence，原始候选仍可回放。

## 评测闭环

系统内置 Golden Case 与 Bad Case。Replay Regression 对四类能力和两类安全反例进行批量重放，保存 Provider、输出、匹配得分、通过状态和时间。替换 Provider 后应先运行 Replay，禁止只凭主观体验上线。

## API

- `GET /v1/intelligence/overview`
- `POST /v1/intelligence/data-changes/{id}/analyze`
- `POST /v1/intelligence/data-changes/{id}/skills/{skill}`
- `POST /v1/intelligence/findings/{id}/verify`
- `POST /v1/intelligence/findings/{id}/reject`
- `POST /v1/intelligence/replay`
