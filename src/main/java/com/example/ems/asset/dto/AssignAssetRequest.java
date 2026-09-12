package com.example.ems.asset.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Schema(description = "Request body for assigning an asset to an employee")
public record AssignAssetRequest(
    @Schema(description = "ID of the employee to assign asset to", example = "101")
    @NotNull(message = "Employee ID is required")
    Long employeeId,

    @Schema(description = "Expected return date", example = "2026-12-31")
    LocalDate expectedReturnDate,

    @Schema(description = "Assignment notes or remarks", example = "Assigned for development work")
    String notes
) {}
