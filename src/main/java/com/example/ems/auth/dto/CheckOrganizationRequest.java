package com.example.ems.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request body for checking organization name availability")
public record CheckOrganizationRequest(
    @Schema(description = "Organization name to check", example = "Acme Corp")
    @NotBlank(message = "Organization name is required")
    String orgName,

    @Schema(description = "Optional organization code", example = "ACME")
    String organizationCode
) {}
