package com.example.ems.employee.dto.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Detailed employee attendance summary response")
public record EmployeeAttendanceDetailSummaryDto(
        @Schema(description = "Summary evaluation period")
        EmployeeAttendancePeriodDto period,

        @Schema(description = "Working days in period", example = "24")
        Integer workingDays,

        @Schema(description = "Present days in period", example = "22")
        Integer presentDays,

        @Schema(description = "Attendance percentage", example = "92.0")
        Double attendancePercentage,

        @Schema(description = "Trend change percentage compared to prior period", example = "2.1")
        Double trendPercentage,

        @Schema(description = "Trend direction (UP, DOWN, STABLE)", example = "UP")
        String trendDirection
) {
}
