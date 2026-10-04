package com.dataengine.governance.api;

import com.dataengine.governance.domain.ChangeType;
import jakarta.validation.constraints.*;

public record CreateChangeRequest(
        @NotBlank @Size(max = 160) String title,
        @NotBlank @Size(max = 4000) String description,
        @NotNull ChangeType type,
        @NotBlank String owner,
        @NotBlank String targetEnvironment,
        @NotBlank String assetQualifiedName,
        @Pattern(regexp = "P[0-3]") String assetTier,
        String beforeSchema,
        String afterSchema,
        @Min(0) int consumerCount,
        boolean destructive,
        boolean recoveryPlanPresent
) {}
