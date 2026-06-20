package com.example.ems.payroll.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.payroll.dto.*;
import com.example.ems.payroll.service.MyPayslipService;
import com.example.ems.security.service.JwtService;

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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class MyPayslipControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private MyPayslipService myPayslipService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private RoleService roleService;

    @InjectMocks
    private MyPayslipController myPayslipController;

    private static final String TOKEN = "mock-token";
    private static final String AUTH_HEADER = "Bearer " + TOKEN;
    private static final String EMAIL = "employee@example.com";

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        mockMvc = MockMvcBuilders.standaloneSetup(myPayslipController).build();

        // Standard auth mock setup
        when(jwtService.validateAccessToken(TOKEN)).thenReturn(true);
        when(jwtService.getEmailFromToken(TOKEN)).thenReturn(EMAIL);
        User user = new User();
        user.setWorkEmail(EMAIL);
        when(userRepository.findByWorkEmail(EMAIL)).thenReturn(Optional.of(user));
    }

    private void mockPermission(String permission, boolean allowed) {
        when(roleService.hasPermission(EMAIL, permission)).thenReturn(allowed);
    }



    @Test
    public void testGetHistorySuccess() throws Exception {
        mockPermission("payslip.self.read", true);

        MyPayslipHistoryResponse.PayslipItem item = new MyPayslipHistoryResponse.PayslipItem(
                1L, "PS-001", "June 2026", BigDecimal.valueOf(100000), BigDecimal.valueOf(10000), BigDecimal.valueOf(90000),
                LocalDate.now(), "PAID", new MyPayslipHistoryResponse.ActionInfo(true, true)
        );
        MyPayslipHistoryResponse.PaginationInfo pag = new MyPayslipHistoryResponse.PaginationInfo(0, 10, 1, 1);
        MyPayslipHistoryResponse resp = new MyPayslipHistoryResponse(List.of(item), pag);

        when(myPayslipService.getPayslipHistory(eq(EMAIL), any(), any(), any(), anyInt(), anyInt(), any())).thenReturn(resp);

        mockMvc.perform(get("/api/v1/my-payslips/history")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.data[0].payslipNumber").value("PS-001"));
    }

    @Test
    public void testGetDetailsSuccess() throws Exception {
        mockPermission("payslip.self.read", true);

        MyPayslipDetailsResponse resp = new MyPayslipDetailsResponse();
        resp.setPayslipId(1L);
        resp.setPayslipNumber("PS-001");

        when(myPayslipService.getPayslipDetails(EMAIL, 1L)).thenReturn(resp);

        mockMvc.perform(get("/api/v1/my-payslips/1")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.payslipNumber").value("PS-001"));
    }

    @Test
    public void testPreviewSuccess() throws Exception {
        mockPermission("payslip.self.preview", true);

        MyPayslipPreviewResponse resp = new MyPayslipPreviewResponse(1L, "PS-001", "/url", "1 Hour");
        when(myPayslipService.previewPayslip(EMAIL, 1L)).thenReturn(resp);

        mockMvc.perform(get("/api/v1/my-payslips/1/preview")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.previewUrl").value("/url"));
    }

    @Test
    public void testDownloadSuccess() throws Exception {
        mockPermission("payslip.self.download", true);

        byte[] pdfBytes = "%PDF-1.4 mock pdf content".getBytes();
        when(myPayslipService.getPayslipPdf(EMAIL, 1L)).thenReturn(pdfBytes);

        mockMvc.perform(get("/api/v1/my-payslips/1/download")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(status().is(200));
    }

    @Test
    public void testGetAnnualSuccess() throws Exception {
        mockPermission("payslip.self.read", true);

        AnnualSalaryStatementResponse resp = new AnnualSalaryStatementResponse();
        resp.setFinancialYear("FY 2025-26");

        when(myPayslipService.getAnnualStatement(eq(EMAIL), any())).thenReturn(resp);

        mockMvc.perform(get("/api/v1/my-payslips/annual-statement")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.financialYear").value("FY 2025-26"));
    }

    @Test
    public void testDownloadAnnualSuccess() throws Exception {
        mockPermission("payslip.self.download", true);

        byte[] pdfBytes = "%PDF-1.4 mock statement pdf content".getBytes();
        when(myPayslipService.getAnnualStatementPdf(eq(EMAIL), any())).thenReturn(pdfBytes);

        mockMvc.perform(get("/api/v1/my-payslips/annual-statement/download")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(status().is(200));
    }

    @Test
    public void testGetSalaryRevisionsSuccess() throws Exception {
        mockPermission("payslip.self.read", true);

        MySalaryRevisionsResponse resp = new MySalaryRevisionsResponse();
        when(myPayslipService.getSalaryRevisionHistory(EMAIL)).thenReturn(resp);

        mockMvc.perform(get("/api/v1/my-payslips/salary-revisions")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testGetTaxSuccess() throws Exception {
        mockPermission("payslip.self.read", true);

        TaxSummaryResponse resp = new TaxSummaryResponse();
        when(myPayslipService.getTaxSummary(EMAIL)).thenReturn(resp);

        mockMvc.perform(get("/api/v1/my-payslips/tax-summary")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testEmailSuccess() throws Exception {
        mockPermission("payslip.self.export", true);

        EmailPayslipRequest req = new EmailPayslipRequest();
        req.setEmail("target@example.com");

        EmailPayslipResponse resp = new EmailPayslipResponse("Emailed", 1L, LocalDateTime.now());
        when(myPayslipService.emailPayslip(eq(EMAIL), eq(1L), eq("target@example.com"))).thenReturn(resp);

        mockMvc.perform(post("/api/v1/my-payslips/1/email")
                .header("Authorization", AUTH_HEADER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testGetTimelineSuccess() throws Exception {
        mockPermission("payslip.self.read", true);

        PayrollTimelineResponse resp = new PayrollTimelineResponse();
        when(myPayslipService.getPayrollTimeline(EMAIL)).thenReturn(resp);

        mockMvc.perform(get("/api/v1/my-payslips/timeline")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
