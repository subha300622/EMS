package com.example.ems.employee.dto.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Detailed leave balance response")
public record EmployeeLeaveBalanceDetailDto(
        @Schema(description = "Total available leave days across all types", example = "12.0")
        Double totalAvailable,

        @Schema(description = "Breakdown of leave balances per leave type")
        List<EmployeeLeaveTypeItemDto> leaveTypes
) {
}
