package com.example.ems.organization.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request body for updating organization status (SUSPENDED or ACTIVE)")
public record UpdateOrganizationStatusRequest(
    @Schema(description = "Target status (SUSPENDED or ACTIVE)", example = "SUSPENDED")
    @NotBlank(message = "Status is required")
    String status,

    @Schema(description = "Reason for status change", example = "Non-payment of subscription fee")
    String reason
) {
    public String getEffectiveReason() {
        return (reason != null && !reason.isBlank()) ? reason : "No reason provided";
    }
}
