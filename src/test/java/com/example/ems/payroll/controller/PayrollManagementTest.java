package com.example.ems.payroll.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.payroll.dto.SalaryStructureRequest;
import com.example.ems.payroll.entity.Payroll;
import com.example.ems.payroll.entity.SalaryStructure;
import com.example.ems.payroll.service.PayrollService;
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
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PayrollManagementTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    @Mock
    private PayrollService payrollService;

    @Mock
    private RoleService roleService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private PayrollController payrollController;

    private static final String TOKEN = "mock-token";
    private static final String AUTH_HEADER = "Bearer " + TOKEN;
    private static final String EMAIL = "finance@company.com";

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(payrollController).build();

        when(jwtService.validateAccessToken(TOKEN)).thenReturn(true);
        when(jwtService.getEmailFromToken(TOKEN)).thenReturn(EMAIL);
        User user = new User();
        user.setWorkEmail(EMAIL);
        user.setFullName("Finance Lead");
        when(userRepository.findByWorkEmail(EMAIL)).thenReturn(Optional.of(user));
        when(roleService.hasPermission(EMAIL, "payroll.manage")).thenReturn(true);
    }

    @Test
    public void testGetPayrollDashboard() throws Exception {
        Map<String, Object> dashboard = Map.of(
                "employeesPaid", 250,
                "grossPayroll", 8500000.0,
                "netPayroll", 7200000.0,
                "pendingPayrolls", 12
        );
        when(payrollService.getPayrollDashboard()).thenReturn(dashboard);

        mockMvc.perform(get("/api/v1/payroll/dashboard")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.employeesPaid").value(250));
    }

    @Test
    public void testSaveSalaryStructure() throws Exception {
        SalaryStructureRequest request = new SalaryStructureRequest(101L, BigDecimal.valueOf(50000), BigDecimal.valueOf(20000), BigDecimal.valueOf(10000));
        SalaryStructure ss = new SalaryStructure(101L, BigDecimal.valueOf(50000), BigDecimal.valueOf(20000), BigDecimal.valueOf(10000));
        when(payrollService.saveSalaryStructure(any(SalaryStructureRequest.class))).thenReturn(ss);

        mockMvc.perform(post("/api/v1/payroll/salary-structures")
                        .header("Authorization", AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.basicSalary").value(50000));
    }

    @Test
    public void testProcessPayrollRun() throws Exception {
        Map<String, Object> body = Map.of("month", "2026-06", "departmentId", 1L);
        when(payrollService.processPayrollRun(eq("2026-06"), eq(1L))).thenReturn(Map.of("processedEmployees", 250, "status", "SUCCESS"));

        mockMvc.perform(post("/api/v1/payroll/process")
                        .header("Authorization", AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("SUCCESS"));
    }

    @Test
    public void testGetCalculationPreview() throws Exception {
        when(payrollService.calculatePreview(101L)).thenReturn(Map.of("grossSalary", 80000, "netSalary", 70200));

        mockMvc.perform(get("/api/v1/payroll/101/calculation")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.netSalary").value(70200));
    }

    @Test
    public void testApprovePayrollRun() throws Exception {
        Payroll p = new Payroll();
        p.setId(15L);
        p.setStatus("APPROVED");
        when(payrollService.approvePayroll(15L)).thenReturn(p);

        mockMvc.perform(post("/api/v1/payroll/15/approve")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("APPROVED"));
    }

    @Test
    public void testGetPayslip() throws Exception {
        when(payrollService.calculatePreview(101L)).thenReturn(Map.of("grossSalary", 80000));

        mockMvc.perform(get("/api/v1/payroll/101/payslip")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/api/v1/payroll/101/payslip")
                        .header("Authorization", AUTH_HEADER)
                        .header("Accept", "application/pdf"))
                .andExpect(status().isOk());
    }

    @Test
    public void testDisburseSalary() throws Exception {
        when(payrollService.disbursePayment(15L)).thenReturn(Map.of("payrollRunId", 15, "status", "PAID"));

        mockMvc.perform(post("/api/v1/payroll/disburse")
                        .header("Authorization", AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("payrollRunId", 15L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testGetHistory() throws Exception {
        when(payrollService.getMonthlyReport()).thenReturn(List.of(Map.of("month", "2026-06")));

        mockMvc.perform(get("/api/v1/payroll/history")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testGetEmployeeHistory() throws Exception {
        Payroll p = new Payroll();
        p.setId(1L);
        when(payrollService.getPayrollByEmployeeId(101L)).thenReturn(List.of(p));

        mockMvc.perform(get("/api/v1/payroll/employees/101/history")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testTaxConfiguration() throws Exception {
        when(payrollService.getTaxSettings()).thenReturn(Map.of("pfRate", 12.0));
        when(payrollService.updateTaxSettings(any(Map.class))).thenReturn(Map.of("pfRate", 15.0));

        mockMvc.perform(get("/api/v1/payroll/taxes")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(put("/api/v1/payroll/taxes")
                        .header("Authorization", AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("pfRate", 15.0))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testAnalytics() throws Exception {
        when(payrollService.getCostTrend()).thenReturn(Map.of("values", List.of(5000000)));
        when(payrollService.getDepartmentCost()).thenReturn(Map.of("values", List.of(2500000)));

        mockMvc.perform(get("/api/v1/payroll/analytics/cost-trend")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/api/v1/payroll/analytics/department-cost")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testReports() throws Exception {
        when(payrollService.getMonthlyReport()).thenReturn(List.of(Map.of("month", "2026-06")));
        when(payrollService.getSalaryRegister()).thenReturn(List.of(Map.of("basic", 50000)));
        when(payrollService.getTaxReport()).thenReturn(List.of(Map.of("pf", 1800)));
        when(payrollService.getDisbursementReport()).thenReturn(List.of(Map.of("amount", 70200)));

        mockMvc.perform(get("/api/v1/payroll/reports/monthly")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/api/v1/payroll/reports/salary-register")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/api/v1/payroll/reports/tax")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/api/v1/payroll/reports/disbursement")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
