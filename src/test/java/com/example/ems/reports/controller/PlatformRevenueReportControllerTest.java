package com.example.ems.reports.controller;

import com.example.ems.auth.entity.Role;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.reports.revenue.controller.PlatformRevenueReportController;
import com.example.ems.reports.revenue.dto.*;
import com.example.ems.reports.revenue.facade.RevenueReportFacade;
import com.example.ems.reports.revenue.validator.RevenueReportValidator;
import com.example.ems.security.service.JwtService;
import com.example.ems.audit.service.AuditLogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class PlatformRevenueReportControllerTest {

    private MockMvc mockMvc;

    @Mock
    private RevenueReportFacade reportFacade;

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
    private PlatformRevenueReportController controller;

    private static final String TOKEN = "admin-token";
    private static final String AUTH_HEADER = "Bearer " + TOKEN;
    private static final String EMAIL = "platform.admin@example.com";

    private User adminUser;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

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

        doNothing().when(reportValidator).validateFilter(any(RevenueFilterRequest.class));
        doNothing().when(reportValidator).validateExport(any(RevenueExportRequest.class));
    }

    @Test
    public void testGetPaymentsReport() throws Exception {
        RevenuePaymentResponse p = new RevenuePaymentResponse(
            1L, 10L, "Acme", "PRO", "INV-001", "STRIPE", "SUCCESS", "USD",
            BigDecimal.valueOf(100), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.valueOf(100), "2026-07-03"
        );
        when(reportFacade.getPaymentsReport(any())).thenReturn(new PageImpl<>(List.of(p), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/v1/platform/revenue/reports/payments")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].organizationName").value("Acme"));
    }

    @Test
    public void testGetInvoicesReport() throws Exception {
        RevenueInvoiceResponse inv = new RevenueInvoiceResponse(
            "INV-001", 10L, "Acme", "PRO", "2026-07-03", "2026-08-03", "PAID",
            BigDecimal.valueOf(100), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.valueOf(100)
        );
        when(reportFacade.getInvoicesReport(any())).thenReturn(new PageImpl<>(List.of(inv), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/v1/platform/revenue/reports/invoices")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].invoiceNumber").value("INV-001"));
    }

    @Test
    public void testExportReport() throws Exception {
        byte[] csvBytes = "ID,Org\n1,Acme".getBytes();
        when(reportFacade.exportReport(any(RevenueExportRequest.class))).thenReturn(csvBytes);

        RevenueExportRequest req = new RevenueExportRequest();
        req.setType("PAYMENTS");
        req.setFormat("CSV");

        mockMvc.perform(post("/api/v1/platform/revenue/reports/export")
                        .header("Authorization", AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(OBJECT_MAPPER.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "form-data; name=\"attachment\"; filename=\"revenue-payments-report.csv\""))
                .andExpect(content().contentType("text/csv"));
    }
}
