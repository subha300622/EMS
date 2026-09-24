package com.example.ems.superadmin.dashboard.service;

import com.example.ems.security.rls.PostgresRlsSessionBinder;
import com.example.ems.superadmin.dashboard.dto.DashboardPeriod;
import com.example.ems.superadmin.dashboard.dto.DashboardPeriod.DateRange;
import com.example.ems.superadmin.dashboard.dto.SuperAdminDashboardResponse;
import com.example.ems.superadmin.dashboard.dto.SuperAdminDashboardResponse.*;
import com.example.ems.superadmin.dashboard.service.query.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class SuperAdminDashboardService {

    @Autowired
    private PostgresRlsSessionBinder postgresRlsSessionBinder;

    @Autowired
    private OrganizationDashboardQuery organizationDashboardQuery;

    @Autowired
    private EmployeeDashboardQuery employeeDashboardQuery;

    @Autowired
    private SubscriptionDashboardQuery subscriptionDashboardQuery;

    @Autowired
    private RevenueDashboardQuery revenueDashboardQuery;

    @Autowired
    private AttendanceDashboardQuery attendanceDashboardQuery;

    @Autowired
    private LeaveDashboardQuery leaveDashboardQuery;

    @Autowired
    private PayrollDashboardQuery payrollDashboardQuery;

    @Autowired
    private ApprovalDashboardQuery approvalDashboardQuery;

    @Autowired
    private SupportDashboardQuery supportDashboardQuery;

    @Autowired
    private AuditDashboardQuery auditDashboardQuery;

    private void establishPlatformReadScope() {
        try {
            postgresRlsSessionBinder.bindTenantToCurrentTransaction(null, true);
        } catch (Exception ignored) {
            // In unit tests or mock environments without active PostgreSQL session, ignore
        }
    }

    public SuperAdminDashboardResponse getAggregatedDashboard(DashboardPeriod period, LocalDate from, LocalDate to) {
        establishPlatformReadScope();

        DashboardPeriod effectivePeriod = period != null ? period : DashboardPeriod.MONTH;
        DateRange dateRange = effectivePeriod.resolveDateRange(from, to);

        // 1. Organization counts
        OrganizationDashboardQuery.OrganizationCounts orgCounts = organizationDashboardQuery.queryCounts(
                dateRange.from(), dateRange.to(), dateRange.previousFrom(), dateRange.previousTo());

        // 2. Employee counts
        EmployeeDashboardQuery.EmployeeCounts empCounts = employeeDashboardQuery.queryCounts(
                dateRange.from(), dateRange.to(), dateRange.previousFrom(), dateRange.previousTo());

        // 3. User counts
        AuditDashboardQuery.UserCounts userCounts = auditDashboardQuery.queryUserCounts();

        // 4. Pending workflows & support tickets
        long pendingApprovals = approvalDashboardQuery.queryPendingApprovals();
        long pendingSupportTickets = supportDashboardQuery.queryPendingSupportTickets();

        // Overview stats
        OverviewStats overview = new OverviewStats(
                orgCounts.total(),
                orgCounts.active(),
                orgCounts.inactive(),
                empCounts.total(),
                empCounts.active(),
                empCounts.inactive(),
                userCounts.totalUsers(),
                userCounts.activeUsers(),
                pendingApprovals,
                pendingSupportTickets
        );

        OrganizationOverviewStats orgOverview = new OrganizationOverviewStats(
                orgCounts.newCount(),
                orgCounts.activated(),
                orgCounts.suspended(),
                orgCounts.growthPercentage()
        );

        EmployeeOverviewStats empOverview = new EmployeeOverviewStats(
                empCounts.newEmployees(),
                empCounts.active(),
                empCounts.exitedEmployees(),
                empCounts.growthPercentage()
        );

        // 5. Subscription counts
        SubscriptionDashboardQuery.SubscriptionCounts subCounts = subscriptionDashboardQuery.queryCounts();
        SubscriptionStats subStats = new SubscriptionStats(
                subCounts.total(),
                subCounts.active(),
                subCounts.trial(),
                subCounts.expired(),
                subCounts.cancelled(),
                subCounts.expiringSoon()
        );

        // 6. Revenue stats
        RevenueDashboardQuery.RevenueStatsResult revCounts = revenueDashboardQuery.queryRevenue(
                dateRange.from(), dateRange.to(), dateRange.previousFrom(), dateRange.previousTo());
        RevenueStats revenue = new RevenueStats(
                revCounts.currentPeriod(),
                revCounts.previousPeriod(),
                revCounts.growthPercentage(),
                revCounts.currency(),
                revCounts.transactions()
        );

        // 7. Attendance stats
        AttendanceStats attendance = attendanceDashboardQuery.queryAttendance(dateRange.from(), dateRange.to());

        // 8. Leave stats
        LeaveStats leave = leaveDashboardQuery.queryLeave(dateRange.from(), dateRange.to());

        // 9. Payroll stats
        PayrollStats payroll = payrollDashboardQuery.queryPayroll();

        // 10. Recents
        List<RecentOrganizationDto> recentOrgs = organizationDashboardQuery.queryRecentOrganizations(10);
        List<RecentUserDto> recentUsers = auditDashboardQuery.queryRecentUsers(10);
        List<RecentActivityDto> recentActivities = auditDashboardQuery.queryRecentActivities(20);

        // 11. Charts
        ChartsData charts = new ChartsData(
                organizationDashboardQuery.queryGrowthChart(),
                employeeDashboardQuery.queryGrowthChart(),
                revenueDashboardQuery.queryRevenueTrend(),
                subscriptionDashboardQuery.querySubscriptionTrend()
        );

        DashboardData data = new DashboardData(
                overview,
                orgOverview,
                empOverview,
                subStats,
                revenue,
                attendance,
                leave,
                payroll,
                recentOrgs,
                recentUsers,
                recentActivities,
                charts
        );

        DashboardMeta meta = new DashboardMeta(
                effectivePeriod.name(),
                dateRange.from(),
                dateRange.to(),
                Instant.now().toString()
        );

        return new SuperAdminDashboardResponse(true, data, meta);
    }

    public OrganizationDrilldownStats getOrganizationStats(DashboardPeriod period, LocalDate from, LocalDate to) {
        establishPlatformReadScope();
        DashboardPeriod effectivePeriod = period != null ? period : DashboardPeriod.MONTH;
        DateRange dateRange = effectivePeriod.resolveDateRange(from, to);
        OrganizationDashboardQuery.OrganizationCounts counts = organizationDashboardQuery.queryCounts(
                dateRange.from(), dateRange.to(), dateRange.previousFrom(), dateRange.previousTo());
        return new OrganizationDrilldownStats(
                counts.total(),
                counts.active(),
                counts.inactive(),
                counts.newCount(),
                counts.suspended(),
                counts.growthPercentage()
        );
    }

    public EmployeeDrilldownStats getEmployeeStats(DashboardPeriod period, LocalDate from, LocalDate to) {
        establishPlatformReadScope();
        DashboardPeriod effectivePeriod = period != null ? period : DashboardPeriod.MONTH;
        DateRange dateRange = effectivePeriod.resolveDateRange(from, to);
        EmployeeDashboardQuery.EmployeeCounts counts = employeeDashboardQuery.queryCounts(
                dateRange.from(), dateRange.to(), dateRange.previousFrom(), dateRange.previousTo());
        return new EmployeeDrilldownStats(
                counts.total(),
                counts.active(),
                counts.inactive(),
                counts.newEmployees(),
                counts.exitedEmployees(),
                counts.growthPercentage()
        );
    }

    public SubscriptionStats getSubscriptionStats() {
        establishPlatformReadScope();
        SubscriptionDashboardQuery.SubscriptionCounts counts = subscriptionDashboardQuery.queryCounts();
        return new SubscriptionStats(
                counts.total(),
                counts.active(),
                counts.trial(),
                counts.expired(),
                counts.cancelled(),
                counts.expiringSoon()
        );
    }

    public RevenueStats getRevenueStats(LocalDate from, LocalDate to) {
        establishPlatformReadScope();
        DashboardPeriod.DateRange dateRange = DashboardPeriod.CUSTOM.resolveDateRange(from, to);
        RevenueDashboardQuery.RevenueStatsResult counts = revenueDashboardQuery.queryRevenue(
                dateRange.from(), dateRange.to(), dateRange.previousFrom(), dateRange.previousTo());
        return new RevenueStats(
                counts.currentPeriod(),
                counts.previousPeriod(),
                counts.growthPercentage(),
                counts.currency(),
                counts.transactions()
        );
    }

    public AttendanceStats getAttendanceStats(LocalDate from, LocalDate to) {
        establishPlatformReadScope();
        LocalDate start = from != null ? from : LocalDate.now().withDayOfMonth(1);
        LocalDate end = to != null ? to : LocalDate.now();
        return attendanceDashboardQuery.queryAttendance(start, end);
    }

    public LeaveStats getLeaveStats(LocalDate from, LocalDate to) {
        establishPlatformReadScope();
        LocalDate start = from != null ? from : LocalDate.now().withDayOfMonth(1);
        LocalDate end = to != null ? to : LocalDate.now();
        return leaveDashboardQuery.queryLeave(start, end);
    }

    public PayrollStats getPayrollStats() {
        establishPlatformReadScope();
        return payrollDashboardQuery.queryPayroll();
    }

    public PageDto<RecentOrganizationDto> getRecentOrganizations(int limit) {
        establishPlatformReadScope();
        int effectiveLimit = limit > 0 ? limit : 10;
        List<RecentOrganizationDto> items = organizationDashboardQuery.queryRecentOrganizations(effectiveLimit);
        OrganizationDashboardQuery.OrganizationCounts counts = organizationDashboardQuery.queryCounts(
                LocalDate.now().minusMonths(1), LocalDate.now(), LocalDate.now().minusMonths(2), LocalDate.now().minusMonths(1));
        return new PageDto<>(items, counts.total());
    }

    public PageDto<RecentUserDto> getRecentUsers(int limit) {
        establishPlatformReadScope();
        int effectiveLimit = limit > 0 ? limit : 10;
        List<RecentUserDto> items = auditDashboardQuery.queryRecentUsers(effectiveLimit);
        AuditDashboardQuery.UserCounts counts = auditDashboardQuery.queryUserCounts();
        return new PageDto<>(items, counts.totalUsers());
    }

    public PageDto<RecentActivityDto> getRecentActivities(int limit) {
        establishPlatformReadScope();
        int effectiveLimit = limit > 0 ? limit : 20;
        List<RecentActivityDto> items = auditDashboardQuery.queryRecentActivities(effectiveLimit);
        return new PageDto<>(items, items.size());
    }

    public GrowthChartResponse getGrowthChart() {
        establishPlatformReadScope();
        return new GrowthChartResponse(
                organizationDashboardQuery.queryGrowthChart(),
                employeeDashboardQuery.queryGrowthChart()
        );
    }

    public RevenueChartResponse getRevenueChart() {
        establishPlatformReadScope();
        return new RevenueChartResponse(
                "INR",
                revenueDashboardQuery.queryRevenueTrend()
        );
    }
}
