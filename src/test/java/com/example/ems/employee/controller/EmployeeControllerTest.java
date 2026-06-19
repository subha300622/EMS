package com.example.ems.employee.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.employee.dto.EmployeeRequest;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.service.EmployeeService;
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
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class EmployeeControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private EmployeeService employeeService;

    @Mock
    private RoleService roleService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private EmployeeController employeeController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        mockMvc = MockMvcBuilders.standaloneSetup(employeeController).build();
    }

    @Test
    public void testCreateEmployeeSuccess() throws Exception {
        String adminEmail = "super_admin@company.com";
        User adminUser = new User();
        adminUser.setWorkEmail(adminEmail);

        EmployeeRequest request = new EmployeeRequest();
        request.setFullName("John Doe");
        request.setEmail("johndoe@example.com");
        request.setDepartment("Engineering");
        request.setDesignation("Software Engineer");
        request.setAnnualSalary(new BigDecimal("1200000.00"));
        request.setJoiningDate(LocalDate.of(2026, 6, 1));

        Employee created = new Employee();
        created.setId(1L);
        created.setFullName("John Doe");
        created.setEmail("johndoe@example.com");
        created.setDepartment("Engineering");
        created.setDesignation("Software Engineer");
        created.setAnnualSalary(new BigDecimal("1200000.00"));
        created.setJoiningDate(LocalDate.of(2026, 6, 1));

        when(jwtService.validateAccessToken("mock-token")).thenReturn(true);
        when(jwtService.getEmailFromToken("mock-token")).thenReturn(adminEmail);
        when(userRepository.findByWorkEmail(adminEmail)).thenReturn(Optional.of(adminUser));
        when(roleService.hasPermission(adminEmail, "employee.create")).thenReturn(true);
        when(employeeService.createEmployee(any(EmployeeRequest.class))).thenReturn(created);

        mockMvc.perform(post("/api/v1/employees")
                .header("Authorization", "Bearer mock-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.fullName").value("John Doe"))
                .andExpect(jsonPath("$.data.email").value("johndoe@example.com"))
                .andExpect(jsonPath("$.data.department").value("Engineering"))
                .andExpect(jsonPath("$.data.designation").value("Software Engineer"));
    }

    @Test
    public void testCreateEmployeeAccessDenied() throws Exception {
        String testEmail = "employee@example.com";
        User employeeUser = new User();
        employeeUser.setWorkEmail(testEmail);

        EmployeeRequest request = new EmployeeRequest();
        request.setFullName("Jane Doe");
        request.setEmail("janedoe@example.com");
        request.setDepartment("Marketing");
        request.setDesignation("Marketing Specialist");
        request.setAnnualSalary(new BigDecimal("800000.00"));
        request.setJoiningDate(LocalDate.of(2026, 6, 1));

        when(jwtService.validateAccessToken("mock-token")).thenReturn(true);
        when(jwtService.getEmailFromToken("mock-token")).thenReturn(testEmail);
        when(userRepository.findByWorkEmail(testEmail)).thenReturn(Optional.of(employeeUser));
        when(roleService.hasPermission(testEmail, "employee.create")).thenReturn(false);

        mockMvc.perform(post("/api/v1/employees")
                .header("Authorization", "Bearer mock-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testCreateEmployeeUnauthorized() throws Exception {
        EmployeeRequest request = new EmployeeRequest();
        request.setFullName("Jane Doe");
        request.setEmail("janedoe@example.com");
        request.setDepartment("Marketing");
        request.setDesignation("Marketing Specialist");
        request.setAnnualSalary(new BigDecimal("800000.00"));
        request.setJoiningDate(LocalDate.of(2026, 6, 1));

        mockMvc.perform(post("/api/v1/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
