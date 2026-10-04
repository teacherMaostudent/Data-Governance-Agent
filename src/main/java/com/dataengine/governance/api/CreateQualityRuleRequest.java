package com.dataengine.governance.api;
import com.dataengine.governance.domain.GateStatus; import jakarta.validation.constraints.*;
public record CreateQualityRuleRequest(@NotBlank String assetQualifiedName,@NotBlank String name,@NotBlank String metric,@DecimalMin("0.0") double threshold,@NotNull GateStatus severity,@NotBlank String owner) {}
