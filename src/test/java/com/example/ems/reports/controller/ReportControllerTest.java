package com.example.ems.reports.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
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

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ReportControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleService roleService;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private ReportController reportController;

    private static final String TOKEN = "mock-token";
    private static final String AUTH_HEADER = "Bearer " + TOKEN;
    private static final String EMAIL = "admin@example.com";

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(reportController).build();

        when(jwtService.validateAccessToken(TOKEN)).thenReturn(true);
        when(jwtService.getEmailFromToken(TOKEN)).thenReturn(EMAIL);
        User user = new User();
        user.setWorkEmail(EMAIL);
        user.setFullName("System Admin");
        when(userRepository.findByWorkEmail(EMAIL)).thenReturn(Optional.of(user));
    }

    @Test
    public void testGetDashboardSummary() throws Exception {
        mockMvc.perform(get("/api/v1/reports/dashboard-summary")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.employeeCost").value(2840000));
    }

    @Test
    public void testGetPayrollCostTrend() throws Exception {
        mockMvc.perform(get("/api/v1/reports/payroll-cost-trend")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.labels[0]").value("May"));
    }

    @Test
    public void testGetDepartmentCostDistribution() throws Exception {
        mockMvc.perform(get("/api/v1/reports/department-cost-distribution")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalCost").value(2840000));
    }

    @Test
    public void testGetPayrollReportTab() throws Exception {
        mockMvc.perform(get("/api/v1/reports/payroll")
                        .header("Authorization", AUTH_HEADER)
                        .param("fromDate", "2026-01-01")
                        .param("toDate", "2026-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.payrollRunStatus").value("COMPLETED"));
    }

    @Test
    public void testGetExpenseReportTab() throws Exception {
        mockMvc.perform(get("/api/v1/reports/expenses")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.approvedExpenses").value(150000));
    }

    @Test
    public void testGetTaxReportTab() throws Exception {
        mockMvc.perform(get("/api/v1/reports/tax")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.tds").value(250000));
    }

    @Test
    public void testGetAssetReportTab() throws Exception {
        mockMvc.perform(get("/api/v1/reports/assets")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.assetValue").value(48000000L));
    }

    @Test
    public void testBuildCustomReport() throws Exception {
        Map<String, Object> body = Map.of(
                "name", "Dept Payroll",
                "module", "PAYROLL",
                "columns", List.of("employeeName", "grossSalary")
        );

        mockMvc.perform(post("/api/v1/reports/custom")
                        .header("Authorization", AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Dept Payroll"));
    }

    @Test
    public void testGetReportHistory() throws Exception {
        mockMvc.perform(get("/api/v1/reports/history")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].reportName").value("Payroll Report"));
    }

    @Test
    public void testExportAndDownloadReport() throws Exception {
        Map<String, Object> body = Map.of(
                "reportType", "PAYROLL",
                "format", "EXCEL",
                "period", "MONTH"
        );

        String response = mockMvc.perform(post("/api/v1/reports/export")
                        .header("Authorization", AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.exportId").exists())
                .andReturn().getResponse().getContentAsString();

        Map<String, Object> data = (Map<String, Object>) new ObjectMapper().readValue(response, Map.class).get("data");
        String exportId = (String) data.get("exportId");

        mockMvc.perform(get("/api/v1/reports/export/" + exportId)
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk());
    }

    @Test
    public void testScheduledReportsCrud() throws Exception {
        Map<String, Object> body = Map.of(
                "reportType", "PAYROLL",
                "frequency", "WEEKLY",
                "emailRecipients", List.of("test@example.com")
        );

        // 1. Create Schedule
        String res = mockMvc.perform(post("/api/v1/reports/schedules")
                        .header("Authorization", AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").exists())
                .andReturn().getResponse().getContentAsString();

        Map<String, Object> data = (Map<String, Object>) new ObjectMapper().readValue(res, Map.class).get("data");
        Number id = (Number) data.get("id");
        Long scheduleId = id.longValue();

        // 2. Get Schedules
        mockMvc.perform(get("/api/v1/reports/schedules")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 3. Update Schedule
        Map<String, Object> updateBody = Map.of("frequency", "DAILY");
        mockMvc.perform(put("/api/v1/reports/schedules/" + scheduleId)
                        .header("Authorization", AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(updateBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.frequency").value("DAILY"));

        // 4. Delete Schedule
        mockMvc.perform(delete("/api/v1/reports/schedules/" + scheduleId)
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testGetBranchesDropdown() throws Exception {
        mockMvc.perform(get("/api/v1/branches/dropdown")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].name").value("Headquarters"));
    }

    @Test
    public void testGetCategoriesAndPeriods() throws Exception {
        mockMvc.perform(get("/api/v1/reports/categories")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0]").value("PAYROLL"));

        mockMvc.perform(get("/api/v1/reports/periods")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0]").value("MONTH"));
    }
}
