package com.example.ems.leave.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;

@Schema(description = "Leave dashboard metrics and distribution statistics")
public record LeaveDashboardMetricsDto(
        @Schema(description = "High-level summary counts")
        LeaveSummaryCountsDto summary,

        @Schema(description = "Leave count distribution by leave type name")
        Map<String, Long> leaveTypeDistribution,

        @Schema(description = "Leave count distribution by department")
        Map<String, Long> departmentDistribution
) {
    @Schema(description = "Leave summary status counts")
    public record LeaveSummaryCountsDto(
            @Schema(description = "Total number of leave requests", example = "50")
            long total,

            @Schema(description = "Total pending leave requests", example = "5")
            long pending,

            @Schema(description = "Total approved leave requests", example = "40")
            long approved,

            @Schema(description = "Total rejected leave requests", example = "3")
            long rejected,

            @Schema(description = "Total cancelled leave requests", example = "2")
            long cancelled
    ) {}
}
