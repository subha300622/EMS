package com.example.ems.audit.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Audit log dashboard summary statistics")
public record AuditDashboardStatsDto(
    @Schema(description = "Count of financial actions today", example = "14") long financeActionsToday,
    @Schema(description = "Count of flagged audit events", example = "2") long flaggedCount,
    @Schema(description = "Count of payroll events this week", example = "8") long payrollEventsThisWeek,
    @Schema(description = "Pending approvals this month", example = "5") long pendingApprovalsThisMonth
) {}
