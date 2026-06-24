package com.example.ems.onboarding.dto;

public record TeamOnboardingCreateResponse(
    Long onboardingId,
    String status,
    String message
) {}
