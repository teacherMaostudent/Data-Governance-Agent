package com.dataengine.governance.domain;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "evaluation_case")
public class EvaluationCase {
    @Id private UUID id;
    @Column(nullable = false, unique = true) private String name;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private SkillType skillType;
    @Column(nullable = false, length = 2000) private String input;
    @Column(nullable = false) private String expectedSignals;
    @Column(nullable = false) private boolean badCase;

    protected EvaluationCase() {}
    public EvaluationCase(String name, SkillType skillType, String input, String expectedSignals, boolean badCase) {
        this.id = UUID.randomUUID(); this.name = name; this.skillType = skillType; this.input = input;
        this.expectedSignals = expectedSignals; this.badCase = badCase;
    }
    public UUID getId() { return id; }
    public String getName() { return name; }
    public SkillType getSkillType() { return skillType; }
    public String getInput() { return input; }
    public String getExpectedSignals() { return expectedSignals; }
    public boolean isBadCase() { return badCase; }
}
