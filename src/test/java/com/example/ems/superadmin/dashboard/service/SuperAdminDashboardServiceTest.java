package com.example.ems.superadmin.dashboard.service;

import com.example.ems.security.rls.PostgresRlsSessionBinder;
import com.example.ems.superadmin.dashboard.dto.DashboardPeriod;
import com.example.ems.superadmin.dashboard.dto.SuperAdminDashboardResponse;
import com.example.ems.superadmin.dashboard.dto.SuperAdminDashboardResponse.*;
import com.example.ems.superadmin.dashboard.service.query.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

class SuperAdminDashboardServiceTest {

    @Mock
    private PostgresRlsSessionBinder postgresRlsSessionBinder;

    @Mock
    private OrganizationDashboardQuery organizationDashboardQuery;

    @Mock
    private EmployeeDashboardQuery employeeDashboardQuery;

    @Mock
    private SubscriptionDashboardQuery subscriptionDashboardQuery;

    @Mock
    private RevenueDashboardQuery revenueDashboardQuery;

    @Mock
    private AttendanceDashboardQuery attendanceDashboardQuery;

    @Mock
    private LeaveDashboardQuery leaveDashboardQuery;

    @Mock
    private PayrollDashboardQuery payrollDashboardQuery;

    @Mock
    private ApprovalDashboardQuery approvalDashboardQuery;

    @Mock
    private SupportDashboardQuery supportDashboardQuery;

    @Mock
    private AuditDashboardQuery auditDashboardQuery;

    @InjectMocks
    private SuperAdminDashboardService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAggregatedDashboard_success() {
        LocalDate from = LocalDate.of(2026, 9, 1);
        LocalDate to = LocalDate.of(2026, 9, 23);

        when(organizationDashboardQuery.queryCounts(any(), any(), any(), any()))
                .thenReturn(new OrganizationDashboardQuery.OrganizationCounts(128, 114, 14, 8, 6, 2, 6.67));

        when(employeeDashboardQuery.queryCounts(any(), any(), any(), any()))
                .thenReturn(new EmployeeDashboardQuery.EmployeeCounts(18452, 17120, 1332, 624, 87, 3.8));

        when(auditDashboardQuery.queryUserCounts())
                .thenReturn(new AuditDashboardQuery.UserCounts(19284, 18120));

        when(approvalDashboardQuery.queryPendingApprovals()).thenReturn(47L);
        when(supportDashboardQuery.queryPendingSupportTickets()).thenReturn(19L);

        when(subscriptionDashboardQuery.queryCounts())
                .thenReturn(new SubscriptionDashboardQuery.SubscriptionCounts(128, 114, 7, 5, 2, 9L));

        when(revenueDashboardQuery.queryRevenue(any(), any(), any(), any()))
                .thenReturn(new RevenueDashboardQuery.RevenueStatsResult(
                        new BigDecimal("2485000.00"), new BigDecimal("2210000.00"), 12.44, "INR", 184L));

        when(attendanceDashboardQuery.queryAttendance(any(), any()))
                .thenReturn(new AttendanceStats(15820, 634, 421, 245, 92.31));

        when(leaveDashboardQuery.queryLeave(any(), any()))
                .thenReturn(new LeaveStats(184, 823, 72, 21L));

        when(payrollDashboardQuery.queryPayroll())
                .thenReturn(new PayrollStats(14, 102, 2L, new BigDecimal("184500000.00"), "INR"));

        when(organizationDashboardQuery.queryRecentOrganizations(anyInt()))
                .thenReturn(List.of(new RecentOrganizationDto(1001L, "ABC Tech", "ACTIVE", 245, "2026-09-22T10:15:00Z")));

        when(auditDashboardQuery.queryRecentUsers(anyInt()))
                .thenReturn(List.of(new RecentUserDto(9001L, "John Doe", "john@example.com", "ABC Tech", "ACTIVE", "2026-09-23T08:20:00Z")));

        when(auditDashboardQuery.queryRecentActivities(anyInt()))
                .thenReturn(List.of(new RecentActivityDto(99182L, "ORGANIZATION_CREATED", "ORGANIZATION", "1001", "9001", "1001", "SUCCESS", "2026-09-23T10:21:11Z")));

        when(organizationDashboardQuery.queryGrowthChart())
                .thenReturn(List.of(new ChartPointDto("2026-01", 82)));

        when(employeeDashboardQuery.queryGrowthChart())
                .thenReturn(List.of(new ChartPointDto("2026-01", 12450)));

        when(revenueDashboardQuery.queryRevenueTrend())
                .thenReturn(List.of(new RevenueChartDataPoint("2026-01", new BigDecimal("1845000.00"))));

        when(subscriptionDashboardQuery.querySubscriptionTrend())
                .thenReturn(List.of(new ChartPointDto("2026-01", 120)));

        SuperAdminDashboardResponse response = service.getAggregatedDashboard(DashboardPeriod.MONTH, from, to);

        assertThat(response).isNotNull();
        assertThat(response.success()).isTrue();
        assertThat(response.data()).isNotNull();

        // Check Overview
        OverviewStats overview = response.data().overview();
        assertThat(overview.totalOrganizations()).isEqualTo(128);
        assertThat(overview.activeOrganizations()).isEqualTo(114);
        assertThat(overview.inactiveOrganizations()).isEqualTo(14);
        assertThat(overview.totalEmployees()).isEqualTo(18452);
        assertThat(overview.activeEmployees()).isEqualTo(17120);
        assertThat(overview.totalUsers()).isEqualTo(19284);
        assertThat(overview.activeUsers()).isEqualTo(18120);
        assertThat(overview.pendingApprovals()).isEqualTo(47);
        assertThat(overview.pendingSupportTickets()).isEqualTo(19);

        // Check Attendance %
        assertThat(response.data().attendance().attendancePercentage()).isEqualTo(92.31);

        // Check Revenue
        assertThat(response.data().revenue().currency()).isEqualTo("INR");
        assertThat(response.data().revenue().growthPercentage()).isEqualTo(12.44);

        // Check Meta
        assertThat(response.meta().period()).isEqualTo("MONTH");
    }

    @Test
    void testDateRangeResolution() {
        DashboardPeriod.DateRange monthRange = DashboardPeriod.MONTH.resolveDateRange(null, null);
        assertThat(monthRange.from()).isNotNull();
        assertThat(monthRange.to()).isNotNull();
        assertThat(monthRange.from()).isBeforeOrEqualTo(monthRange.to());

        LocalDate customStart = LocalDate.of(2026, 8, 1);
        LocalDate customEnd = LocalDate.of(2026, 8, 31);
        DashboardPeriod.DateRange customRange = DashboardPeriod.CUSTOM.resolveDateRange(customStart, customEnd);
        assertThat(customRange.from()).isEqualTo(customStart);
        assertThat(customRange.to()).isEqualTo(customEnd);
        assertThat(customRange.previousTo()).isEqualTo(LocalDate.of(2026, 7, 31));
    }
}
