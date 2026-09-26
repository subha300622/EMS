package com.example.ems.dashboard;

import com.example.ems.config.GlobalExceptionHandler;
import com.example.ems.employee.controller.TeamLeaderDashboardController;
import com.example.ems.employee.dto.teamleader.*;
import com.example.ems.employee.service.TeamLeaderDashboardService;
import com.example.ems.security.service.PermissionCheckService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class TeamLeaderDashboardControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TeamLeaderDashboardService dashboardService;

    @Mock
    private PermissionCheckService permissionCheckService;

    @InjectMocks
    private TeamLeaderDashboardController controller;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("tl@company.com", "password",
                        List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("team.dashboard.view")))
        );
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void testGetDashboard_Returns200AndExpectedStructure() throws Exception {
        TeamLeaderDashboardResponse.Summary summary = new TeamLeaderDashboardResponse.Summary(
                5, 5, 4, 80, 0,
                new TeamLeaderDashboardResponse.TeamPerformance(4.5, 5.0, 4.5)
        );
        TeamLeaderDashboardResponse mockResponse = new TeamLeaderDashboardResponse(
                summary, Collections.emptyList(), Collections.emptyList()
        );
        when(dashboardService.getDashboard()).thenReturn(mockResponse);

        mockMvc.perform(get("/api/v1/team-leader/dashboard")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary.teamMembers").value(5))
                .andExpect(jsonPath("$.summary.activeDirectReports").value(5))
                .andExpect(jsonPath("$.summary.presentToday").value(4))
                .andExpect(jsonPath("$.summary.attendancePercentage").value(80))
                .andExpect(jsonPath("$.summary.pendingLeaveReviews").value(0))
                .andExpect(jsonPath("$.summary.teamPerformance.rating").value(4.5))
                .andExpect(jsonPath("$.summary.teamPerformance.maxRating").value(5.0))
                .andExpect(jsonPath("$.summary.teamPerformance.sprintDeliveryIndex").value(4.5));
    }

    @Test
    void testGetTeamMembers_Returns200AndRosterList() throws Exception {
        TeamMemberRosterDto m1 = new TeamMemberRosterDto(101L, "EMP-001", "Dev One", "dev1@test.com", "Engineering", "Developer", "ACTIVE", "PRESENT", false);
        TeamMemberRosterDto m2 = new TeamMemberRosterDto(102L, "EMP-002", "Dev Two", "dev2@test.com", "Engineering", "Developer", "ACTIVE", "ABSENT", false);
        when(dashboardService.getTeamMembers()).thenReturn(List.of(m1, m2));

        mockMvc.perform(get("/api/v1/team-leader/team-members")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(101))
                .andExpect(jsonPath("$[0].name").value("Dev One"))
                .andExpect(jsonPath("$[1].id").value(102))
                .andExpect(jsonPath("$[1].name").value("Dev Two"));
    }

    @Test
    void testGetTeamMemberById_Returns200AndMemberDetails() throws Exception {
        TeamMemberRosterDto m1 = new TeamMemberRosterDto(101L, "EMP-001", "Dev One", "dev1@test.com", "Engineering", "Developer", "ACTIVE", "PRESENT", false);
        when(dashboardService.getTeamMemberById(101L)).thenReturn(m1);

        mockMvc.perform(get("/api/v1/team-leader/team-members/101")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(101))
                .andExpect(jsonPath("$.name").value("Dev One"))
                .andExpect(jsonPath("$.attendanceStatus").value("PRESENT"));
    }

    @Test
    void testRecommendLeave_ValidPayload_Returns200() throws Exception {
        TeamLeaveRecommendationRequest req = new TeamLeaveRecommendationRequest("RECOMMEND", "Team permits leave.");

        mockMvc.perform(post("/api/v1/team-leader/leave-reviews/501/recommend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        verify(dashboardService, times(1)).recommendLeave(eq(501L), any(TeamLeaveRecommendationRequest.class));
    }

    @Test
    void testRecommendLeave_InvalidPayloadBlankDecision_Returns400() throws Exception {
        TeamLeaveRecommendationRequest invalidReq = new TeamLeaveRecommendationRequest("", "Invalid");

        mockMvc.perform(post("/api/v1/team-leader/leave-reviews/501/recommend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidReq)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetAttendanceToday_Returns200() throws Exception {
        TeamLeaderAttendanceTodayResponse att = new TeamLeaderAttendanceTodayResponse(5, 2, 1, 1, 1, 60);
        when(dashboardService.getAttendanceToday()).thenReturn(att);

        mockMvc.perform(get("/api/v1/team-leader/attendance/today")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalMembers").value(5))
                .andExpect(jsonPath("$.present").value(2))
                .andExpect(jsonPath("$.remote").value(1))
                .andExpect(jsonPath("$.onLeave").value(1))
                .andExpect(jsonPath("$.absent").value(1))
                .andExpect(jsonPath("$.attendancePercentage").value(60));
    }

    @Test
    void testGetPerformance_Returns200() throws Exception {
        TeamLeaderPerformanceResponse perf = new TeamLeaderPerformanceResponse(
                4.5, 5.0, 4.5, List.of(new TeamLeaderPerformanceResponse.MemberPerformanceDto(101L, "Dev One", 4.5))
        );
        when(dashboardService.getPerformance()).thenReturn(perf);

        mockMvc.perform(get("/api/v1/team-leader/performance")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating").value(4.5))
                .andExpect(jsonPath("$.maxRating").value(5.0))
                .andExpect(jsonPath("$.sprintDeliveryIndex").value(4.5))
                .andExpect(jsonPath("$.memberRatings[0].employeeId").value(101));
    }
}
