package com.example.ems.training.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Training Employee Summary Response")
public record TrainingEmployeeSummaryResponse(
    @Schema(description = "Database ID", example = "1")
    Long id,
    @Schema(description = "Employee Code", example = "EMP001")
    String employeeId,
    @Schema(description = "Full name", example = "John Doe")
    String fullName,
    @Schema(description = "Email", example = "john.doe@example.com")
    String email
) {}
