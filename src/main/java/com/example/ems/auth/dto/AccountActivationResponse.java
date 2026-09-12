package com.example.ems.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Account activation response")
public record AccountActivationResponse(
    @Schema(description = "Employee ID", example = "EMP001") String employeeId,
    @Schema(description = "Account status", example = "ACTIVE") String status
) {}
