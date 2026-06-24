package com.example.ems.appraisal.dto;

import jakarta.validation.constraints.NotBlank;

public record AttendanceJustificationRequest(
    @NotBlank(message = "Reason is required")
    String reason
) {}
