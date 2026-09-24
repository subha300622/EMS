package com.example.ems.employee.dto.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "Compensation and CTC summary")
public record EmployeeCompensationSummaryDto(
        @Schema(description = "Current annual or periodic CTC", example = "1800000")
        BigDecimal currentCtc,

        @Schema(description = "Currency ISO code", example = "INR")
        String currency,

        @Schema(description = "Compensation frequency period", example = "ANNUAL")
        String period,

        @Schema(description = "Effective revision date in ISO-8601 format", example = "2025-04-01")
        String revisedDate
) {
}
