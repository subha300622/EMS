package com.example.ems.employee.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Employee status details")
public record EmployeeStatusResponseDto(
    @Schema(description = "Employee Code", example = "EMP101")
    String employeeId,
    @Schema(description = "Status", example = "ACTIVE")
    String status,
    @Schema(description = "Employment Status", example = "ACTIVE")
    String employmentStatus,
    @Schema(description = "User Account Status", example = "ACTIVE")
    String userAccountStatus
) {}
