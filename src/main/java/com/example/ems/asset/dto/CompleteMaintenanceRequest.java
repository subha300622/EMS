package com.example.ems.asset.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "Request body for completing asset maintenance")
public record CompleteMaintenanceRequest(
    @Schema(description = "Actual cost incurred", example = "145.50")
    BigDecimal actualCost
) {}
