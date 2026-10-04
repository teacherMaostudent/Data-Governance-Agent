package com.dataengine.governance.api;
import jakarta.validation.constraints.NotBlank;
public record CreateContractRequest(@NotBlank String assetQualifiedName, @NotBlank String version,
                                    @NotBlank String schemaDefinition, @NotBlank String semanticDefinition,
                                    @NotBlank String qualityPolicy, boolean requireBackwardCompatibility,
                                    @NotBlank String owner) {}
