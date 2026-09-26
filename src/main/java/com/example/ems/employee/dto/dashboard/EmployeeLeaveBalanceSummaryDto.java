package com.example.ems.employee.dto.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;

@Schema(description = "Leave balance summary for employee dashboard")
public record EmployeeLeaveBalanceSummaryDto(
        @Schema(description = "Total available leave days across all types", example = "12.0")
        Double totalAvailable,

        @Schema(description = "Available leave balances grouped by leave type code", example = "{\"CL\": 6.0, \"EL\": 4.0, \"SL\": 2.0}")
        Map<String, Double> balances
) {
}
