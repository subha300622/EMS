package com.example.ems.onboarding.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Onboarding timeline event item")
public record OnboardingTimelineEventDto(
        @Schema(description = "Date and time of event", example = "2024-01-15T09:30:00")
        String date,

        @Schema(description = "Type of timeline event", example = "INITIALIZED")
        String type,

        @Schema(description = "Title of timeline event", example = "Onboarding Initialized")
        String title,

        @Schema(description = "Detailed event description", example = "Onboarding process initialized for employee")
        String description
) {}
