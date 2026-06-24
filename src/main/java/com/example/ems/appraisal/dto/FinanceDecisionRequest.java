package com.example.ems.appraisal.dto;

import jakarta.validation.constraints.NotBlank;

public record FinanceDecisionRequest(
    @NotBlank(message = "Comments are required")
    String comments
) {}
