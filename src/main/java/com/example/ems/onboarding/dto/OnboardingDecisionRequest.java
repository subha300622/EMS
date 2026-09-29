package com.example.ems.onboarding.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request body for onboarding policy approval/rejection/send-back actions")
public record OnboardingDecisionRequest(
    @Schema(description = "Remarks or justification for the action", example = "Documents verified and approved")
    String remarks
) {
    public String getEffectiveRemarks(String defaultRemarks) {
        return (remarks != null && !remarks.isBlank()) ? remarks : defaultRemarks;
    }
}
