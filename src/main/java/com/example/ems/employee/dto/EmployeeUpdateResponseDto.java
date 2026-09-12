package com.example.ems.employee.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Employee update confirmation")
public record EmployeeUpdateResponseDto(
    @Schema(description = "Employee Code", example = "EMP101")
    String employeeId,
    @Schema(description = "Full Name", example = "Jane Doe")
    String fullName,
    @Schema(description = "Email", example = "jane.doe@example.com")
    String email,
    @Schema(description = "Employment Status", example = "ACTIVE")
    String employmentStatus
) {}
