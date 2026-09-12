package com.example.ems.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Email availability response")
public record EmailAvailabilityResponse(
    @Schema(description = "Whether the email is available", example = "true") boolean available,
    @Schema(description = "Normalized email", example = "john@example.com") String normalizedEmail
) {}
