package com.example.ems.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resend OTP response")
public record ResendOtpResponse(
    @Schema(description = "Generated OTP code", example = "123456") String otp
) {}
