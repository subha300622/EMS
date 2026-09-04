package com.example.ems.employee.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request body for updating an employee's status")
public record UpdateEmployeeStatusRequest(
    @Schema(description = "New status of the employee (e.g. Active, Inactive, Suspended, Terminated)", example = "Inactive")
    @NotBlank(message = "Status is required")
    String status,

    @Schema(description = "Reason for status change", example = "Resignation")
    String reason
) {}
