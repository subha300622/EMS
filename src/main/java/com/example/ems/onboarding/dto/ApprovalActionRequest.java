package com.example.ems.onboarding.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request body for handling generic entity approval actions")
public record ApprovalActionRequest(
    @Schema(description = "Type of entity to approve (e.g. ONBOARDING, FINANCE)", example = "ONBOARDING")
    @NotBlank(message = "entityType is required")
    String entityType,

    @Schema(description = "ID of the target entity", example = "101")
    @NotNull(message = "entityId is required")
    Long entityId,

    @Schema(description = "Approval action to perform (e.g. APPROVE, COMPLETE, REJECT)", example = "APPROVE")
    @NotBlank(message = "action is required")
    String action,

    @Schema(description = "Optional approval notes or comments", example = "Approved by manager")
    String notes
) {
    public String getEffectiveNotes() {
        return (notes != null && !notes.isBlank()) ? notes : "Action processed by approvals engine";
    }
}
