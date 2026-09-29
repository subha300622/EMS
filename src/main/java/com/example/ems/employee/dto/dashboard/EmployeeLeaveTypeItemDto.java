package com.example.ems.employee.dto.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Leave balance detail for a specific leave type")
public record EmployeeLeaveTypeItemDto(
        @Schema(description = "Leave type short code", example = "CL")
        String code,

        @Schema(description = "Leave type display name", example = "Casual Leave")
        String name,

        @Schema(description = "Available leave balance in days", example = "6.0")
        Double available
) {
}
