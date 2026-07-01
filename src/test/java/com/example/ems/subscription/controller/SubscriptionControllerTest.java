package com.example.ems.subscription.controller;

import com.example.ems.auth.entity.Role;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.organization.dto.SubscriptionDtos.*;
import com.example.ems.security.service.JwtService;
import com.example.ems.subscription.service.SubscriptionService;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class SubscriptionControllerTest {

    private MockMvc mockMvc;

    @Mock
    private SubscriptionService subscriptionService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleService roleService;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private SubscriptionController controller;

    private static final String TOKEN = "admin-token";
    private static final String AUTH_HEADER = "Bearer " + TOKEN;
    private static final String EMAIL = "platform.admin@example.com";
    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        User adminUser = new User();
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
    public void testCreateSubscription() throws Exception {
        CreateSubscriptionRequest req = new CreateSubscriptionRequest(
                null,
                new PlanDto("ENTERPRISE", "Enterprise Plan"),
                new BillingDto("YEARLY", BigDecimal.valueOf(99999.00), "INR", BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.valueOf(99999.00)),
                new DurationDto(LocalDate.now(), LocalDate.now().plusYears(1), true, 365L),
                new LimitsDto(1000, 25, 100, 500, 1000000),
                new FeaturesDto(true, true, true, true, true, true, true, true, true, true, true, true, true),
                new PaymentDto("BANK_TRANSFER", "INV-2026-000123", "PAID"),
                "Notes"
        );

        SubscriptionResponse resp = new SubscriptionResponse(
                501L, 101L, "ACTIVE",
                new PlanDto("ENTERPRISE", "Enterprise Plan"),
                new BillingDto("YEARLY", BigDecimal.valueOf(99999.00), "INR", BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.valueOf(99999.00)),
                new DurationDto(LocalDate.now(), LocalDate.now().plusYears(1), true, 365L),
                new LimitsDto(1000, 25, 100, 500, 1000000),
                new PaymentDto("BANK_TRANSFER", "INV-2026-000123", "PAID"),
                "2026-07-01T09:45:12Z", "platform-admin"
        );

        when(subscriptionService.createSubscription(eq(101L), any(), any())).thenReturn(resp);

        mockMvc.perform(post("/api/v1/platform-admin/subscriptions/organization/101")
                        .header("Authorization", AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.subscriptionId").value(501));
    }

    @Test
    public void testCreateSubscriptionBodyBased() throws Exception {
        CreateSubscriptionRequest req = new CreateSubscriptionRequest(
                101L,
                new PlanDto("ENTERPRISE", "Enterprise Plan"),
                new BillingDto("YEARLY", BigDecimal.valueOf(99999.00), "INR", BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.valueOf(99999.00)),
                new DurationDto(LocalDate.now(), LocalDate.now().plusYears(1), true, 365L),
                new LimitsDto(1000, 25, 100, 500, 1000000),
                new FeaturesDto(true, true, true, true, true, true, true, true, true, true, true, true, true),
                new PaymentDto("BANK_TRANSFER", "INV-2026-000123", "PAID"),
                "Notes"
        );

        SubscriptionResponse resp = new SubscriptionResponse(
                501L, 101L, "ACTIVE",
                new PlanDto("ENTERPRISE", "Enterprise Plan"),
                new BillingDto("YEARLY", BigDecimal.valueOf(99999.00), "INR", BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.valueOf(99999.00)),
                new DurationDto(LocalDate.now(), LocalDate.now().plusYears(1), true, 365L),
                new LimitsDto(1000, 25, 100, 500, 1000000),
                new PaymentDto("BANK_TRANSFER", "INV-2026-000123", "PAID"),
                "2026-07-01T09:45:12Z", "platform-admin"
        );

        when(subscriptionService.createSubscription(eq(101L), any(), any())).thenReturn(resp);

        mockMvc.perform(post("/api/v1/platform-admin/subscriptions")
                        .header("Authorization", AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.subscriptionId").value(501));
    }

    @Test
    public void testUpgradeSubscription() throws Exception {
        UpgradeSubscriptionRequest req = new UpgradeSubscriptionRequest(
                "ULTIMATE", LocalDate.now(), "Upgraded", null, null, null
        );

        SubscriptionResponse resp = new SubscriptionResponse(
                501L, 101L, "ACTIVE",
                new PlanDto("ULTIMATE", "Ultimate Plan"),
                null, null, null, null, "2026-07-01T09:45:12Z", "platform-admin"
        );

        when(subscriptionService.upgradeSubscription(eq(501L), any(), any())).thenReturn(resp);

        mockMvc.perform(put("/api/v1/platform-admin/subscriptions/501/upgrade")
                        .header("Authorization", AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.plan.code").value("ULTIMATE"));
    }

    @Test
    public void testGetUsage() throws Exception {
        SubscriptionUsageResponse usage = new SubscriptionUsageResponse(
                101L, 501L,
                Map.of("employees", 1000L),
                Map.of("employees", 612L),
                Map.of("employees", 388L),
                Map.of("employees", 61.2),
                "2026-07-01"
        );

        when(subscriptionService.getUsage(501L)).thenReturn(usage);

        mockMvc.perform(get("/api/v1/platform-admin/subscriptions/501/usage")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.subscriptionId").value(501))
                .andExpect(jsonPath("$.data.limits.employees").value(1000));
    }

    @Test
    public void testDownloadInvoice() throws Exception {
        byte[] pdfBytes = "DummyPDFContent".getBytes();
        when(subscriptionService.downloadInvoice(123L)).thenReturn(pdfBytes);

        mockMvc.perform(get("/api/v1/platform-admin/subscriptions/invoices/123/download")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(contentLength(pdfBytes.length));
    }

    private org.springframework.test.web.servlet.ResultMatcher contentLength(int length) {
        return result -> org.junit.jupiter.api.Assertions.assertEquals(length, result.getResponse().getContentAsByteArray().length);
    }
}
