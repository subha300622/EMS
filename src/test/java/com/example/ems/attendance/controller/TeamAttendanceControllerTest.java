package com.example.ems.attendance.controller;

import com.example.ems.attendance.dto.TeamMemberAttendanceDto;
import com.example.ems.attendance.dto.TeamSummaryDto;
import com.example.ems.attendance.dto.TeamTrendDto;
import com.example.ems.attendance.service.TeamAttendanceService;
import com.example.ems.attendance.service.TeamResolutionService;
import com.example.ems.attendance.service.TeamAttendanceCalendarService;
import com.example.ems.attendance.dto.*;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.security.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TeamAttendanceControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TeamResolutionService teamResolutionService;

    @Mock
    private TeamAttendanceService teamAttendanceService;

    @Mock
    private TeamAttendanceCalendarService teamAttendanceCalendarService;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleService roleService;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private TeamAttendanceController teamAttendanceController;

    private static final String TOKEN = "mock-token";
    private static final String AUTH_HEADER = "Bearer " + TOKEN;
    private static final String EMAIL = "hr@company.com";
    private static final String EMP_EMAIL = "emp@company.com";

    private User hrUser;
    private User empUser;
    private Employee hrEmployee;
    private Employee regularEmployee;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(teamAttendanceController).build();

        hrUser = new User();
        hrUser.setId(1L);
        hrUser.setWorkEmail(EMAIL);
        hrUser.setFullName("HR Manager");

        empUser = new User();
        empUser.setId(2L);
        empUser.setWorkEmail(EMP_EMAIL);
        empUser.setFullName("Regular Employee");

        hrEmployee = new Employee();
        hrEmployee.setId(10L);
        hrEmployee.setEmail(EMAIL);
        hrEmployee.setFullName("HR Manager");

        regularEmployee = new Employee();
        regularEmployee.setId(20L);
        regularEmployee.setEmail(EMP_EMAIL);
        regularEmployee.setFullName("Regular Employee");
    }

    private void mockAuth(User user, Employee employee, boolean isAdmin) {
        when(jwtService.validateAccessToken(TOKEN)).thenReturn(true);
        when(jwtService.getEmailFromToken(TOKEN)).thenReturn(user.getWorkEmail());
        when(userRepository.findByWorkEmail(user.getWorkEmail())).thenReturn(Optional.of(user));
        when(employeeRepository.findByEmail(user.getWorkEmail())).thenReturn(Optional.of(employee));
        when(roleService.hasPermission(user.getWorkEmail(), "attendance.read")).thenReturn(isAdmin);
        when(roleService.hasRoleOrGreater(user, "HR")).thenReturn(isAdmin);
    }

    @Test
    public void testGetTeamAttendanceSuccessGlobal() throws Exception {
        mockAuth(hrUser, hrEmployee, true);

        List<Employee> emps = List.of(regularEmployee);
        when(teamResolutionService.resolveEmployees(eq("global"), any(), any(), any(), any())).thenReturn(emps);

        TeamMemberAttendanceDto dto = new TeamMemberAttendanceDto(
                20L, "Regular Employee", "Developer", "OFFICE", List.of()
        );
        when(teamAttendanceService.getTeamAttendance(eq(emps), any(), any())).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/team-attendance")
                        .header("Authorization", AUTH_HEADER)
                        .param("scope", "global"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].employeeId").value(20L))
                .andExpect(jsonPath("$.data[0].name").value("Regular Employee"));
    }

    @Test
    public void testGetTeamAttendanceForbiddenGlobal() throws Exception {
        mockAuth(empUser, regularEmployee, false);

        mockMvc.perform(get("/api/v1/team-attendance")
                        .header("Authorization", AUTH_HEADER)
                        .param("scope", "global"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("AUTH_002"));
    }

    @Test
    public void testGetTeamAttendanceMissingParam() throws Exception {
        mockAuth(hrUser, hrEmployee, true);

        mockMvc.perform(get("/api/v1/team-attendance")
                        .header("Authorization", AUTH_HEADER)
                        .param("scope", "employee"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("VAL_001"));
    }

    @Test
    public void testGetTeamAttendanceDateRangeLimit() throws Exception {
        mockAuth(hrUser, hrEmployee, true);

        mockMvc.perform(get("/api/v1/team-attendance")
                        .header("Authorization", AUTH_HEADER)
                        .param("scope", "global")
                        .param("from", "2026-06-01")
                        .param("to", "2026-07-15"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("VAL_002"));
    }

    @Test
    public void testGetTeamAttendanceSummarySuccess() throws Exception {
        mockAuth(hrUser, hrEmployee, true);

        List<Employee> emps = List.of(regularEmployee);
        when(teamResolutionService.resolveEmployees(eq("global"), any(), any(), any(), any())).thenReturn(emps);

        TeamSummaryDto summary = new TeamSummaryDto(
                "2026-06-23", 1, 1, 0, 0, 0
        );
        when(teamAttendanceService.getTeamAttendanceSummary(eq(emps), any())).thenReturn(summary);

        mockMvc.perform(get("/api/v1/team-attendance/summary")
                        .header("Authorization", AUTH_HEADER)
                        .param("scope", "global")
                        .param("date", "2026-06-23"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.present").value(1))
                .andExpect(jsonPath("$.data.totalMembers").value(1));
    }

    @Test
    public void testGetTeamAttendanceTrendSuccess() throws Exception {
        mockAuth(hrUser, hrEmployee, true);

        List<Employee> emps = List.of(regularEmployee);
        when(teamResolutionService.resolveEmployees(eq("global"), any(), any(), any(), any())).thenReturn(emps);

        TeamTrendDto trend = new TeamTrendDto(
                List.of("2026-06-23"), List.of(1L), List.of(0L), List.of(0L), List.of(0L)
        );
        when(teamAttendanceService.getTeamAttendanceTrend(eq(emps), any(), any())).thenReturn(trend);

        mockMvc.perform(get("/api/v1/team-attendance/trend")
                        .header("Authorization", AUTH_HEADER)
                        .param("scope", "global")
                        .param("from", "2026-06-23")
                        .param("to", "2026-06-23"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.labels[0]").value("2026-06-23"))
                .andExpect(jsonPath("$.data.presentCount[0]").value(1));
    }

    @Test
    public void testGetTeamMonthlyCalendarSuccess() throws Exception {
        mockAuth(hrUser, hrEmployee, true);

        List<Employee> emps = List.of(regularEmployee);
        when(teamResolutionService.resolveEmployees(eq("global"), any(), any(), any(), any())).thenReturn(emps);

        TeamCalendarGridDto grid = new TeamCalendarGridDto(
                "2026-06", null, null, List.of(
                        new TeamCalendarGridDto.CalendarDayDto(
                                "2026-06-01", "MONDAY",
                                new TeamCalendarGridDto.DaySummaryDto(1, 0, 0, 0),
                                List.of(new TeamCalendarGridDto.EmployeeDayDto(20L, "Regular Employee", "PRESENT", "09:10", "18:05"))
                        )
                )
        );
        when(teamAttendanceCalendarService.getTeamMonthlyCalendar(eq(emps), any(), any(), any(), any(), eq("full"))).thenReturn(grid);

        mockMvc.perform(get("/api/v1/team-attendance/calendar/monthly")
                        .header("Authorization", AUTH_HEADER)
                        .param("month", "2026-06")
                        .param("view", "full"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.month").value("2026-06"))
                .andExpect(jsonPath("$.data.calendar[0].summary.present").value(1))
                .andExpect(jsonPath("$.data.calendar[0].employees[0].name").value("Regular Employee"));
    }

    @Test
    public void testGetTeamMonthlyCalendarMissingMonth() throws Exception {
        mockAuth(hrUser, hrEmployee, true);

        mockMvc.perform(get("/api/v1/team-attendance/calendar/monthly")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("VAL_001"));
    }

    @Test
    public void testGetTeamHeatmapSuccess() throws Exception {
        mockAuth(hrUser, hrEmployee, true);

        List<Employee> emps = List.of(regularEmployee);
        when(teamResolutionService.resolveEmployees(eq("global"), any(), any(), any(), any())).thenReturn(emps);

        TeamHeatmapDto heatmap = new TeamHeatmapDto(
                "2026-06", List.of(
                        new TeamHeatmapDto.HeatmapDayDto("2026-06-01", 100, "GREEN")
                )
        );
        when(teamAttendanceCalendarService.getTeamHeatmap(eq(emps), any(), any())).thenReturn(heatmap);

        mockMvc.perform(get("/api/v1/team-attendance/calendar/heatmap")
                        .header("Authorization", AUTH_HEADER)
                        .param("month", "2026-06"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.month").value("2026-06"))
                .andExpect(jsonPath("$.data.data[0].status").value("GREEN"));
    }

    @Test
    public void testGetEmployeeMonthlyCalendarSuccess() throws Exception {
        mockAuth(empUser, regularEmployee, false);

        when(employeeRepository.findById(20L)).thenReturn(Optional.of(regularEmployee));

        EmployeeCalendarDto empCal = new EmployeeCalendarDto(
                20L, "2026-06", List.of(
                        new EmployeeCalendarDto.EmployeeDayRecordDto("2026-06-01", "PRESENT", "09:10", "18:05")
                )
        );
        when(teamAttendanceCalendarService.getEmployeeMonthlyCalendar(eq(regularEmployee), any(), any())).thenReturn(empCal);

        mockMvc.perform(get("/api/v1/team-attendance/calendar/employee/20")
                        .header("Authorization", AUTH_HEADER)
                        .param("month", "2026-06"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.employeeId").value(20L))
                .andExpect(jsonPath("$.data.calendar[0].status").value("PRESENT"));
    }

    @Test
    public void testGetEmployeeMonthlyCalendarForbidden() throws Exception {
        mockAuth(empUser, regularEmployee, false);

        mockMvc.perform(get("/api/v1/team-attendance/calendar/employee/10")
                        .header("Authorization", AUTH_HEADER)
                        .param("month", "2026-06"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("AUTH_002"));
    }

    @Test
    public void testGetDepartmentCalendarSummarySuccess() throws Exception {
        mockAuth(hrUser, hrEmployee, true);

        List<Employee> emps = List.of(regularEmployee);
        when(teamResolutionService.resolveEmployees(eq("global"), any(), any(), any(), any())).thenReturn(emps);

        DepartmentCalendarSummaryDto summary = new DepartmentCalendarSummaryDto(
                null, null, "2026-06",
                new DepartmentCalendarSummaryDto.TotalsDto(22, 100.0, 1),
                new DepartmentCalendarSummaryDto.TrendDto("2026-06-10", "2026-06-03")
        );
        when(teamAttendanceCalendarService.getDepartmentCalendarSummary(eq(emps), any(), any(), any(), any())).thenReturn(summary);

        mockMvc.perform(get("/api/v1/team-attendance/calendar/summary")
                        .header("Authorization", AUTH_HEADER)
                        .param("month", "2026-06"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totals.workingDays").value(22))
                .andExpect(jsonPath("$.data.totals.avgAttendance").value(100.0))
                .andExpect(jsonPath("$.data.trend.bestDay").value("2026-06-10"));
    }
}
