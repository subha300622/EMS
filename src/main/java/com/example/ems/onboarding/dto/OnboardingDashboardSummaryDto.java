package com.example.ems.onboarding.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "HR Onboarding dashboard summary statistics")
public record OnboardingDashboardSummaryDto(
        @Schema(description = "Total active onboardings", example = "12")
        long activeOnboardings,

        @Schema(description = "Onboardings completed this month", example = "5")
        long completedThisMonth,

        @Schema(description = "Total overdue tasks across onboardings", example = "3")
        long tasksOverdue,

        @Schema(description = "Candidates joining this current week", example = "4")
        long joiningThisWeek,

        @Schema(description = "Pending verification documents", example = "8")
        long pendingDocuments,

        @Schema(description = "Average onboarding completion time in days", example = "14")
        int avgCompletionTimeDays
) {}
