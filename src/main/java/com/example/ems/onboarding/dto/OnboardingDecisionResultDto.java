package com.example.ems.onboarding.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Onboarding approval decision response")
public record OnboardingDecisionResultDto(
        @Schema(description = "Onboarding ID", example = "101")
        Long onboardingId,

        @Schema(description = "Onboarding decision status", example = "APPROVED")
        String status,

        @Schema(description = "Decision remarks or notes", example = "Approved via Approval Policy Engine")
        String remarks
) {}
