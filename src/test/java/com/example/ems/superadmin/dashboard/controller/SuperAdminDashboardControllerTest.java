package com.example.ems.superadmin.dashboard.controller;

import com.example.ems.superadmin.dashboard.dto.SuperAdminDashboardResponse;
import com.example.ems.superadmin.dashboard.dto.SuperAdminDashboardResponse.*;
import com.example.ems.superadmin.dashboard.service.SuperAdminDashboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class SuperAdminDashboardControllerTest {

    private MockMvc mockMvc;

    @Mock
    private SuperAdminDashboardService dashboardService;

    @InjectMocks
    private SuperAdminDashboardController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void testGetDashboard_returnsAggregatedPayload() throws Exception {
        OverviewStats overview = new OverviewStats(128, 114, 14, 18452, 17120, 1332, 19284, 18120, 47, 19);
        OrganizationOverviewStats orgStats = new OrganizationOverviewStats(8, 6, 2, 6.67);
        EmployeeOverviewStats empStats = new EmployeeOverviewStats(624, 17120, 87, 3.8);
        SubscriptionStats subStats = new SubscriptionStats(128, 114, 7, 5, 2, 9L);
        RevenueStats revenue = new RevenueStats(new BigDecimal("2485000.00"), new BigDecimal("2210000.00"), 12.44, "INR", 184L);
        AttendanceStats attendance = new AttendanceStats(15820, 634, 421, 245, 92.31);
        LeaveStats leave = new LeaveStats(184, 823, 72, 21L);
        PayrollStats payroll = new PayrollStats(14, 102, 2L, new BigDecimal("184500000.00"), "INR");

        DashboardData data = new DashboardData(
                overview, orgStats, empStats, subStats, revenue, attendance, leave, payroll,
                List.of(), List.of(), List.of(),
                new ChartsData(List.of(), List.of(), List.of(), List.of())
        );
        DashboardMeta meta = new DashboardMeta("MONTH", LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 23), "2026-09-23T10:55:00Z");

        when(dashboardService.getAggregatedDashboard(any(), any(), any()))
                .thenReturn(new SuperAdminDashboardResponse(true, data, meta));

        mockMvc.perform(get("/api/v1/super-admin/dashboard?period=MONTH&from=2026-09-01&to=2026-09-23")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.overview.totalOrganizations").value(128))
                .andExpect(jsonPath("$.data.overview.activeOrganizations").value(114))
                .andExpect(jsonPath("$.data.overview.totalEmployees").value(18452))
                .andExpect(jsonPath("$.data.overview.pendingApprovals").value(47))
                .andExpect(jsonPath("$.data.overview.pendingSupportTickets").value(19))
                .andExpect(jsonPath("$.data.revenue.currency").value("INR"))
                .andExpect(jsonPath("$.data.attendance.attendancePercentage").value(92.31))
                .andExpect(jsonPath("$.meta.period").value("MONTH"));
    }

    @Test
    void testGetOrganizations_drilldown() throws Exception {
        when(dashboardService.getOrganizationStats(any(), any(), any()))
                .thenReturn(new OrganizationDrilldownStats(128, 114, 14, 8, 2, 6.67));

        mockMvc.perform(get("/api/v1/super-admin/dashboard/organizations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(128))
                .andExpect(jsonPath("$.active").value(114))
                .andExpect(jsonPath("$.inactive").value(14))
                .andExpect(jsonPath("$.new").value(8))
                .andExpect(jsonPath("$.suspended").value(2))
                .andExpect(jsonPath("$.growthPercentage").value(6.67));
    }

    @Test
    void testGetEmployees_drilldown() throws Exception {
        when(dashboardService.getEmployeeStats(any(), any(), any()))
                .thenReturn(new EmployeeDrilldownStats(18452, 17120, 1332, 624, 87, 3.8));

        mockMvc.perform(get("/api/v1/super-admin/dashboard/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(18452))
                .andExpect(jsonPath("$.active").value(17120))
                .andExpect(jsonPath("$.inactive").value(1332))
                .andExpect(jsonPath("$.new").value(624))
                .andExpect(jsonPath("$.exited").value(87))
                .andExpect(jsonPath("$.growthPercentage").value(3.8));
    }

    @Test
    void testGetSubscriptions_drilldown() throws Exception {
        when(dashboardService.getSubscriptionStats())
                .thenReturn(new SubscriptionStats(128, 114, 7, 5, 2, 9L));

        mockMvc.perform(get("/api/v1/super-admin/dashboard/subscriptions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(128))
                .andExpect(jsonPath("$.active").value(114))
                .andExpect(jsonPath("$.trial").value(7))
                .andExpect(jsonPath("$.expired").value(5))
                .andExpect(jsonPath("$.cancelled").value(2))
                .andExpect(jsonPath("$.expiringSoon").value(9));
    }

    @Test
    void testGetRevenue_drilldown() throws Exception {
        when(dashboardService.getRevenueStats(any(), any()))
                .thenReturn(new RevenueStats(new BigDecimal("2485000.00"), new BigDecimal("2210000.00"), 12.44, "INR", 184L));

        mockMvc.perform(get("/api/v1/super-admin/dashboard/revenue?from=2026-09-01&to=2026-09-23"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentPeriod").value(2485000.00))
                .andExpect(jsonPath("$.previousPeriod").value(2210000.00))
                .andExpect(jsonPath("$.growthPercentage").value(12.44))
                .andExpect(jsonPath("$.currency").value("INR"))
                .andExpect(jsonPath("$.transactions").value(184));
    }

    @Test
    void testGetAttendance_drilldown() throws Exception {
        when(dashboardService.getAttendanceStats(any(), any()))
                .thenReturn(new AttendanceStats(15820, 634, 421, 245, 92.31));

        mockMvc.perform(get("/api/v1/super-admin/dashboard/attendance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.present").value(15820))
                .andExpect(jsonPath("$.absent").value(634))
                .andExpect(jsonPath("$.late").value(421))
                .andExpect(jsonPath("$.onLeave").value(245))
                .andExpect(jsonPath("$.attendancePercentage").value(92.31));
    }

    @Test
    void testGetLeave_drilldown() throws Exception {
        when(dashboardService.getLeaveStats(any(), any()))
                .thenReturn(new LeaveStats(184, 823, 72, 21L));

        mockMvc.perform(get("/api/v1/super-admin/dashboard/leave"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pending").value(184))
                .andExpect(jsonPath("$.approved").value(823))
                .andExpect(jsonPath("$.rejected").value(72))
                .andExpect(jsonPath("$.cancelled").value(21));
    }

    @Test
    void testGetPayroll_drilldown() throws Exception {
        when(dashboardService.getPayrollStats())
                .thenReturn(new PayrollStats(14, 102, 2L, new BigDecimal("184500000.00"), "INR"));

        mockMvc.perform(get("/api/v1/super-admin/dashboard/payroll"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pending").value(14))
                .andExpect(jsonPath("$.processed").value(102))
                .andExpect(jsonPath("$.failed").value(2))
                .andExpect(jsonPath("$.totalPayroll").value(184500000.00))
                .andExpect(jsonPath("$.currency").value("INR"));
    }

    @Test
    void testGetRecentOrganizations() throws Exception {
        when(dashboardService.getRecentOrganizations(anyInt()))
                .thenReturn(new PageDto<>(List.of(
                        new RecentOrganizationDto(1001L, "ABC Tech", "ACTIVE", 245, "2026-09-22T10:15:00Z")
                ), 128));

        mockMvc.perform(get("/api/v1/super-admin/dashboard/recent-organizations?limit=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].organizationId").value(1001))
                .andExpect(jsonPath("$.content[0].organizationName").value("ABC Tech"))
                .andExpect(jsonPath("$.totalElements").value(128));
    }

    @Test
    void testGetRecentUsers() throws Exception {
        when(dashboardService.getRecentUsers(anyInt()))
                .thenReturn(new PageDto<>(List.of(
                        new RecentUserDto(9001L, "John Doe", "john@example.com", "ABC Tech", "ACTIVE", "2026-09-23T08:20:00Z")
                ), 19284));

        mockMvc.perform(get("/api/v1/super-admin/dashboard/recent-users?limit=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].userId").value(9001))
                .andExpect(jsonPath("$.content[0].name").value("John Doe"))
                .andExpect(jsonPath("$.content[0].email").value("john@example.com"));
    }

    @Test
    void testGetActivity() throws Exception {
        when(dashboardService.getRecentActivities(anyInt()))
                .thenReturn(new PageDto<>(List.of(
                        new RecentActivityDto(99182L, "ORGANIZATION_CREATED", "ORGANIZATION", "1001", "9001", "1001", "SUCCESS", "2026-09-23T10:21:11Z")
                ), 1));

        mockMvc.perform(get("/api/v1/super-admin/dashboard/activity?limit=20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(99182))
                .andExpect(jsonPath("$.content[0].action").value("ORGANIZATION_CREATED"))
                .andExpect(jsonPath("$.content[0].resource").value("ORGANIZATION"));
    }

    @Test
    void testGetGrowthChart() throws Exception {
        when(dashboardService.getGrowthChart())
                .thenReturn(new GrowthChartResponse(
                        List.of(new ChartPointDto("2026-01", 82)),
                        List.of(new ChartPointDto("2026-01", 12450))
                ));

        mockMvc.perform(get("/api/v1/super-admin/dashboard/charts/growth?period=YEAR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.organizationGrowth[0].period").value("2026-01"))
                .andExpect(jsonPath("$.organizationGrowth[0].count").value(82))
                .andExpect(jsonPath("$.employeeGrowth[0].count").value(12450));
    }

    @Test
    void testGetRevenueChart() throws Exception {
        when(dashboardService.getRevenueChart())
                .thenReturn(new RevenueChartResponse(
                        "INR",
                        List.of(new RevenueChartDataPoint("2026-01", new BigDecimal("1845000.00")))
                ));

        mockMvc.perform(get("/api/v1/super-admin/dashboard/charts/revenue"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currency").value("INR"))
                .andExpect(jsonPath("$.data[0].period").value("2026-01"))
                .andExpect(jsonPath("$.data[0].revenue").value(1845000.00));
    }
}
