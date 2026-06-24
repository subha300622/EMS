package com.example.ems.onboarding.dto;

import java.time.LocalDate;

public record TeamOnboardingCreateRequest(
    Long employeeId,
    Long managerId,
    LocalDate joiningDate
) {}
