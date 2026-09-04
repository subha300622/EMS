package com.example.ems.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request body for verifying email token")
public record VerifyEmailRequest(
    @Schema(description = "Verification token", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
    @NotBlank(message = "Token is required")
    String token
) {}
