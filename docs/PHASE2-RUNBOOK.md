# Phase 2 运行手册

## 执行安全约束

1. 审批动作冻结 Release Bundle；后续执行只引用其 SHA-256 摘要。
2. 幂等键由 `changeId + bundleDigest + action` 生成，相同动作重放返回原 Receipt。
3. Connector 的统一 Attempt Budget 为 3，调用方不得叠加独立重试。
4. 每次执行记录 Connector、资源版本、Trace ID、耗时和结果。
5. 高风险变更没有 Recovery Plan 时不得提交审批。

## 默认运行模式

`LOCAL_SAFE` Connector 模拟 Database/Hive、Kafka、Flink 和 BI/Metric 的协议语义，只写控制面与回执，不触达生产资源。真实 Connector 应实现相同的输入、幂等和回执约束，并从 Vault 类系统获取凭证。

## 恢复演练

Recovery 依次表达停止新版本、恢复旧版本、清理中间产物、Replay/Backfill、数据与指标重算验证。演练后应在“可观测性”页面确认恢复演练 SLO 为 PASS，并使用 Trace ID 定位完整证据链。

## 生产替换点

- 将 `ExecutionRuntimeService` 的同步活动提交替换为 Temporal Workflow/Activity。
- 保留 Release Bundle、幂等键、Attempt Budget 和 Execution Receipt 数据契约。
- Connector Gateway 保持无状态；真实资源版本必须由目标系统回传，不可由调用方猜测。
- 将 Evidence 原文与对账报告写入版本化对象存储，并把索引投递至 OpenSearch。
