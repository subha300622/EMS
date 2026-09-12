package com.example.ems.asset.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request body for transferring an asset to another employee")
public record TransferAssetRequest(
    @Schema(description = "Target employee ID", example = "102")
    @NotNull(message = "Target employee ID is required")
    Long toEmployeeId,

    @Schema(description = "Transfer remarks", example = "Transferred to new team member")
    String remarks,

    @Schema(description = "Transfer reason", example = "Role transition")
    String reason
) {
    public String getEffectiveRemarks() {
        if (remarks != null && !remarks.isBlank()) {
            return remarks;
        }
        if (reason != null && !reason.isBlank()) {
            return reason;
        }
        return "Transferred via Admin API";
    }
}
