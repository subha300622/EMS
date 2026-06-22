package com.example.ems.employee.controller;

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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

        when(jwtService.validateAccessToken(TOKEN)).thenReturn(true);
        when(jwtService.getEmailFromToken(TOKEN)).thenReturn(ADMIN_EMAIL);
        when(userRepository.findByWorkEmail(ADMIN_EMAIL)).thenReturn(Optional.of(adminUser));
        when(roleService.hasPermission(ADMIN_EMAIL, "employee.create")).thenReturn(true);
        when(employeeService.createEmployee(any(EmployeeRequest.class))).thenReturn(created);

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
        request.setFullName("Jane Doe");
        request.setEmail("janedoe@example.com");
        request.setDepartment("Engineering");
        request.setDesignation("Software Engineer");
        request.setAnnualSalary(new BigDecimal("1200000.00"));
        request.setJoiningDate(LocalDate.of(2026, 6, 1));

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
    public void testGetReportingChainSuccess() throws Exception {
        User adminUser = new User();
        adminUser.setWorkEmail(ADMIN_EMAIL);

        Employee ceo = new Employee();
        ceo.setId(1L);
        ceo.setFullName("CEO Name");
        ceo.setManager(null);

        Employee employee = new Employee();
        employee.setId(2L);
        employee.setFullName("Employee Name");
        employee.setManager(ceo);

        when(jwtService.validateAccessToken(TOKEN)).thenReturn(true);
        when(jwtService.getEmailFromToken(TOKEN)).thenReturn(ADMIN_EMAIL);
        when(userRepository.findByWorkEmail(ADMIN_EMAIL)).thenReturn(Optional.of(adminUser));
        when(roleService.hasPermission(ADMIN_EMAIL, "employee.directory.read")).thenReturn(true);
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(employee));

        mockMvc.perform(get("/api/v1/employees/2/reporting-chain")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].fullName").value("Employee Name"))
                .andExpect(jsonPath("$.data[1].fullName").value("CEO Name"));
    }
}
