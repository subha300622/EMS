package com.example.ems.reports.controller;

import com.example.ems.auth.entity.Role;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.reports.subscription.facade.SubscriptionDashboardFacade;
import com.example.ems.reports.subscription.controller.PlatformSubscriptionDashboardController;
import com.example.ems.reports.subscription.dto.*;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PlatformSubscriptionDashboardControllerTest {

    private MockMvc mockMvc;

    @Mock
    private SubscriptionDashboardFacade dashboardFacade;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleService roleService;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private PlatformSubscriptionDashboardController controller;

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
    public void testGetSummary() throws Exception {
        SubscriptionDashboardSummary summary = new SubscriptionDashboardSummary(
                500, 450, 20, 20, 10, 5,
                BigDecimal.valueOf(90000), BigDecimal.valueOf(1080000), BigDecimal.valueOf(200)
        );
        when(dashboardFacade.getSummary()).thenReturn(summary);

        mockMvc.perform(get("/api/platform/reports/subscriptions/summary")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalOrganizations").value(500))
                .andExpect(jsonPath("$.data.activeSubscriptions").value(450));
    }

    @Test
    public void testGetGrowth() throws Exception {
        SubscriptionGrowthEntry entry = new SubscriptionGrowthEntry("2026-06", 10, 5, 1);
        when(dashboardFacade.getGrowth(any(), any(), any())).thenReturn(List.of(entry));

        mockMvc.perform(get("/api/platform/reports/subscriptions/growth")
                        .header("Authorization", AUTH_HEADER)
                        .param("period", "monthly"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].date").value("2026-06"))
                .andExpect(jsonPath("$.data[0].newSubscriptions").value(10));
    }

    @Test
    public void testGetStatus() throws Exception {
        SubscriptionStatusResponse statusResp = new SubscriptionStatusResponse(450, 20, 10, 5, 2);
        when(dashboardFacade.getStatusDistribution()).thenReturn(statusResp);

        mockMvc.perform(get("/api/platform/reports/subscriptions/status")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.active").value(450));
    }

    @Test
    public void testGetRevenueReport() throws Exception {
        RevenueReportEntry entry = new RevenueReportEntry("2026-06", BigDecimal.valueOf(80000), BigDecimal.valueOf(10000), BigDecimal.valueOf(90000));
        when(dashboardFacade.getRevenueReport(any(), any(), any())).thenReturn(List.of(entry));

        mockMvc.perform(get("/api/platform/reports/subscriptions/revenue")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].monthlyRevenue").value(80000));
    }

    @Test
    public void testGetPlans() throws Exception {
        PlanRevenueEntry entry = new PlanRevenueEntry("Professional Plan", 250, BigDecimal.valueOf(50000));
        when(dashboardFacade.getPlanRevenue()).thenReturn(List.of(entry));

        mockMvc.perform(get("/api/platform/reports/subscriptions/plans")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].planName").value("Professional Plan"));
    }

    @Test
    public void testGetDistribution() throws Exception {
        PlanDistributionEntry entry = new PlanDistributionEntry("Enterprise", 100, 20.0);
        when(dashboardFacade.getPlanDistribution()).thenReturn(List.of(entry));

        mockMvc.perform(get("/api/platform/reports/subscriptions/distribution")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].plan").value("Enterprise"));
    }

    @Test
    public void testGetConversion() throws Exception {
        SubscriptionConversionResponse conversion = new SubscriptionConversionResponse(100, 80, 80.0, 15.0);
        when(dashboardFacade.getConversion()).thenReturn(conversion);

        mockMvc.perform(get("/api/platform/reports/subscriptions/conversion")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.conversionRate").value(80.0));
    }

    @Test
    public void testGetChurn() throws Exception {
        SubscriptionChurnResponse churn = new SubscriptionChurnResponse(2.5, 10, 150, 97.5);
        when(dashboardFacade.getChurn()).thenReturn(churn);

        mockMvc.perform(get("/api/platform/reports/subscriptions/churn")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.churnRate").value(2.5));
    }
}
