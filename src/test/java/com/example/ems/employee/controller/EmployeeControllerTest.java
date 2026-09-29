package com.example.ems.employee.controller;

// Trigger IDE re-parse
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.employee.dto.EmployeeRequest;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
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
import static org.mockito.ArgumentMatchers.anyString;
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
    private EmployeeRepository employeeRepository;

    @Mock
    private RoleService roleService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private EmployeeController employeeController;

    private static final String TOKEN = "mock-token";
    private static final String AUTH_HEADER = "Bearer " + TOKEN;
    private static final String ADMIN_EMAIL = "super_admin@company.com";

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        mockMvc = MockMvcBuilders.standaloneSetup(employeeController).build();
    }

    @Test
    public void testCreateEmployeeSuccess() throws Exception {
        User adminUser = new User();
        adminUser.setWorkEmail(ADMIN_EMAIL);

        EmployeeRequest request = new EmployeeRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("johndoe@example.com");
        request.setDepartment("Engineering");
        request.setDesignation("Software Engineer");
        request.setAnnualSalary(new BigDecimal("1200000.00"));
        request.setDateOfJoining(LocalDate.of(2026, 6, 1));

        Employee created = new Employee();
        created.setId(1L);
        created.setFirstName("John");
        created.setLastName("Doe");
        created.setFullName("John Doe");
        created.setEmail("johndoe@example.com");
        created.setDepartment("Engineering");
        created.setDesignation("Software Engineer");
        created.setAnnualSalary(new BigDecimal("1200000.00"));
        created.setJoiningDate(LocalDate.of(2026, 6, 1));

        when(jwtService.validateAccessToken(TOKEN)).thenReturn(true);
        when(jwtService.getEmailFromToken(TOKEN)).thenReturn(ADMIN_EMAIL);
        when(userRepository.findByWorkEmail(ADMIN_EMAIL)).thenReturn(Optional.of(adminUser));
        when(roleService.hasPermission(ADMIN_EMAIL, "employee.create")).thenReturn(true);
        when(employeeService.createEmployee(any(EmployeeRequest.class), anyString())).thenReturn(created);

        mockMvc.perform(post("/api/v1/employees")
                .header("Authorization", AUTH_HEADER)
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
        User employeeUser = new User();
        employeeUser.setWorkEmail(ADMIN_EMAIL);

        EmployeeRequest request = new EmployeeRequest();
        request.setFirstName("Jane");
        request.setLastName("Doe");
        request.setEmail("janedoe@example.com");
        request.setDepartment("Engineering");
        request.setDesignation("Software Engineer");
        request.setAnnualSalary(new BigDecimal("1200000.00"));
        request.setDateOfJoining(LocalDate.of(2026, 6, 1));

        when(jwtService.validateAccessToken(TOKEN)).thenReturn(true);
        when(jwtService.getEmailFromToken(TOKEN)).thenReturn(ADMIN_EMAIL);
        when(userRepository.findByWorkEmail(ADMIN_EMAIL)).thenReturn(Optional.of(employeeUser));
        when(roleService.hasPermission(ADMIN_EMAIL, "employee.create")).thenReturn(false);

        mockMvc.perform(post("/api/v1/employees")
                .header("Authorization", AUTH_HEADER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testCreateEmployeeWithAllOnboardingFieldsSuccess() throws Exception {
        User adminUser = new User();
        adminUser.setWorkEmail(ADMIN_EMAIL);

        EmployeeRequest request = new EmployeeRequest();
        request.setFirstName("Alice");
        request.setLastName("Smith");
        request.setEmail("alice.smith@company.com");
        request.setPassword("AlicePass123!");
        request.setConfirmPassword("AlicePass123!");
        request.setReportingManager("EMP045");
        request.setDepartment("Human Resources");
        request.setLocation("Mumbai");
        request.setDesignation("HR Manager");
        request.setEmploymentType("Full-time");
        request.setEmployeeStatus("Active");
        request.setPersonalMobile("+91 9876543211");
        request.setAadhaarNumber("987654321098");
        request.setPanNumber("PQRST5678G");
        request.setEmergencyContactName("Bob Smith");
        request.setEmergencyContactNumber("9123456789");
        request.setDateOfJoining(LocalDate.of(2026, 7, 17));
        request.setProbationEndDate(LocalDate.of(2027, 1, 17));
        request.setSendInvite(true);
        request.setNotifyManager(true);
        request.setNotifyHR(true);
        request.setReminderUnopened(false);

        Employee created = new Employee();
        created.setId(2L);
        created.setFirstName("Alice");
        created.setLastName("Smith");
        created.setFullName("Alice Smith");
        created.setEmail("alice.smith@company.com");
        created.setDepartment("Human Resources");
        created.setDesignation("HR Manager");
        created.setAnnualSalary(new BigDecimal("1500000.00"));
        created.setJoiningDate(LocalDate.of(2026, 7, 17));

        when(jwtService.validateAccessToken(TOKEN)).thenReturn(true);
        when(jwtService.getEmailFromToken(TOKEN)).thenReturn(ADMIN_EMAIL);
        when(userRepository.findByWorkEmail(ADMIN_EMAIL)).thenReturn(Optional.of(adminUser));
        when(roleService.hasPermission(ADMIN_EMAIL, "employee.create")).thenReturn(true);
        when(employeeService.createEmployee(any(EmployeeRequest.class), anyString())).thenReturn(created);

        mockMvc.perform(post("/api/v1/employees")
                .header("Authorization", AUTH_HEADER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.fullName").value("Alice Smith"))
                .andExpect(jsonPath("$.data.email").value("alice.smith@company.com"))
                .andExpect(jsonPath("$.data.department").value("Human Resources"))
                .andExpect(jsonPath("$.data.designation").value("HR Manager"));
    }
}
