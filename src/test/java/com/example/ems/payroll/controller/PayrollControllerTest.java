package com.example.ems.payroll.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.payroll.dto.PayrollGenerateRequest;
import com.example.ems.payroll.entity.Payroll;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

public class PayrollControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private PayrollService payrollService;

    @Mock
    private RoleService roleService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private PayrollController payrollController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        mockMvc = MockMvcBuilders.standaloneSetup(payrollController).build();
    }

    @Test
    public void testGeneratePayrollSuccess() throws Exception {
        String token = "Bearer mock-token";
        String email = "admin@example.com";

        User user = new User();
        user.setWorkEmail(email);

        PayrollGenerateRequest request = new PayrollGenerateRequest(6, 2026);
        Employee emp = new Employee();
        emp.setId(1L);
        emp.setFullName("John Doe");

        Payroll p = new Payroll(1L, emp, 6, 2026, BigDecimal.valueOf(5000), BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.valueOf(5000), "GENERATED", null, null);

        when(jwtService.validateAccessToken("mock-token")).thenReturn(true);
        when(jwtService.getEmailFromToken("mock-token")).thenReturn(email);
        when(userRepository.findByWorkEmail(email)).thenReturn(Optional.of(user));
        when(roleService.hasPermission(email, "payroll.manage")).thenReturn(true);
        when(payrollService.generatePayroll(6, 2026)).thenReturn(List.of(p));

        mockMvc.perform(post("/api/v1/payroll-runs")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].month").value(6))
                .andExpect(jsonPath("$.data[0].year").value(2026))
                .andExpect(jsonPath("$.data[0].basicSalary").value(5000));
    }

    @Test
    public void testDeletePayrollSuccess() throws Exception {
        String token = "Bearer mock-token";
        String email = "admin@example.com";

        User user = new User();
        user.setWorkEmail(email);

        when(jwtService.validateAccessToken("mock-token")).thenReturn(true);
        when(jwtService.getEmailFromToken("mock-token")).thenReturn(email);
        when(userRepository.findByWorkEmail(email)).thenReturn(Optional.of(user));
        when(roleService.hasPermission(email, "payroll.manage")).thenReturn(true);
        when(payrollService.deletePayroll(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/v1/payroll-runs/1")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testDeletePayrollNotFound() throws Exception {
        String token = "Bearer mock-token";
        String email = "admin@example.com";

        User user = new User();
        user.setWorkEmail(email);

        when(jwtService.validateAccessToken("mock-token")).thenReturn(true);
        when(jwtService.getEmailFromToken("mock-token")).thenReturn(email);
        when(userRepository.findByWorkEmail(email)).thenReturn(Optional.of(user));
        when(roleService.hasPermission(email, "payroll.manage")).thenReturn(true);
        when(payrollService.deletePayroll(99L)).thenReturn(false);

        mockMvc.perform(delete("/api/v1/payroll-runs/99")
                .header("Authorization", token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    public void testGetPayrollReportsSuccess() throws Exception {
        String token = "Bearer mock-token";
        String email = "admin@example.com";

        User user = new User();
        user.setWorkEmail(email);

        when(jwtService.validateAccessToken("mock-token")).thenReturn(true);
        when(jwtService.getEmailFromToken("mock-token")).thenReturn(email);
        when(userRepository.findByWorkEmail(email)).thenReturn(Optional.of(user));
        when(roleService.hasPermission(email, "payroll.read")).thenReturn(true);
        when(payrollService.getPayrollStats()).thenReturn(Map.of("totalNetPay", BigDecimal.valueOf(5000)));

        mockMvc.perform(get("/api/v1/payroll-runs/reports")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testExportPayrollSuccess() throws Exception {
        String token = "Bearer mock-token";
        String email = "admin@example.com";

        User user = new User();
        user.setWorkEmail(email);

        Employee emp = new Employee();
        emp.setId(1L);
        emp.setFullName("John Doe");

        Payroll p = new Payroll(1L, emp, 6, 2026, BigDecimal.valueOf(5000), BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.valueOf(5000), "GENERATED", null, null);

        when(jwtService.validateAccessToken("mock-token")).thenReturn(true);
        when(jwtService.getEmailFromToken("mock-token")).thenReturn(email);
        when(userRepository.findByWorkEmail(email)).thenReturn(Optional.of(user));
        when(roleService.hasPermission(email, "payroll.read")).thenReturn(true);
        when(payrollService.getAllPayroll()).thenReturn(List.of(p));

        mockMvc.perform(get("/api/v1/payroll-runs/export")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM));
    }

    @Test
    public void testProcessPayrollSuccess() throws Exception {
        String token = "Bearer mock-token";
        String email = "admin@example.com";

        User user = new User();
        user.setWorkEmail(email);

        Employee emp = new Employee();
        emp.setId(1L);

        Payroll p = new Payroll(1L, emp, 6, 2026, BigDecimal.valueOf(5000), BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.valueOf(5000), "PROCESSED", null, null);

        when(jwtService.validateAccessToken("mock-token")).thenReturn(true);
        when(jwtService.getEmailFromToken("mock-token")).thenReturn(email);
        when(userRepository.findByWorkEmail(email)).thenReturn(Optional.of(user));
        when(roleService.hasPermission(email, "payroll.manage")).thenReturn(true);
        when(payrollService.processPayroll(1L)).thenReturn(p);

        mockMvc.perform(post("/api/v1/payroll-runs/1/process")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("PROCESSED"));
    }
}
