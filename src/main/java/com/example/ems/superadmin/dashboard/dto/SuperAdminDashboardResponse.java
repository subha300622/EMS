package com.example.ems.superadmin.dashboard.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Aggregated Super Admin Dashboard Response")
public record SuperAdminDashboardResponse(
        @Schema(description = "Success indicator", example = "true")
        boolean success,

        @Schema(description = "Aggregated dashboard data")
        DashboardData data,

        @Schema(description = "Dashboard request metadata")
        DashboardMeta meta
) {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "Dashboard metrics data")
    public record DashboardData(
            OverviewStats overview,
            OrganizationOverviewStats organizationStats,
            EmployeeOverviewStats employeeStats,
            SubscriptionStats subscriptionStats,
            RevenueStats revenue,
            AttendanceStats attendance,
            LeaveStats leave,
            PayrollStats payroll,
            List<RecentOrganizationDto> recentOrganizations,
            List<RecentUserDto> recentUsers,
            List<RecentActivityDto> recentActivities,
            ChartsData charts
    ) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "Platform-wide overview statistics")
    public record OverviewStats(
            long totalOrganizations,
            long activeOrganizations,
            long inactiveOrganizations,
            long totalEmployees,
            long activeEmployees,
            long inactiveEmployees,
            long totalUsers,
            long activeUsers,
            long pendingApprovals,
            long pendingSupportTickets
    ) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "Organization growth and status stats for overview")
    public record OrganizationOverviewStats(
            long newOrganizations,
            long activatedOrganizations,
            long suspendedOrganizations,
            double growthPercentage
    ) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "Detailed organization stats for drilldown")
    public record OrganizationDrilldownStats(
            long total,
            long active,
            long inactive,
            long newCount,
            long suspended,
            double growthPercentage
    ) {
        // Alias getter for json serialization "new"
        public long getNew() { return newCount; }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "Employee stats for overview")
    public record EmployeeOverviewStats(
            long newEmployees,
            long activeEmployees,
            long exitedEmployees,
            double growthPercentage
    ) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "Detailed employee stats for drilldown")
    public record EmployeeDrilldownStats(
            long total,
            long active,
            long inactive,
            long newCount,
            long exited,
            double growthPercentage
    ) {
        public long getNew() { return newCount; }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "Platform-level subscription stats")
    public record SubscriptionStats(
            long totalSubscriptions,
            long activeSubscriptions,
            long trialSubscriptions,
            long expiredSubscriptions,
            long cancelledSubscriptions,
            Long expiringSoon
    ) {
        // Convenience constructor for drilldown
        public long getTotal() { return totalSubscriptions; }
        public long getActive() { return activeSubscriptions; }
        public long getTrial() { return trialSubscriptions; }
        public long getExpired() { return expiredSubscriptions; }
        public long getCancelled() { return cancelledSubscriptions; }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "Platform revenue stats")
    public record RevenueStats(
            BigDecimal currentPeriod,
            BigDecimal previousPeriod,
            double growthPercentage,
            String currency,
            Long transactions
    ) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "Platform attendance overview")
    public record AttendanceStats(
            long present,
            long absent,
            long late,
            long onLeave,
            double attendancePercentage
    ) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "Platform leave overview")
    public record LeaveStats(
            long pending,
            long approved,
            long rejected,
            Long cancelled
    ) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "Platform payroll overview")
    public record PayrollStats(
            long pending,
            long processed,
            Long failed,
            BigDecimal totalPayroll,
            String currency
    ) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "Recent organization item")
    public record RecentOrganizationDto(
            Long organizationId,
            String organizationName,
            String status,
            long employeeCount,
            String createdAt
    ) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "Recent user item - sanitized, no credentials")
    public record RecentUserDto(
            Long userId,
            String name,
            String email,
            String organizationName,
            String status,
            String createdAt
    ) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "Recent activity item from platform audit log")
    public record RecentActivityDto(
            Long id,
            String action,
            String resource,
            String resourceId,
            String actorUserId,
            String organizationId,
            String status,
            String timestamp
    ) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "Aggregated charts data")
    public record ChartsData(
            List<ChartPointDto> organizationGrowth,
            List<ChartPointDto> employeeGrowth,
            List<RevenueChartDataPoint> revenueTrend,
            List<ChartPointDto> subscriptionTrend
    ) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "Generic period chart point")
    public record ChartPointDto(
            String period,
            long count
    ) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "Revenue trend chart point")
    public record RevenueChartDataPoint(
            String period,
            BigDecimal revenue
    ) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "Growth charts response")
    public record GrowthChartResponse(
            List<ChartPointDto> organizationGrowth,
            List<ChartPointDto> employeeGrowth
    ) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "Revenue chart response")
    public record RevenueChartResponse(
            String currency,
            List<RevenueChartDataPoint> data
    ) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "Page response wrapper for recent items")
    public record PageDto<T>(
            List<T> content,
            long totalElements
    ) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "Metadata regarding the dashboard execution")
    public record DashboardMeta(
            String period,
            LocalDate from,
            LocalDate to,
            String generatedAt
    ) {}
}
