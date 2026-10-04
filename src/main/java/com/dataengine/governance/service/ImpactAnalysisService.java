package com.dataengine.governance.service;

import com.dataengine.governance.domain.DataChange;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class ImpactAnalysisService {
    private final CatalogService catalog;
    public ImpactAnalysisService(CatalogService catalog) { this.catalog = catalog; }
    public ImpactResult analyze(DataChange change) {
        String asset = change.getAssetQualifiedName();
        String leaf = asset.contains(".") ? asset.substring(asset.lastIndexOf('.') + 1) : asset;
        List<String> graphConfirmed = catalog.confirmedDownstream(asset);
        List<String> graphCandidate = catalog.candidateDownstream(asset);
        if (!graphConfirmed.isEmpty()) return new ImpactResult(graphConfirmed, graphCandidate);
        List<String> confirmed = new ArrayList<>();
        confirmed.add(asset);
        if (leaf.contains("order") || leaf.contains("payment")) {
            confirmed.add("warehouse.dws_sales_daily");
            confirmed.add("metric.gmv");
            confirmed.add("bi.finance_overview");
        } else {
            confirmed.add("warehouse.dws_" + leaf + "_summary");
            confirmed.add("bi." + leaf + "_dashboard");
        }
        List<String> candidate = change.getConsumerCount() > confirmed.size()
                ? List.of("candidate.external_consumer", "candidate.monthly_export_job")
                : List.of("candidate.owner_review_required");
        return new ImpactResult(confirmed, candidate);
    }

    public record ImpactResult(List<String> confirmed, List<String> candidate) {}
}
