package com.example.ems.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Organization name availability response")
public record AvailabilityCheckResponse(
    @Schema(description = "Whether the name is available", example = "true") boolean available,
    @Schema(description = "Normalized organization name", example = "acme-corp") String normalizedName
) {}
