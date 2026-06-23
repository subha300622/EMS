package com.example.ems.common.controller;

import com.example.ems.auth.entity.Role;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.common.dto.manager.*;
import com.example.ems.common.service.ManagerDashboardService;
import com.example.ems.security.service.JwtService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ManagerDashboardControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ManagerDashboardService managerDashboardService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private ManagerDashboardController managerDashboardController;

    private static final String TOKEN = "mgr-token";
    private static final String AUTH_HEADER = "Bearer " + TOKEN;
    private static final String EMAIL = "manager@example.com";

    private User managerUser;
    private Employee managerEmployee;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(managerDashboardController).build();

        managerUser = new User();
        managerUser.setWorkEmail(EMAIL);
        Role role = new Role();
        role.setName("MANAGER");
        managerUser.setRole(role);

        managerEmployee = new Employee();
        managerEmployee.setId(10L);
        managerEmployee.setEmail(EMAIL);
        managerEmployee.setFullName("Tony Manager");

        when(jwtService.validateAccessToken(TOKEN)).thenReturn(true);
        when(jwtService.getEmailFromToken(TOKEN)).thenReturn(EMAIL);
        when(userRepository.findByWorkEmail(EMAIL)).thenReturn(Optional.of(managerUser));
        when(employeeRepository.findByEmail(EMAIL)).thenReturn(Optional.of(managerEmployee));
    }

    @Test
    public void testGetSummarySuccess() throws Exception {
        SummaryDto summaryDto = new SummaryDto(12L, 91.6, 1L, 42L, 10L, 1L);
        when(managerDashboardService.getSummary(any(Employee.class))).thenReturn(summaryDto);

        mockMvc.perform(get("/api/v1/manager/dashboard/summary")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.teamSize").value(12))
                .andExpect(jsonPath("$.data.attendanceRate").value(91.6));
    }

    @Test
    public void testGetAttendanceTrendSuccess() throws Exception {
        List<AttendanceTrendDto> trend = List.of(new AttendanceTrendDto("2026-05-01", 92.0));
        when(managerDashboardService.getAttendanceTrend(any(Employee.class), any(DashboardPeriod.class))).thenReturn(trend);

        mockMvc.perform(get("/api/v1/manager/dashboard/attendance-trend")
                .param("period", "MONTH")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].attendanceRate").value(92.0));
    }

    @Test
    public void testGetTeamCompositionSuccess() throws Exception {
        TeamCompositionDto comp = new TeamCompositionDto(10L, 1L, 1L);
        when(managerDashboardService.getTeamComposition(any(Employee.class))).thenReturn(comp);

        mockMvc.perform(get("/api/v1/manager/dashboard/team-composition")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.active").value(10));
    }

    @Test
    public void testGetTeamMembersSuccess() throws Exception {
        List<TeamMemberDto> membersList = List.of(new TeamMemberDto(1L, "Priya Sharma", "Engineer", 98, ShiftType.DAY_SHIFT, AttendanceStatus.PRESENT));
        Page<TeamMemberDto> page = new PageImpl<>(membersList, PageRequest.of(0, 10), 1);
        when(managerDashboardService.getTeamMembers(any(Employee.class), anyInt(), anyInt())).thenReturn(page);

        mockMvc.perform(get("/api/v1/manager/dashboard/team-members")
                .param("page", "0")
                .param("size", "10")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].name").value("Priya Sharma"));
    }

    @Test
    public void testGetPerformanceSuccess() throws Exception {
        PerformanceDto perf = new PerformanceDto(92, 86, 79, 95);
        when(managerDashboardService.getPerformance(any(Employee.class))).thenReturn(perf);

        mockMvc.perform(get("/api/v1/manager/dashboard/performance")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.performance").value(86));
    }

    @Test
    public void testGetOvertimeSuccess() throws Exception {
        List<OvertimeDto> list = List.of(new OvertimeDto(1L, "Priya Sharma", 42.0, 40.0, "EXCEEDED"));
        when(managerDashboardService.getOvertime(any(Employee.class))).thenReturn(list);

        mockMvc.perform(get("/api/v1/manager/dashboard/overtime")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].overtimeHours").value(42.0));
    }

    @Test
    public void testGetPendingApprovalsSuccess() throws Exception {
        List<PendingApprovalDto> pending = List.of(new PendingApprovalDto(101L, 2L, "Priya Sharma", "LEAVE", "2026-05-20", ApprovalStatus.PENDING));
        when(managerDashboardService.getPendingApprovals(any(Employee.class))).thenReturn(pending);

        mockMvc.perform(get("/api/v1/manager/dashboard/pending-approvals")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].employeeName").value("Priya Sharma"));
    }

    @Test
    public void testGetApprovalSummarySuccess() throws Exception {
        PendingApprovalCountsDto counts = new PendingApprovalCountsDto(5L, 2L, 1L, 3L, 0L, 11L);
        when(managerDashboardService.getApprovalSummary(any(Employee.class))).thenReturn(counts);

        mockMvc.perform(get("/api/v1/manager/dashboard/approval-summary")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.total").value(11));
    }

    @Test
    public void testGetTeamTodaySuccess() throws Exception {
        List<TeamTodayDto> today = List.of(new TeamTodayDto(1L, "John", "/api/v1/files/avatars/default.png", AttendanceStatus.PRESENT));
        when(managerDashboardService.getTeamToday(any(Employee.class))).thenReturn(today);

        mockMvc.perform(get("/api/v1/manager/dashboard/team-today")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].name").value("John"));
    }

    @Test
    public void testGetLeaveSummarySuccess() throws Exception {
        List<LeaveSummaryDto> summary = List.of(new LeaveSummaryDto(1L, "Priya Sharma", "Casual Leave", "2026-06-25", "2026-06-27", ApprovalStatus.PENDING));
        when(managerDashboardService.getLeaveSummary(any(Employee.class))).thenReturn(summary);

        mockMvc.perform(get("/api/v1/manager/dashboard/leave-summary")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].leaveType").value("Casual Leave"));
    }

    @Test
    public void testGetUpcomingEventsSuccess() throws Exception {
        List<UpcomingEventDto> events = List.of(new UpcomingEventDto("Priya Sharma - Birthday", "2026-06-25", "BIRTHDAY", "Celebrating birthday"));
        when(managerDashboardService.getUpcomingEvents(any(Employee.class))).thenReturn(events);

        mockMvc.perform(get("/api/v1/manager/dashboard/upcoming-events")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].type").value("BIRTHDAY"));
    }

    @Test
    public void testGetAlertsSuccess() throws Exception {
        List<AlertDto> alerts = List.of(new AlertDto("OVERTIME", "limit exceeded"));
        when(managerDashboardService.getAlerts(any(Employee.class))).thenReturn(alerts);

        mockMvc.perform(get("/api/v1/manager/dashboard/alerts")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].type").value("OVERTIME"));
    }

    @Test
    public void testGetInsightsSuccess() throws Exception {
        List<InsightDto> insights = List.of(new InsightDto(InsightSeverity.HIGH, "attendance low"));
        when(managerDashboardService.getInsights(any(Employee.class))).thenReturn(insights);

        mockMvc.perform(get("/api/v1/manager/dashboard/insights")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].severity").value("HIGH"));
    }

    @Test
    public void testGetQuickActionsSuccess() throws Exception {
        List<QuickActionDto> actions = List.of(new QuickActionDto("APPROVE_LEAVE", "Approve"));
        when(managerDashboardService.getQuickActions(any(Employee.class))).thenReturn(actions);

        mockMvc.perform(get("/api/v1/manager/dashboard/actions")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].code").value("APPROVE_LEAVE"));
    }

    @Test
    public void testGetNotificationsSuccess() throws Exception {
        List<NotificationDto> list = List.of(new NotificationDto(1L, "Pending", "body", "APPROVAL", "HIGH", false, "date"));
        when(managerDashboardService.getNotifications(any(Employee.class))).thenReturn(list);

        mockMvc.perform(get("/api/v1/manager/dashboard/notifications")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].title").value("Pending"));
    }

    @Test
    public void testGetScheduleSuccess() throws Exception {
        ScheduleSnapshotDto sched = new ScheduleSnapshotDto(12L, 8L, 3L, 1L);
        when(managerDashboardService.getScheduleSnapshot(any(Employee.class))).thenReturn(sched);

        mockMvc.perform(get("/api/v1/manager/dashboard/schedule")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.todayShifts").value(12));
    }

    @Test
    public void testGetAggregatedDashboardSuccess() throws Exception {
        ManagerDashboardResponse resp = new ManagerDashboardResponse(
                new DashboardMetadata(Instant.now(), 10L, 5),
                new SummaryDto(12L, 91.6, 1L, 42L, 10L, 1L),
                Collections.emptyList(),
                new TeamCompositionDto(10L, 1L, 1L),
                Collections.emptyList(),
                new PerformanceDto(92, 86, 79, 95),
                new PendingApprovalCountsDto(5L, 2L, 1L, 3L, 0L, 11L),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList()
        );
        when(managerDashboardService.getAggregatedDashboard(any(Employee.class), any())).thenReturn(resp);

        mockMvc.perform(get("/api/v1/manager/dashboard")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.metadata.managerId").value(10L));
    }

    @Test
    public void testRefreshDashboardSuccess() throws Exception {
        ManagerDashboardResponse resp = new ManagerDashboardResponse(
                new DashboardMetadata(Instant.now(), 10L, 5),
                new SummaryDto(12L, 91.6, 1L, 42L, 10L, 1L),
                Collections.emptyList(),
                new TeamCompositionDto(10L, 1L, 1L),
                Collections.emptyList(),
                new PerformanceDto(92, 86, 79, 95),
                new PendingApprovalCountsDto(5L, 2L, 1L, 3L, 0L, 11L),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList()
        );
        when(managerDashboardService.getAggregatedDashboard(any(Employee.class), any())).thenReturn(resp);

        mockMvc.perform(post("/api/v1/manager/dashboard/refresh")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.summary.teamSize").value(12));
    }

    @Test
    public void testGetDashboardUnauthorized() throws Exception {
        when(jwtService.validateAccessToken("invalid-token")).thenReturn(false);

        mockMvc.perform(get("/api/v1/manager/dashboard")
                .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testGetDashboardForbidden() throws Exception {
        User employee = new User();
        employee.setWorkEmail("emp@example.com");
        Role role = new Role();
        role.setName("EMPLOYEE");
        employee.setRole(role);

        when(jwtService.validateAccessToken("emp-token")).thenReturn(true);
        when(jwtService.getEmailFromToken("emp-token")).thenReturn("emp@example.com");
        when(userRepository.findByWorkEmail("emp@example.com")).thenReturn(Optional.of(employee));

        mockMvc.perform(get("/api/v1/manager/dashboard")
                .header("Authorization", "Bearer emp-token"))
                .andExpect(status().isForbidden());
    }
}
