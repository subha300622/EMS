package com.example.ems.employee.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.security.service.JwtService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class OrgChartControllerTest {

    private MockMvc mockMvc;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleService roleService;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private OrgChartController orgChartController;

    private static final String TOKEN = "mock-token";
    private static final String AUTH_HEADER = "Bearer " + TOKEN;
    private static final String EMAIL = "user@example.com";

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(orgChartController).build();

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
    public void testGetOrganizationChartSuccess() throws Exception {
        mockPermission("employee.directory.read", true);

        Employee ceo = new Employee();
        ceo.setId(1L);
        ceo.setFullName("CEO Name");
        ceo.setManager(null);

        Employee manager = new Employee();
        manager.setId(2L);
        manager.setFullName("Manager Name");
        manager.setManager(ceo);

        when(employeeRepository.findAll()).thenReturn(List.of(ceo, manager));

        mockMvc.perform(get("/api/v1/organization-chart")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].fullName").value("CEO Name"))
                .andExpect(jsonPath("$.data[0].children[0].fullName").value("Manager Name"));
    }

    @Test
    public void testGetOrganizationChartForEmployeeSuccess() throws Exception {
        mockPermission("employee.directory.read", true);

        Employee manager = new Employee();
        manager.setId(2L);
        manager.setFullName("Manager Name");
        manager.setManager(null);
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(manager));
        when(employeeRepository.findAll()).thenReturn(List.of(manager));

        mockMvc.perform(get("/api/v1/organization-chart/2")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fullName").value("Manager Name"));
    }

    @Test
    public void testGetReportingChainSuccess() throws Exception {
        mockPermission("employee.directory.read", true);

        Employee ceo = new Employee();
        ceo.setId(1L);
        ceo.setFullName("CEO Name");
        ceo.setManager(null);

        Employee employee = new Employee();
        employee.setId(2L);
        employee.setFullName("Employee Name");
        employee.setManager(ceo);

        when(employeeRepository.findById(2L)).thenReturn(Optional.of(employee));

        mockMvc.perform(get("/api/v1/employees/2/reporting-chain")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].fullName").value("Employee Name"))
                .andExpect(jsonPath("$.data[1].fullName").value("CEO Name"));
    }
}
