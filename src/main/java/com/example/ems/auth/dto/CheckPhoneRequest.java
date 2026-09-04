package com.example.ems.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request body for checking mobile number availability")
public record CheckPhoneRequest(
    @Schema(description = "Mobile phone number to check", example = "+1234567890")
    @NotBlank(message = "Mobile number is required")
    String mobileNumber
) {}
