# Data Change Governance Platform

企业数据变更治理与智能执行平台的可运行实现。项目已完成 Phase 1 治理闭环、Phase 2 生产执行语义和 Phase 3 智能治理：受约束 Skills、版本化 RAG、Candidate Evidence Verification、Golden/Bad Case Replay 与智能能力评测。

业务能力、边界、接口与后续收口清单见 [项目说明文档](docs/PROJECT-GUIDE.md)。

## 快速启动

环境要求：Java 21+、Maven 3.9+。

```powershell
mvn spring-boot:run
```

浏览器打开 <http://localhost:8080>。系统默认使用 H2 内存数据库并自动装载演示数据，不要求预装 PostgreSQL、Kafka 或 Temporal。

## 测试

```powershell
mvn test
```

## 生产数据库

设置环境变量后启用 `prod` Profile：

```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/change_governance"
$env:DB_USERNAME="change_governance"
$env:DB_PASSWORD="change_governance"
mvn spring-boot:run "-Dspring-boot.run.profiles=prod"
```

## 已实现能力

- Data Change 生命周期与受控状态迁移
- 基于资产等级、破坏性、消费者、证据完整性的确定性风险评分
- Confirmed 与 Candidate 影响结果分离
- Contract、Schema、Quality、Recovery 门禁
- 审批、Shadow、Canary、Promote 与 Recovery 演示闭环
- Evidence、状态历史和执行回执式审计记录
- SHA-256 不可变 Release Bundle，审批后冻结执行上下文
- Connector Gateway 路由、幂等键、统一 Attempt Budget、资源版本和 Trace ID
- Shadow、Canary、Promote、Backfill、Recovery 的持久化 Execution Receipt
- 运行时连接器健康与执行成功率、P95、恢复演练 SLO 页面
- Requirement、Impact、Quality、Incident 四类治理 Skill
- 本地确定性智能 Provider；无需联网、模型服务或 API Key
- 版本化治理知识库与带引用的 Candidate Finding
- 人工验证/驳回流程，Agent 结论不会直接写入正式事实
- Golden/Bad Case Replay Regression 与通过率看板
- 中文治理工作台和 REST API
- 非法状态迁移、缺少恢复计划、门禁失败的服务层测试

## 工程边界

当前版本是模块化单体，并提供本地安全执行器。Temporal Worker 与真实 Kafka/Flink/Database/BI Connector 通过现有执行边界替换接入；默认配置不产生生产副作用。运行约束见 [Phase 2 运行手册](docs/PHASE2-RUNBOOK.md)。

智能能力默认使用 `LOCAL_DETERMINISTIC` Provider。后续接入 Ollama、企业内网模型或任意外部模型时，只需实现 `GovernanceIntelligenceProvider`，不改变候选事实、验证和评测流程。详细约束见 [Phase 3 智能治理说明](docs/PHASE3-INTELLIGENCE.md)。

## 企业集成与生产启用

资产目录、字段级确认/候选血缘、Data Contract、Schema 兼容性检查和多级审批任务均已内置。OpenMetadata、OpenLineage、Temporal、Kafka、Flink 和 Vault 由配置目录统一管理；默认 `LOCAL_SAFE`，不会主动连接任何外部系统。启用前需提供各系统地址与凭证引用，见 [企业集成清单](docs/ENTERPRISE-INTEGRATION.md)。
