package com.example.ems.asset.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Schema(description = "Request body for creating asset maintenance request")
public record CreateMaintenanceRequest(
    @Schema(description = "Description of maintenance issue", example = "Screen replacement needed")
    @NotBlank(message = "Issue is required")
    String issue,

    @Schema(description = "Vendor handling the maintenance", example = "Dell Service Center")
    @NotBlank(message = "Vendor is required")
    String vendor,

    @Schema(description = "Estimated cost of maintenance", example = "150.00")
    @NotNull(message = "Estimated cost is required")
    BigDecimal estimatedCost
) {}
