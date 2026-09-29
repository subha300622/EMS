package com.example.ems.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Token verification response")
public record VerifyTokenResponse(
    @Schema(description = "Whether the token is valid", example = "true") boolean valid,
    @Schema(description = "Employee ID", example = "EMP001") String employeeId,
    @Schema(description = "Work email", example = "john@example.com") String email
) {}
