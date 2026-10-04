package com.dataengine.governance.service;

import com.dataengine.governance.domain.*;
import org.springframework.stereotype.Service;

@Service
public class RiskEngine {
    public RiskAssessment assess(DataChange change) {
        int score = switch (change.getAssetTier()) {
            case "P0" -> 22;
            case "P1" -> 16;
            case "P2" -> 9;
            default -> 4;
        };
        if (change.isDestructive()) score += 25;
        score += Math.min(15, change.getConsumerCount() * 2);
        score += switch (change.getType()) {
            case SEMANTIC, METRIC -> 18;
            case SCHEMA, SOURCE -> 14;
            case PIPELINE -> 11;
            default -> 7;
        };
        if (!change.isRecoveryPlanPresent()) score += 12;
        score = Math.min(100, score);
        RiskLevel level = score >= 80 ? RiskLevel.CRITICAL
                : score >= 60 ? RiskLevel.HIGH
                : score >= 35 ? RiskLevel.MEDIUM : RiskLevel.LOW;
        return new RiskAssessment(score, level);
    }

    public record RiskAssessment(int score, RiskLevel level) {}
}
