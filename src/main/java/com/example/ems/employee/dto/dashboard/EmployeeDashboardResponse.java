package com.example.ems.employee.dto.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Employee Dashboard root response")
public record EmployeeDashboardResponse(
        @Schema(description = "Domain summary aggregation")
        SummaryDto summary,

        @Schema(description = "Pending actions and tasks")
        EmployeeActionCenterResponseDto pendingActions
) {
    @Schema(description = "Aggregated summaries")
    public record SummaryDto(
            @Schema(description = "Attendance summary")
            EmployeeAttendanceSummaryDto attendance,

            @Schema(description = "Leave balance summary")
            EmployeeLeaveBalanceSummaryDto leaveBalance,

            @Schema(description = "Compensation summary")
            EmployeeCompensationSummaryDto compensation,

            @Schema(description = "Performance summary")
            EmployeePerformanceSummaryDto performance
    ) {}
}
