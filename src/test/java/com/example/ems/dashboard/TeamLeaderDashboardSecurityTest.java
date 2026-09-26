package com.example.ems.dashboard;

import com.example.ems.auth.service.PermissionRegistry;
import com.example.ems.config.GlobalExceptionHandler;
import com.example.ems.employee.controller.TeamLeaderDashboardController;
import com.example.ems.employee.dto.teamleader.TeamLeaderDashboardResponse;
import com.example.ems.employee.service.TeamLeaderDashboardService;
import com.example.ems.security.service.PermissionCheckService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class TeamLeaderDashboardSecurityTest {

    private MockMvc mockMvc;

    @Mock
    private TeamLeaderDashboardService dashboardService;

    @Mock
    private PermissionCheckService permissionCheckService;

    @InjectMocks
    private TeamLeaderDashboardController controller;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testGetDashboard_Unauthenticated_Returns401() throws Exception {
        SecurityContextHolder.clearContext();

        mockMvc.perform(get("/api/v1/team-leader/dashboard")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testGetDashboard_WithTeamDashboardPermission_Returns200() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("tl@company.com", "password", List.of(new SimpleGrantedAuthority("team.dashboard.view")))
        );

        doNothing().when(permissionCheckService).requireAnyPermission(
                PermissionRegistry.TEAM_DASHBOARD_VIEW, PermissionRegistry.TEAM_DASHBOARD_READ
        );
        when(dashboardService.getDashboard()).thenReturn(
                new TeamLeaderDashboardResponse(new TeamLeaderDashboardResponse.Summary(), Collections.emptyList(), Collections.emptyList())
        );

        mockMvc.perform(get("/api/v1/team-leader/dashboard")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testGetDashboard_WithoutPermission_Returns403() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user@company.com", "password", Collections.emptyList())
        );

        doThrow(new AccessDeniedException("Access Denied: Missing required permission"))
                .when(permissionCheckService).requireAnyPermission(
                        PermissionRegistry.TEAM_DASHBOARD_VIEW, PermissionRegistry.TEAM_DASHBOARD_READ
                );

        mockMvc.perform(get("/api/v1/team-leader/dashboard")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void testNormalEmployee_TryingTeamLeaderApi_Returns403() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("employee@company.com", "password", Collections.emptyList())
        );

        doThrow(new AccessDeniedException("Access Denied: Normal employee cannot access Team Leader API"))
                .when(permissionCheckService).requireAnyPermission(
                        PermissionRegistry.TEAM_DASHBOARD_VIEW, PermissionRegistry.TEAM_DASHBOARD_READ
                );

        mockMvc.perform(get("/api/v1/team-leader/dashboard")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}
