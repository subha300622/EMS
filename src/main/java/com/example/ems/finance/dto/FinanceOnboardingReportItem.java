package com.example.ems.finance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Schema(description = "Finance onboarding report record item")
public record FinanceOnboardingReportItem(
        @Schema(description = "Finance onboarding ID", example = "1")
        Long onboardingId,

        @Schema(description = "Employee code/ID", example = "EMP-001")
        String employeeId,

        @Schema(description = "Employee full name", example = "John Doe")
        String employeeName,

        @Schema(description = "Employee work email", example = "john.doe@company.com")
        String employeeEmail,

        @Schema(description = "Date of joining", example = "2024-01-15")
        LocalDate joiningDate,

        @Schema(description = "Onboarding status", example = "APPROVED")
        String status,

        @Schema(description = "Whether bank details are verified", example = "true")
        boolean bankVerified,

        @Schema(description = "Whether PAN details are verified", example = "true")
        boolean panVerified,

        @Schema(description = "Whether UAN details are verified", example = "true")
        boolean uanVerified,

        @Schema(description = "Whether salary structure is assigned", example = "true")
        Boolean salaryStructureAssigned,

        @Schema(description = "Whether payroll is activated", example = "true")
        Boolean payrollActivated
) {}
