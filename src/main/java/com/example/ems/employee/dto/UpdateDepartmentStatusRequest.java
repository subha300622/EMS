package com.example.ems.employee.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request body for updating department status")
public record UpdateDepartmentStatusRequest(
    @Schema(description = "Department status (Active or Inactive)", example = "Active")
    @NotBlank(message = "Status is required")
    String status
) {}
