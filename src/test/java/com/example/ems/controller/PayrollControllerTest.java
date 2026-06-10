package com.example.ems.controller;

import com.example.ems.dto.PayrollGenerateRequest;
import com.example.ems.entity.Employee;
import com.example.ems.entity.Payroll;
import com.example.ems.entity.User;
import com.example.ems.repository.EmployeeRepository;
import com.example.ems.repository.UserRepository;
import com.example.ems.service.JwtService;
import com.example.ems.service.PayrollService;
import com.example.ems.service.RoleService;
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
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

        mockMvc.perform(post("/api/payroll/generate")
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
    public void testGetMyPayrollSuccess() throws Exception {
        String token = "Bearer mock-token";
        String email = "john.doe@example.com";

        User user = new User();
        user.setWorkEmail(email);

        Employee employee = new Employee();
        employee.setId(1L);
        employee.setEmail(email);

        Payroll p = new Payroll(1L, employee, 6, 2026, BigDecimal.valueOf(5000), BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.valueOf(5000), "PAID", null, null);

        when(jwtService.validateAccessToken("mock-token")).thenReturn(true);
        when(jwtService.getEmailFromToken("mock-token")).thenReturn(email);
        when(userRepository.findByWorkEmail(email)).thenReturn(Optional.of(user));
        when(employeeRepository.findByEmail(email)).thenReturn(Optional.of(employee));
        when(payrollService.getPayrollByEmployeeId(1L)).thenReturn(List.of(p));

        mockMvc.perform(get("/api/payroll/my")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].status").value("PAID"));
    }
}