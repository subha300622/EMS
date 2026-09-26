package com.example.ems.reports.controller;

import com.example.ems.auth.entity.Role;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.reports.organization.DashboardFacade;
import com.example.ems.reports.organization.controller.PlatformOrganizationDashboardController;
import com.example.ems.reports.organization.dto.ChartResponse;
import com.example.ems.reports.organization.dto.DashboardSummaryResponse;
import com.example.ems.reports.organization.dto.DistributionResponse;
import com.example.ems.security.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PlatformOrganizationDashboardControllerTest {

    private MockMvc mockMvc;

    @Mock
    private DashboardFacade dashboardFacade;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleService roleService;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private PlatformOrganizationDashboardController controller;

    private static final String TOKEN = "admin-token";
    private static final String AUTH_HEADER = "Bearer " + TOKEN;
    private static final String EMAIL = "platform.admin@example.com";

    private User adminUser;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        adminUser = new User();
        adminUser.setWorkEmail(EMAIL);
        Role role = new Role();
        role.setName("SUPER_ADMIN");
        adminUser.setRole(role);

        when(jwtService.validateAccessToken(TOKEN)).thenReturn(true);
        when(jwtService.getEmailFromToken(TOKEN)).thenReturn(EMAIL);
        when(userRepository.findByWorkEmail(EMAIL)).thenReturn(Optional.of(adminUser));
        when(roleService.hasPermission(eq(EMAIL), any())).thenReturn(true);
    }

    @Test
    public void testGetDashboard() throws Exception {
        DashboardSummaryResponse response = new DashboardSummaryResponse(10, 8, 2, 0, 100, 80, 12.4, 15.0);
        when(dashboardFacade.getDashboard()).thenReturn(response);

        mockMvc.perform(get("/api/v1/platform/dashboard/organizations")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalOrganizations").value(10))
                .andExpect(jsonPath("$.data.activeOrganizations").value(8));
    }

    @Test
    public void testGetGrowth() throws Exception {
        ChartResponse monthly = new ChartResponse(List.of("Jan"), List.of(10));
        when(dashboardFacade.getGrowth()).thenReturn(Map.of("monthly", monthly));

        mockMvc.perform(get("/api/v1/platform/dashboard/organizations/growth")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.monthly.labels[0]").value("Jan"));
    }

    @Test
    public void testGetStatusDistribution() throws Exception {
        DistributionResponse dist = new DistributionResponse("Active", 8);
        when(dashboardFacade.getStatusDistribution()).thenReturn(List.of(dist));

        mockMvc.perform(get("/api/v1/platform/dashboard/organizations/status-distribution")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].name").value("Active"))
                .andExpect(jsonPath("$.data[0].count").value(8));
    }
}
