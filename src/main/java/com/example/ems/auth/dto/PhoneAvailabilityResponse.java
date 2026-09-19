package com.example.ems.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Phone number availability response")
public record PhoneAvailabilityResponse(
    @Schema(description = "Whether the phone number is available", example = "true") boolean available,
    @Schema(description = "Normalized phone number", example = "+919876543210") String normalizedPhone
) {}
