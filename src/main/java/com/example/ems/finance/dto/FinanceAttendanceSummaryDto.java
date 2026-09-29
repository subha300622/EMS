package com.example.ems.finance.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Attendance summary for finance dashboard")
public record FinanceAttendanceSummaryDto(
        @Schema(description = "Attendance percentage for current month", example = "94.0")
        Double percentage,

        @Schema(description = "Total days present in current month", example = "22")
        Integer presentDays,

        @Schema(description = "Total working days in current month", example = "24")
        Integer workingDays,

        @Schema(description = "Month-over-month percentage change", example = "2.1")
        Double changePercentage
) {}
