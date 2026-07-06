package com.example.ems.reports.controller;

import com.example.ems.auth.entity.Role;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.reports.subscription.facade.SubscriptionReportFacade;
import com.example.ems.reports.subscription.controller.PlatformSubscriptionReportController;
import com.example.ems.reports.subscription.dto.*;
import com.example.ems.reports.subscription.validator.SubscriptionReportValidator;
import com.example.ems.security.service.JwtService;
import com.example.ems.audit.service.AuditLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class PlatformSubscriptionReportControllerTest {

    private MockMvc mockMvc;

    @Mock
    private SubscriptionReportFacade reportFacade;

    @Mock
    private SubscriptionReportValidator validator;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleService roleService;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private PlatformSubscriptionReportController controller;

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

        doNothing().when(validator).validateFilter(any());
        doNothing().when(validator).validateExport(any());
    }

    @Test
    public void testGetSubscriptionList() throws Exception {
        OrgSubscriptionListItem item = new OrgSubscriptionListItem(
                1L, "Test Org", "ENTERPRISE", "ACTIVE", "YEARLY",
                "2026-01-01", "2027-01-01", BigDecimal.valueOf(50000), true
        );
        PageImpl<OrgSubscriptionListItem> page = new PageImpl<>(List.of(item), PageRequest.of(0, 10), 1);
        when(reportFacade.getSubscriptionList(any())).thenReturn(page);

        mockMvc.perform(get("/api/platform/reports/subscriptions")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].organizationName").value("Test Org"))
                .andExpect(jsonPath("$.data.content[0].plan").value("ENTERPRISE"));
    }

    @Test
    public void testGetExpiringSubscriptions() throws Exception {
        ExpiringSubscriptionEntry entry = new ExpiringSubscriptionEntry(1L, "Expiring Org", "Starter", "2026-07-20", 17);
        PageImpl<ExpiringSubscriptionEntry> page = new PageImpl<>(List.of(entry), PageRequest.of(0, 10), 1);
        when(reportFacade.getExpiringSubscriptions(any(Integer.class), any())).thenReturn(page);

        mockMvc.perform(get("/api/platform/reports/subscriptions/expiring")
                        .header("Authorization", AUTH_HEADER)
                        .param("days", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].organizationName").value("Expiring Org"))
                .andExpect(jsonPath("$.data.content[0].daysRemaining").value(17));
    }

    @Test
    public void testGetTrialOrganizations() throws Exception {
        TrialOrganizationEntry entry = new TrialOrganizationEntry(1L, "Trial Org", "2026-06-15", "2026-07-15", 12);
        PageImpl<TrialOrganizationEntry> page = new PageImpl<>(List.of(entry), PageRequest.of(0, 10), 1);
        when(reportFacade.getTrialOrganizations(any())).thenReturn(page);

        mockMvc.perform(get("/api/platform/reports/subscriptions/trials")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].organizationName").value("Trial Org"))
                .andExpect(jsonPath("$.data.content[0].daysRemaining").value(12));
    }

    @Test
    public void testGetSubscriptionDetail() throws Exception {
        SubscriptionDetailResponse detail = new SubscriptionDetailResponse(
                1L, 2L, "Org Name", "ACTIVE", null, null, null, null, null, "2026-07-02", "system"
        );
        when(reportFacade.getSubscriptionDetail(2L)).thenReturn(detail);

        mockMvc.perform(get("/api/platform/reports/subscriptions/2")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.organizationName").value("Org Name"));
    }

    @Test
    public void testExportReport() throws Exception {
        byte[] csvBytes = "ID,Name\n1,Org".getBytes();
        when(reportFacade.exportReport(any(SubscriptionExportRequest.class))).thenReturn(csvBytes);

        mockMvc.perform(get("/api/platform/reports/subscriptions/export")
                        .header("Authorization", AUTH_HEADER)
                        .param("format", "CSV"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "form-data; name=\"attachment\"; filename=\"subscription-report.csv\""))
                .andExpect(content().contentType("text/csv"))
                .andExpect(content().bytes(csvBytes));
    }
}
