package com.example.ems.payroll.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.payroll.entity.Payroll;
import com.example.ems.payroll.entity.Payslip;
import com.example.ems.payroll.service.PayslipService;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PayslipControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private PayslipService payslipService;

    @Mock
    private RoleService roleService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private PayslipController payslipController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        mockMvc = MockMvcBuilders.standaloneSetup(payslipController).build();
    }

    @Test
    public void testDeletePayslipSuccess() throws Exception {
        String token = "Bearer mock-token";
        String email = "admin@example.com";

        User user = new User();
        user.setWorkEmail(email);

        when(jwtService.validateAccessToken("mock-token")).thenReturn(true);
        when(jwtService.getEmailFromToken("mock-token")).thenReturn(email);
        when(userRepository.findByWorkEmail(email)).thenReturn(Optional.of(user));
        when(roleService.hasPermission(email, "payroll.manage")).thenReturn(true);
        when(payslipService.deletePayslip(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/v1/payslips/1")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testDeletePayslipNotFound() throws Exception {
        String token = "Bearer mock-token";
        String email = "admin@example.com";

        User user = new User();
        user.setWorkEmail(email);

        when(jwtService.validateAccessToken("mock-token")).thenReturn(true);
        when(jwtService.getEmailFromToken("mock-token")).thenReturn(email);
        when(userRepository.findByWorkEmail(email)).thenReturn(Optional.of(user));
        when(roleService.hasPermission(email, "payroll.manage")).thenReturn(true);
        when(payslipService.deletePayslip(99L)).thenReturn(false);

        mockMvc.perform(delete("/api/v1/payslips/99")
                .header("Authorization", token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    public void testExportPayslipsSuccess() throws Exception {
        String token = "Bearer mock-token";
        String email = "admin@example.com";

        User user = new User();
        user.setWorkEmail(email);

        Employee emp = new Employee();
        emp.setId(1L);
        emp.setFullName("John Doe");

        Payroll payroll = new Payroll(1L, emp, 6, 2026, BigDecimal.valueOf(5000), BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.valueOf(5000), "PAID", null, null);

        Payslip payslip = new Payslip(1L, payroll, "PS-123", LocalDateTime.now());

        when(jwtService.validateAccessToken("mock-token")).thenReturn(true);
        when(jwtService.getEmailFromToken("mock-token")).thenReturn(email);
        when(userRepository.findByWorkEmail(email)).thenReturn(Optional.of(user));
        when(roleService.hasPermission(email, "payroll.read")).thenReturn(true);
        when(payslipService.getAllPayslips()).thenReturn(List.of(payslip));

        mockMvc.perform(get("/api/v1/payslips/export")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM));
    }
}
