package com.example.ems.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "User organization context response")
public record OrgContextSummaryDto(
    @Schema(description = "User ID", example = "USR-001") String userId,
    @Schema(description = "Organization ID", example = "1") String organizationId,
    @Schema(description = "Organization name", example = "Acme Corp") String organizationName,
    @Schema(description = "Scope", example = "ORGANIZATION") String scope
) {}
