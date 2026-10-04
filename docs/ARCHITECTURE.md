# 企业数据变更治理平台 MVP 架构

## 设计结论

MVP 采用模块化单体承载控制面，并把会产生副作用的执行能力保持为明确接口边界。这样既保留设计文档中的事实优先、控制执行分离、版本化、证据化和失败可恢复原则，也避免在验证业务闭环前引入过多基础设施。

## 模块边界

| 模块 | 当前实现 | 后续替换或扩展 |
| --- | --- | --- |
| Change Control | Data Change、状态机、状态历史 | 独立控制面服务 |
| Impact and Risk | 确定性影响集合、候选集合、风险评分 | OpenMetadata、OpenLineage、SQL AST |
| Contract and Quality | 兼容性与质量门禁 | Schema Registry、质量计算引擎 |
| Policy | 状态迁移要求、审批和恢复约束 | OPA 或版本化策略服务 |
| Execution | Release Bundle、幂等 Receipt、Attempt Budget、本地安全 Connector | Temporal Worker 与生产 Connector |
| Evidence and Audit | Evidence、状态历史、Receipt、Trace ID | 对象存储、WORM 审计、OpenSearch |
| Portal | 单页治理工作台 | 独立前端应用 |
| Intelligence | 四类 Skill、版本化 RAG、Candidate Finding、Replay Eval | Ollama、内网模型或外部模型 Provider |

## 安全不变量

Agent 建议不得直接变更正式事实或状态。智能输出首先保存为 `CANDIDATE`，必须具备知识引用并经过人工验证才能生成正式 Evidence。所有状态变化由服务层状态机校验；生产提升必须经过质量门禁、审批、Release Bundle 冻结、Shadow、Canary，并且高风险变更必须具备 Recovery Plan。所有执行先生成幂等键并受统一 Attempt Budget 约束；默认 Connector 处于 `LOCAL_SAFE` 模式。

## 数据模型

`data_change` 是聚合根；`release_bundle` 保存审批时冻结的不可变清单及 SHA-256；`execution_receipt` 保存动作、幂等键、Connector、Attempt、资源版本、Trace ID、结果与耗时；`evidence` 和 `change_state_history` 构成审计轨迹。

## 生产化路线

1. 接入 PostgreSQL、OIDC 与资产级 RBAC/ABAC。
2. 接入 OpenMetadata/OpenLineage 和 SQL AST，替换演示影响分析器。
3. 引入 Temporal，将长时验证、审批等待、Shadow、Canary、Backfill 和 Recovery 编排外置。
4. 建立 Connector Gateway、幂等键、Attempt Budget、Execution Receipt 和 Vault 凭证引用。
5. 引入 Kafka 事件、OpenSearch、MinIO、OpenTelemetry，并完成故障注入与 Replay Regression。
