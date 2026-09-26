package com.example.ems.superadmin.dashboard;

import com.example.ems.auth.entity.Permission;
import com.example.ems.auth.entity.Role;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.PermissionRepository;
import com.example.ems.auth.repository.RoleRepository;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.entity.OrganizationStatus;
import com.example.ems.organization.repository.OrganizationRepository;
import com.example.ems.security.context.TenantContext;
import com.example.ems.security.dto.AuthPrincipal;
import com.example.ems.superadmin.dashboard.controller.SuperAdminDashboardController;
import com.example.ems.superadmin.dashboard.dto.DashboardPeriod;
import com.example.ems.superadmin.dashboard.dto.SuperAdminDashboardResponse;
import com.example.ems.superadmin.dashboard.dto.SuperAdminDashboardResponse.*;
import com.example.ems.superadmin.dashboard.service.SuperAdminDashboardService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class SuperAdminDashboardIntegrationTest {

    @Autowired
    private SuperAdminDashboardService dashboardService;

    @Autowired
    private SuperAdminDashboardController dashboardController;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    private Organization testOrg;
    private User superAdminUser;
    private User normalUser;

    @BeforeEach
    void setUp() {
        // Create an organization for testing
        testOrg = new Organization();
        testOrg.setName("Super Admin Test Org " + System.currentTimeMillis());
        testOrg.setOrganizationCode("SATO_" + System.currentTimeMillis());
        testOrg.setStatus(OrganizationStatus.ACTIVE);
        testOrg = organizationRepository.save(testOrg);

        // Ensure platform.dashboard.view permission exists
        Permission dashPerm = permissionRepository.findByName("platform.dashboard.view")
                .orElseGet(() -> {
                    Permission p = new Permission();
                    p.setName("platform.dashboard.view");
                    p.setDescription("View Platform Dashboard");
                    p.setActive(true);
                    return permissionRepository.save(p);
                });

        // Create Super Admin Role
        Role superAdminRole = roleRepository.findByName("SUPER_ADMIN")
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setName("SUPER_ADMIN");
                    r.setDescription("Super Admin Role");
                    r.setPlatformTemplate(true);
                    r.setSystemRole(true);
                    r.setPermissions(new HashSet<>(Set.of(dashPerm)));
                    return roleRepository.save(r);
                });

        // Create Normal Employee Role without dashboard permission
        Role employeeRole = roleRepository.findByName("EMPLOYEE_NO_DASH")
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setName("EMPLOYEE_NO_DASH");
                    r.setDescription("Employee Without Dashboard Access");
                    r.setPlatformTemplate(false);
                    r.setSystemRole(false);
                    r.setOrganization(testOrg);
                    r.setPermissions(new HashSet<>());
                    return roleRepository.save(r);
                });

        // Create Super Admin User
        superAdminUser = new User();
        superAdminUser.setUserId("SA_USER_" + System.currentTimeMillis());
        superAdminUser.setWorkEmail("sa_" + System.currentTimeMillis() + "@platform.internal");
        superAdminUser.setFullName("Super Administrator");
        superAdminUser.setRole(superAdminRole);
        superAdminUser.setStatus("ACTIVE");
        superAdminUser = userRepository.save(superAdminUser);

        // Create Regular User
        normalUser = new User();
        normalUser.setUserId("NORM_USER_" + System.currentTimeMillis());
        normalUser.setWorkEmail("norm_" + System.currentTimeMillis() + "@tenant.internal");
        normalUser.setFullName("Normal Employee");
        normalUser.setRole(employeeRole);
        normalUser.setOrganization(testOrg);
        normalUser.setStatus("ACTIVE");
        normalUser = userRepository.save(normalUser);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        TenantContext.clear();
    }

    private void authenticateAsSuperAdmin() {
        AuthPrincipal principal = new AuthPrincipal(
                superAdminUser.getUserId(),
                "session-token-sa",
                1,
                System.currentTimeMillis(),
                superAdminUser.getWorkEmail(),
                "SUPER_ADMIN"
        );
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private void authenticateAsNormalUser() {
        AuthPrincipal principal = new AuthPrincipal(
                normalUser.getUserId(),
                "session-token-norm",
                1,
                System.currentTimeMillis(),
                normalUser.getWorkEmail(),
                "EMPLOYEE_NO_DASH"
        );
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_EMPLOYEE"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    // ── 1. Real Service Execution against database ─────────────────────────────

    @Test
    @DisplayName("Dashboard Service: getAggregatedDashboard executes across all periods without error")
    void testService_getAggregatedDashboard_allPeriods() {
        for (DashboardPeriod period : DashboardPeriod.values()) {
            LocalDate from = (period == DashboardPeriod.CUSTOM) ? LocalDate.now().minusDays(15) : null;
            LocalDate to = (period == DashboardPeriod.CUSTOM) ? LocalDate.now() : null;

            SuperAdminDashboardResponse response = dashboardService.getAggregatedDashboard(period, from, to);

            assertThat(response).isNotNull();
            assertThat(response.success()).isTrue();
            assertThat(response.data()).isNotNull();
            assertThat(response.data().overview()).isNotNull();
            assertThat(response.data().overview().totalOrganizations()).isGreaterThanOrEqualTo(1);
            assertThat(response.data().organizationStats()).isNotNull();
            assertThat(response.data().employeeStats()).isNotNull();
            assertThat(response.data().subscriptionStats()).isNotNull();
            assertThat(response.data().revenue()).isNotNull();
            assertThat(response.data().attendance()).isNotNull();
            assertThat(response.data().leave()).isNotNull();
            assertThat(response.data().payroll()).isNotNull();
            assertThat(response.data().charts()).isNotNull();
            assertThat(response.meta()).isNotNull();
            assertThat(response.meta().period()).isEqualTo(period.name());
        }
    }

    @Test
    @DisplayName("Dashboard Service: Drill-down methods execute successfully against database")
    void testService_drilldownMethods() {
        OrganizationDrilldownStats orgStats = dashboardService.getOrganizationStats(DashboardPeriod.MONTH, null, null);
        assertThat(orgStats).isNotNull();
        assertThat(orgStats.total()).isGreaterThanOrEqualTo(1);

        EmployeeDrilldownStats empStats = dashboardService.getEmployeeStats(DashboardPeriod.MONTH, null, null);
        assertThat(empStats).isNotNull();

        SubscriptionStats subStats = dashboardService.getSubscriptionStats();
        assertThat(subStats).isNotNull();

        RevenueStats revStats = dashboardService.getRevenueStats(LocalDate.now().minusDays(30), LocalDate.now());
        assertThat(revStats).isNotNull();
        assertThat(revStats.currency()).isEqualTo("INR");

        AttendanceStats attStats = dashboardService.getAttendanceStats(null, null);
        assertThat(attStats).isNotNull();

        LeaveStats leaveStats = dashboardService.getLeaveStats(null, null);
        assertThat(leaveStats).isNotNull();

        PayrollStats payStats = dashboardService.getPayrollStats();
        assertThat(payStats).isNotNull();
        assertThat(payStats.currency()).isEqualTo("INR");

        PageDto<RecentOrganizationDto> recentOrgs = dashboardService.getRecentOrganizations(5);
        assertThat(recentOrgs).isNotNull();
        assertThat(recentOrgs.totalElements()).isGreaterThanOrEqualTo(1);

        PageDto<RecentUserDto> recentUsers = dashboardService.getRecentUsers(5);
        assertThat(recentUsers).isNotNull();

        PageDto<RecentActivityDto> recentActivities = dashboardService.getRecentActivities(10);
        assertThat(recentActivities).isNotNull();

        GrowthChartResponse growthChart = dashboardService.getGrowthChart();
        assertThat(growthChart).isNotNull();

        RevenueChartResponse revChart = dashboardService.getRevenueChart();
        assertThat(revChart).isNotNull();
        assertThat(revChart.currency()).isEqualTo("INR");
    }

    // ── 2. Security & Controller Level Access Control ─────────────────────────

    @Test
    @DisplayName("Controller Security: SuperAdmin user successfully accesses all dashboard endpoints")
    void testController_superAdminAccessGranted() {
        authenticateAsSuperAdmin();

        ResponseEntity<SuperAdminDashboardResponse> dashResp =
                dashboardController.getDashboard(DashboardPeriod.MONTH, null, null);
        assertThat(dashResp.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(dashResp.getBody()).isNotNull();
        assertThat(dashResp.getBody().success()).isTrue();

        ResponseEntity<OrganizationDrilldownStats> orgResp =
                dashboardController.getOrganizations(DashboardPeriod.MONTH, null, null);
        assertThat(orgResp.getStatusCode().is2xxSuccessful()).isTrue();

        ResponseEntity<EmployeeDrilldownStats> empResp =
                dashboardController.getEmployees(DashboardPeriod.MONTH, null, null);
        assertThat(empResp.getStatusCode().is2xxSuccessful()).isTrue();

        ResponseEntity<SubscriptionStats> subResp = dashboardController.getSubscriptions();
        assertThat(subResp.getStatusCode().is2xxSuccessful()).isTrue();

        ResponseEntity<RevenueStats> revResp = dashboardController.getRevenue(null, null);
        assertThat(revResp.getStatusCode().is2xxSuccessful()).isTrue();

        ResponseEntity<AttendanceStats> attResp = dashboardController.getAttendance(null, null);
        assertThat(attResp.getStatusCode().is2xxSuccessful()).isTrue();

        ResponseEntity<LeaveStats> leaveResp = dashboardController.getLeave(null, null);
        assertThat(leaveResp.getStatusCode().is2xxSuccessful()).isTrue();

        ResponseEntity<PayrollStats> payResp = dashboardController.getPayroll();
        assertThat(payResp.getStatusCode().is2xxSuccessful()).isTrue();

        ResponseEntity<PageDto<RecentOrganizationDto>> recentOrgResp =
                dashboardController.getRecentOrganizations(5);
        assertThat(recentOrgResp.getStatusCode().is2xxSuccessful()).isTrue();

        ResponseEntity<PageDto<RecentUserDto>> recentUserResp =
                dashboardController.getRecentUsers(5);
        assertThat(recentUserResp.getStatusCode().is2xxSuccessful()).isTrue();

        ResponseEntity<PageDto<RecentActivityDto>> actResp =
                dashboardController.getActivity(10);
        assertThat(actResp.getStatusCode().is2xxSuccessful()).isTrue();

        ResponseEntity<GrowthChartResponse> growthResp =
                dashboardController.getGrowthChart(DashboardPeriod.YEAR);
        assertThat(growthResp.getStatusCode().is2xxSuccessful()).isTrue();

        ResponseEntity<RevenueChartResponse> revChartResp =
                dashboardController.getRevenueChart();
        assertThat(revChartResp.getStatusCode().is2xxSuccessful()).isTrue();
    }

    @Test
    @DisplayName("Controller Security: Normal employee without permission is blocked by @PreAuthorize")
    void testController_normalUserAccessDenied() {
        authenticateAsNormalUser();

        assertThatThrownBy(() -> dashboardController.getDashboard(DashboardPeriod.MONTH, null, null))
                .isInstanceOf(AccessDeniedException.class);

        assertThatThrownBy(() -> dashboardController.getOrganizations(DashboardPeriod.MONTH, null, null))
                .isInstanceOf(AccessDeniedException.class);

        assertThatThrownBy(() -> dashboardController.getEmployees(DashboardPeriod.MONTH, null, null))
                .isInstanceOf(AccessDeniedException.class);

        assertThatThrownBy(() -> dashboardController.getSubscriptions())
                .isInstanceOf(AccessDeniedException.class);

        assertThatThrownBy(() -> dashboardController.getRevenue(null, null))
                .isInstanceOf(AccessDeniedException.class);

        assertThatThrownBy(() -> dashboardController.getAttendance(null, null))
                .isInstanceOf(AccessDeniedException.class);

        assertThatThrownBy(() -> dashboardController.getLeave(null, null))
                .isInstanceOf(AccessDeniedException.class);

        assertThatThrownBy(() -> dashboardController.getPayroll())
                .isInstanceOf(AccessDeniedException.class);

        assertThatThrownBy(() -> dashboardController.getRecentOrganizations(10))
                .isInstanceOf(AccessDeniedException.class);

        assertThatThrownBy(() -> dashboardController.getRecentUsers(10))
                .isInstanceOf(AccessDeniedException.class);

        assertThatThrownBy(() -> dashboardController.getActivity(20))
                .isInstanceOf(AccessDeniedException.class);

        assertThatThrownBy(() -> dashboardController.getGrowthChart(DashboardPeriod.YEAR))
                .isInstanceOf(AccessDeniedException.class);

        assertThatThrownBy(() -> dashboardController.getRevenueChart())
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("Controller Security: Unauthenticated request is blocked by @PreAuthorize")
    void testController_unauthenticatedAccessDenied() {
        SecurityContextHolder.clearContext();

        assertThatThrownBy(() -> dashboardController.getDashboard(DashboardPeriod.MONTH, null, null))
                .isInstanceOf(AccessDeniedException.class);
    }
}
