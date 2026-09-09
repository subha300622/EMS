package com.example.ems.reports.organization.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Platform user activity report response")
public record UserActivityReportResponse(
        @Schema(description = "Last Login Timestamp", example = "2026-07-01T10:00:00Z") String lastLogin,
        @Schema(description = "Active Users Count", example = "42") long activeUsers,
        @Schema(description = "Inactive Users Count", example = "3") long inactiveUsers,
        @Schema(description = "Last Activity Timestamp", example = "2026-07-01T10:00:00Z") String lastActivity,
        @Schema(description = "Audit Count", example = "128") long auditCount
) {}
