package com.dataengine.governance.config;
import com.dataengine.governance.domain.*; import com.dataengine.governance.repository.*; import org.springframework.boot.CommandLineRunner; import org.springframework.context.annotation.*;
@Configuration public class CatalogDataLoader {
 @Bean CommandLineRunner catalogData(DataAssetRepository assets,LineageEdgeRepository edges,DataContractRepository contracts){return args->{if(assets.count()>0)return;
  assets.save(new DataAsset("warehouse.dwd_order",AssetType.TABLE,"li.ming","P0","CONFIDENTIAL")); assets.save(new DataAsset("warehouse.dws_sales_daily",AssetType.TABLE,"analytics.team","P1","INTERNAL")); assets.save(new DataAsset("metric.gmv",AssetType.METRIC,"wang.finance","P0","CONFIDENTIAL")); assets.save(new DataAsset("bi.finance_overview",AssetType.DASHBOARD,"finance.bi","P1","INTERNAL"));
  edges.save(new LineageEdge("warehouse.dwd_order","warehouse.dws_sales_daily","FIELD_TRANSFORM",.98,LineageStatus.CONFIRMED,"openlineage:demo-001")); edges.save(new LineageEdge("warehouse.dws_sales_daily","metric.gmv","METRIC_INPUT",.97,LineageStatus.CONFIRMED,"openmetadata:demo-002")); edges.save(new LineageEdge("metric.gmv","bi.finance_overview","DASHBOARD_INPUT",.96,LineageStatus.CONFIRMED,"bi:demo-003")); edges.save(new LineageEdge("warehouse.dwd_order","external.monthly_export","INFERRED",.61,LineageStatus.CANDIDATE,"agent:demo-004"));
  contracts.save(new DataContract("warehouse.dwd_order","v2","order_id bigint primary key; order_amount decimal(18,2) not null","CNY, tax exclusive","row_count; null_rate; amount_conservation",true));
 };}
}
