package com.example.ems.training.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request body for completing a training assignment")
public record CompleteTrainingRequest(
    @Schema(description = "Employee ID completing the training", example = "101")
    @NotNull(message = "Employee ID is required")
    Long employeeId
) {}
