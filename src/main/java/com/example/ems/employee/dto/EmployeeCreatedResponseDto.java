package com.example.ems.employee.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Schema(description = "Employee creation response details")
public record EmployeeCreatedResponseDto(
    @Schema(description = "Employee numeric ID", example = "101")
    Long id,
    @Schema(description = "Employee Code", example = "EMP-00101")
    String employeeId,
    @Schema(description = "Organization ID", example = "1")
    Long organizationId,
    @Schema(description = "Full Name", example = "Jane Doe")
    String fullName,
    @Schema(description = "Work Email", example = "jane.doe@example.com")
    String email,
    @Schema(description = "Department", example = "Engineering")
    String department,
    @Schema(description = "Designation", example = "Senior Software Engineer")
    String designation,
    @Schema(description = "Joining Date", example = "2025-01-15")
    LocalDate joiningDate,
    @Schema(description = "Status", example = "ACTIVE")
    String status
) {}
