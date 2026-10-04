package com.dataengine.governance.config;

import com.dataengine.governance.api.ActionRequest;
import com.dataengine.governance.api.CreateChangeRequest;
import com.dataengine.governance.domain.*;
import com.dataengine.governance.service.ChangeGovernanceService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DemoDataLoader {
    @Bean
    CommandLineRunner demoData(ChangeGovernanceService service) {
        return args -> {
            ActionRequest system = new ActionRequest("demo.system", "演示轨迹自动生成");

            DataChange completed = service.create(new CreateChangeRequest(
                    "订单金额精度升级", "将 order_amount 从 decimal(12,2) 升级为 decimal(18,2)，保持 CNY 语义不变",
                    ChangeType.SCHEMA, "li.ming", "PROD", "warehouse.dwd_order", "P0",
                    "order_amount decimal(12,2) not null", "order_amount decimal(18,2) not null",
                    8, false, true));
            service.analyze(completed.getId(), system); service.validate(completed.getId(), system);
            service.submit(completed.getId(), system); service.approve(completed.getId(), system);
            service.shadow(completed.getId(), system); service.canary(completed.getId(), system);
            service.promote(completed.getId(), system); service.complete(completed.getId(), system);

            DataChange approval = service.create(new CreateChangeRequest(
                    "GMV 指标切换含税口径", "财务 GMV 从未税金额切换为含税金额，并重算最近 30 天分区",
                    ChangeType.SEMANTIC, "wang.finance", "PROD", "metric.gmv", "P0",
                    "semantic: net_amount", "semantic: gross_amount_with_tax",
                    12, true, true));
            service.analyze(approval.getId(), system); service.validate(approval.getId(), system);
            service.submit(approval.getId(), system);

            DataChange draft = service.create(new CreateChangeRequest(
                    "订单事件增加营销渠道字段", "Kafka order event 新增 nullable marketing_channel 字段",
                    ChangeType.SCHEMA, "chen.data", "DEV", "kafka.order_event", "P2",
                    "fields: [order_id, amount]", "fields: [order_id, amount, marketing_channel?]",
                    2, false, false));

            DataChange shadow = service.create(new CreateChangeRequest(
                    "支付明细 Flink 作业窗口优化", "将支付聚合窗口从 10 分钟调整为 5 分钟并验证延迟与对账",
                    ChangeType.PIPELINE, "zhao.stream", "PROD", "flink.payment_aggregate", "P1",
                    "window: 10m", "window: 5m", 6, false, true));
            service.analyze(shadow.getId(), system); service.validate(shadow.getId(), system);
            service.submit(shadow.getId(), system); service.approve(shadow.getId(), system);
            service.shadow(shadow.getId(), system);
        };
    }
}
