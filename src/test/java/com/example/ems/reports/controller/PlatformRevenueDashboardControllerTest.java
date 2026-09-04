package com.example.ems.reports.controller;

import com.example.ems.auth.entity.Role;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.reports.revenue.controller.PlatformRevenueDashboardController;
import com.example.ems.reports.revenue.dto.*;
import com.example.ems.reports.revenue.facade.RevenueDashboardFacade;
import com.example.ems.reports.revenue.validator.RevenueReportValidator;
import com.example.ems.security.service.JwtService;
import com.example.ems.audit.service.AuditLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PlatformRevenueDashboardControllerTest {

    private MockMvc mockMvc;

    @Mock
    private RevenueDashboardFacade dashboardFacade;

    @Mock
    private RevenueReportValidator reportValidator;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleService roleService;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private PlatformRevenueDashboardController controller;

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

        doNothing().when(reportValidator).validateHorizon(any(Integer.class));
    }

    @Test
    public void testGetDashboard() throws Exception {
        RevenueSummaryResponse summary = new RevenueSummaryResponse();
        summary.setTotalRevenue(BigDecimal.valueOf(100000));
        when(dashboardFacade.getSummary()).thenReturn(summary);
        when(dashboardFacade.getTrends()).thenReturn(List.of());
        when(dashboardFacade.getGrowth()).thenReturn(List.of());
        when(dashboardFacade.getForecast(6)).thenReturn(new RevenueForecastResponse(6, 95.0, List.of()));

        mockMvc.perform(get("/api/v1/platform/revenue/dashboard")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.kpis.totalRevenue").value(100000));
    }

    @Test
    public void testGetSummary() throws Exception {
        RevenueSummaryResponse summary = new RevenueSummaryResponse();
        summary.setTotalRevenue(BigDecimal.valueOf(100000));
        when(dashboardFacade.getSummary()).thenReturn(summary);

        mockMvc.perform(get("/api/v1/platform/revenue/dashboard/summary")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalRevenue").value(100000));
    }

    @Test
    public void testGetTrends() throws Exception {
        RevenueTrendResponse entry = new RevenueTrendResponse("2026-06", BigDecimal.valueOf(80000), BigDecimal.valueOf(80000), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        when(dashboardFacade.getTrends()).thenReturn(List.of(entry));

        mockMvc.perform(get("/api/v1/platform/revenue/dashboard/trends")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].period").value("2026-06"));
    }

    @Test
    public void testGetForecast() throws Exception {
        RevenueForecastResponse forecast = new RevenueForecastResponse(6, 85.0, List.of());
        when(dashboardFacade.getForecast(6)).thenReturn(forecast);

        mockMvc.perform(get("/api/v1/platform/revenue/dashboard/forecast")
                        .header("Authorization", AUTH_HEADER)
                        .param("horizon", "6"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
