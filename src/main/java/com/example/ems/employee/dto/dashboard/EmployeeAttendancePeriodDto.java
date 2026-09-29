package com.example.ems.employee.dto.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Date period boundary for attendance")
public record EmployeeAttendancePeriodDto(
        @Schema(description = "Period start date", example = "2026-09-01")
        String startDate,

        @Schema(description = "Period end date", example = "2026-09-24")
        String endDate
) {
}
