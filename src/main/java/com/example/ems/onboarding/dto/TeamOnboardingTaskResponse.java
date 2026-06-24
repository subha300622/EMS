package com.example.ems.onboarding.dto;

import java.time.LocalDate;

public record TeamOnboardingTaskResponse(
    Long taskId,
    String title,
    String phase,
    String owner,
    String status,
    LocalDate dueDate,
    String estimatedTime
) {}
