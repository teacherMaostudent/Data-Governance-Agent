package com.dataengine.governance.api;

import jakarta.validation.constraints.NotBlank;

public record ActionRequest(@NotBlank String actor, String reason) {
    public String safeReason(String fallback) {
        return reason == null || reason.isBlank() ? fallback : reason.trim();
    }
}
