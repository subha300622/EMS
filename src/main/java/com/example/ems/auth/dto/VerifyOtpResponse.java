package com.example.ems.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "OTP verification response")
public record VerifyOtpResponse(
    @Schema(description = "Password reset token", example = "550e8400-e29b-41d4-a716-446655440000") String resetToken,
    @Schema(description = "Token validity in seconds", example = "600") int expiresIn
) {}
