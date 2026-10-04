package com.dataengine.governance.api;
import jakarta.validation.constraints.DecimalMin; import jakarta.validation.constraints.NotBlank;
public record EvaluateQualityRequest(@DecimalMin("0.0") double observedValue,@NotBlank String actor) {}
