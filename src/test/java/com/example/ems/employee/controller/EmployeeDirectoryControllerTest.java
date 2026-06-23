package com.example.ems.employee.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.employee.dto.MyTeamResponse;
import com.example.ems.employee.dto.EmployeeSearchResponse;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.employee.service.MyEmployeeDirectoryService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class EmployeeDirectoryControllerTest {

    private MockMvc mockMvc;

    @Mock
    private MyEmployeeDirectoryService directoryService;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private RoleService roleService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private EmployeeDirectoryController directoryController;

    private User empUser;
    private final String empEmail = "employee@company.com";
    private static final String token = "mock-token";
    private static final String AUTH_HEADER = "Bearer " + token;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(directoryController).build();

        empUser = new User();
        empUser.setWorkEmail(empEmail);
        empUser.setFullName("Arjun Mehta");

        when(jwtService.validateAccessToken(token)).thenReturn(true);
        when(jwtService.getEmailFromToken(token)).thenReturn(empEmail);
        when(userRepository.findByWorkEmail(empEmail)).thenReturn(Optional.of(empUser));
    }

    private void setupMockPermissions(boolean allowed) {
        when(roleService.isSuperAdmin(empEmail)).thenReturn(false);
        when(roleService.hasPermission(eq(empEmail), any(String.class))).thenReturn(allowed);
    }

    @Test
    public void testGetMyTeam() throws Exception {
        setupMockPermissions(true);
        MyTeamResponse resp = new MyTeamResponse();
        when(directoryService.getMyTeam(empEmail)).thenReturn(resp);

        mockMvc.perform(get("/api/v1/directory/my-team")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testSearchEmployees() throws Exception {
        setupMockPermissions(true);
        EmployeeSearchResponse resp = new EmployeeSearchResponse(List.of(), 0);
        when(directoryService.searchEmployees(eq("raj"), any())).thenReturn(resp);

        mockMvc.perform(get("/api/v1/directory/search")
                .param("keyword", "raj")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testGetOrganizationChartSuccess() throws Exception {
        setupMockPermissions(true);

        Employee ceo = new Employee();
        ceo.setId(1L);
        ceo.setFullName("CEO Name");
        ceo.setManager(null);

        Employee manager = new Employee();
        manager.setId(2L);
        manager.setFullName("Manager Name");
        manager.setManager(ceo);

        when(employeeRepository.findAll()).thenReturn(List.of(ceo, manager));

        mockMvc.perform(get("/api/v1/directory/organization-chart")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].fullName").value("CEO Name"))
                .andExpect(jsonPath("$.data[0].children[0].fullName").value("Manager Name"));
    }

    @Test
    public void testGetOrganizationChartForEmployeeSuccess() throws Exception {
        setupMockPermissions(true);

        Employee manager = new Employee();
        manager.setId(2L);
        manager.setFullName("Manager Name");
        manager.setManager(null);

        when(employeeRepository.findById(2L)).thenReturn(Optional.of(manager));
        when(employeeRepository.findAll()).thenReturn(List.of(manager));

        mockMvc.perform(get("/api/v1/directory/organization-chart/2")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fullName").value("Manager Name"));
    }
}
