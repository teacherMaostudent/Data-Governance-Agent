package com.dataengine.governance.service;

import com.dataengine.governance.domain.*;

public interface GovernanceIntelligenceProvider {
    String name();
    String modelVersion();
    String reason(SkillType type, String input, DataChange change);
}
