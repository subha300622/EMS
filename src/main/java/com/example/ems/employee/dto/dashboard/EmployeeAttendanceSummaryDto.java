package com.example.ems.employee.dto.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Attendance summary for employee dashboard")
public record EmployeeAttendanceSummaryDto(
        @Schema(description = "Attendance percentage for current period", example = "92.0")
        Double percentage,

        @Schema(description = "Total days present in the period", example = "22")
        Integer presentDays,

        @Schema(description = "Total working days in the period", example = "24")
        Integer workingDays,

        @Schema(description = "Trend change percentage compared to prior period", example = "2.1")
        Double trendPercentage,

        @Schema(description = "Trend direction (UP, DOWN, STABLE)", example = "UP")
        String trendDirection
) {
}
