# 企业集成清单

## 已实现的控制面业务

- DataAsset 资产目录：资产类型、Owner、Tier、数据分级。
- LineageEdge 血缘：confirmed/candidate、置信度、证据引用和验证时间。
- DataContract：Schema、语义、质量策略、兼容性要求。
- Schema Compatibility：字段删除、精度收窄、Nullable 收紧、主键移除、枚举收缩和时间精度丢失检测。
- ApprovalTask：Owner 与高风险治理管理员双阶段审批任务、角色、时限和决策留痕。
- Integration Catalog：统一暴露 OpenMetadata、OpenLineage、Temporal、Kafka、Flink、Vault 的启用状态。

## 外部系统接入前置条件

真实连接不能使用演示数据替代，需由平台管理员提供：

| 系统 | 必需配置 | 启用后承担的业务 |
| --- | --- | --- |
| OIDC | issuer、JWKS、audience、角色映射 | 登录、RBAC/ABAC、审批身份 |
| OpenMetadata/DataHub | 服务地址、服务账号或 Vault 引用 | 资产同步、Owner、标签、消费方 |
| OpenLineage | 事件入口与认证引用 | 运行时字段级血缘与影响更新 |
| Temporal | 服务地址、namespace、任务队列、凭证引用 | 审批等待、Shadow、Canary、Backfill、Recovery 编排 |
| Kafka | Bootstrap、Topic ACL、凭证引用 | 变更事件、执行事件、告警事件 |
| Flink | REST 地址、集群身份、凭证引用 | Savepoint、作业发布、观察和恢复 |
| Vault | 地址、认证角色、路径约定 | Connector 与外部系统凭证解析 |
| 对象存储/OpenSearch/OTel | Endpoint、bucket/index、Exporter | Evidence 原文、审计检索、Trace/Metric/Alert |

配置入口为 `governance.integrations.endpoints`。建议把 `credential-ref` 写为 Vault 路径或工作负载身份引用，绝不把密码写进版本库。

## 仍需外部授权的动作

代码已经具备连接配置和控制面业务；但实际连接生产资源、建立 OIDC 客户端、创建 Kafka Topic/ACL、部署 Temporal Worker 或修改 Flink 作业需要目标系统的管理员授权与具体连接信息。提供这些信息后可继续完成真实适配器与联调。
