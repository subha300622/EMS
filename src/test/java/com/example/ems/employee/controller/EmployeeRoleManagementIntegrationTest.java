package com.example.ems.employee.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.employee.dto.*;
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

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class EmployeeRoleManagementIntegrationTest {

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

    private static final String TOKEN = "mock-jwt-token";
    private static final String AUTH_HEADER = "Bearer " + TOKEN;
    private static final String SUPER_ADMIN_EMAIL = "superadmin@acme.com";

    private User superAdminUser;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        mockMvc = MockMvcBuilders.standaloneSetup(employeeController).build();

        superAdminUser = new User();
        superAdminUser.setId(10L);
        superAdminUser.setWorkEmail(SUPER_ADMIN_EMAIL);

        when(jwtService.validateAccessToken(TOKEN)).thenReturn(true);
        when(jwtService.getEmailFromToken(TOKEN)).thenReturn(SUPER_ADMIN_EMAIL);
        when(userRepository.findByWorkEmail(SUPER_ADMIN_EMAIL)).thenReturn(Optional.of(superAdminUser));
    }

    @Test
    public void testGetEmployeeRolesSuccess() throws Exception {
        when(roleService.hasPermission(SUPER_ADMIN_EMAIL, "employee.read")).thenReturn(true);

        EmployeeRolesResponse.EmployeeRoleDto roleDto = new EmployeeRolesResponse.EmployeeRoleDto(5L, "MANAGER", "ACTIVE");
        EmployeeRolesResponse response = new EmployeeRolesResponse(100L, List.of(roleDto));

        when(employeeService.getEmployeeRoles(eq(100L), any())).thenReturn(response);

        mockMvc.perform(get("/api/v1/employees/100/roles")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.employeeId", is(100)))
                .andExpect(jsonPath("$.data.roles[0].roleName", is("MANAGER")));
    }

    @Test
    public void testAssignSingleRoleSuccess() throws Exception {
        when(roleService.hasPermission(SUPER_ADMIN_EMAIL, "role.assign")).thenReturn(true);

        AssignRoleRequest request = new AssignRoleRequest(5L);
        EmployeeRolesResponse.EmployeeRoleDto roleDto = new EmployeeRolesResponse.EmployeeRoleDto(5L, "MANAGER", "ACTIVE");
        EmployeeRolesResponse response = new EmployeeRolesResponse(100L, List.of(roleDto));

        when(employeeService.assignRoleToEmployee(eq(100L), eq(5L), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/employees/100/roles")
                        .header("Authorization", AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.roles[0].roleName", is("MANAGER")));
    }

    @Test
    public void testAssignSingleRolePlatformAdminRejected() throws Exception {
        when(roleService.hasPermission(SUPER_ADMIN_EMAIL, "role.assign")).thenReturn(true);

        AssignRoleRequest request = new AssignRoleRequest(1L);
        when(employeeService.assignRoleToEmployee(eq(100L), eq(1L), any()))
                .thenThrow(new IllegalArgumentException("PLATFORM_ADMIN_ROLE_NOT_ASSIGNABLE: PLATFORM_ADMIN cannot be assigned through employee role management."));

        mockMvc.perform(post("/api/v1/employees/100/roles")
                        .header("Authorization", AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.code", is("PLATFORM_ADMIN_ROLE_NOT_ASSIGNABLE")));
    }

    @Test
    public void testAssignBulkRolesSuccess() throws Exception {
        when(roleService.hasPermission(SUPER_ADMIN_EMAIL, "role.assign")).thenReturn(true);

        AssignBulkRolesRequest request = new AssignBulkRolesRequest(List.of(5L, 6L));
        EmployeeRolesResponse.EmployeeRoleDto r1 = new EmployeeRolesResponse.EmployeeRoleDto(5L, "HR", "ACTIVE");
        EmployeeRolesResponse.EmployeeRoleDto r2 = new EmployeeRolesResponse.EmployeeRoleDto(6L, "MANAGER", "ACTIVE");
        EmployeeRolesResponse response = new EmployeeRolesResponse(100L, List.of(r1, r2));

        when(employeeService.assignBulkRolesToEmployee(eq(100L), eq(List.of(5L, 6L)), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/employees/100/roles/bulk")
                        .header("Authorization", AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.roles.length()", is(2)));
    }

    @Test
    public void testAssignBulkRolesPlatformAdminRejected() throws Exception {
        when(roleService.hasPermission(SUPER_ADMIN_EMAIL, "role.assign")).thenReturn(true);

        AssignBulkRolesRequest request = new AssignBulkRolesRequest(List.of(5L, 1L));
        when(employeeService.assignBulkRolesToEmployee(eq(100L), eq(List.of(5L, 1L)), any()))
                .thenThrow(new IllegalArgumentException("PLATFORM_ADMIN_ROLE_NOT_ASSIGNABLE: PLATFORM_ADMIN cannot be assigned through employee role management."));

        mockMvc.perform(post("/api/v1/employees/100/roles/bulk")
                        .header("Authorization", AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.code", is("PLATFORM_ADMIN_ROLE_NOT_ASSIGNABLE")));
    }

    @Test
    public void testChangeEmployeeRolesSuccess() throws Exception {
        when(roleService.hasPermission(SUPER_ADMIN_EMAIL, "role.assign")).thenReturn(true);

        ChangeEmployeeRoleRequest request = new ChangeEmployeeRoleRequest(List.of(6L), java.time.LocalDate.parse("2026-09-01"), "Promoted to manager");
        EmployeeRolesResponse.EmployeeRoleDto r = new EmployeeRolesResponse.EmployeeRoleDto(6L, "MANAGER", "ACTIVE");
        EmployeeRolesResponse response = new EmployeeRolesResponse(100L, List.of(r));

        when(employeeService.changeEmployeeRoles(eq(100L), any(), any())).thenReturn(response);

        mockMvc.perform(put("/api/v1/employees/100/roles")
                        .header("Authorization", AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.roles[0].roleName", is("MANAGER")));
    }

    @Test
    public void testChangeLastSuperAdminRejected() throws Exception {
        when(roleService.hasPermission(SUPER_ADMIN_EMAIL, "role.assign")).thenReturn(true);

        ChangeEmployeeRoleRequest request = new ChangeEmployeeRoleRequest(List.of(6L), java.time.LocalDate.parse("2026-09-01"), "Demotion");
        when(employeeService.changeEmployeeRoles(eq(100L), any(), any()))
                .thenThrow(new IllegalArgumentException("LAST_SUPER_ADMIN_CANNOT_BE_REMOVED: The last Super Admin of an organization cannot be removed."));

        mockMvc.perform(put("/api/v1/employees/100/roles")
                        .header("Authorization", AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.code", is("LAST_SUPER_ADMIN_CANNOT_BE_REMOVED")));
    }

    @Test
    public void testRemoveEmployeeRoleSuccess() throws Exception {
        when(roleService.hasPermission(SUPER_ADMIN_EMAIL, "role.assign")).thenReturn(true);

        EmployeeRolesResponse response = new EmployeeRolesResponse(100L, List.of());
        when(employeeService.removeEmployeeRole(eq(100L), eq(5L), any())).thenReturn(response);

        mockMvc.perform(delete("/api/v1/employees/100/roles/5")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));
    }

    @Test
    public void testRemoveLastSuperAdminRejected() throws Exception {
        when(roleService.hasPermission(SUPER_ADMIN_EMAIL, "role.assign")).thenReturn(true);

        when(employeeService.removeEmployeeRole(eq(100L), eq(2L), any()))
                .thenThrow(new IllegalArgumentException("LAST_SUPER_ADMIN_CANNOT_BE_REMOVED: The last Super Admin of an organization cannot be removed."));

        mockMvc.perform(delete("/api/v1/employees/100/roles/2")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.code", is("LAST_SUPER_ADMIN_CANNOT_BE_REMOVED")));
    }

    @Test
    public void testGetAssignableRolesExcludesPlatformAdmin() throws Exception {
        AssignableRoleDto r1 = new AssignableRoleDto(2L, "SUPER_ADMIN", "Super Admin");
        AssignableRoleDto r2 = new AssignableRoleDto(5L, "HR", "HR Manager");

        when(employeeService.getAssignableRoles(any())).thenReturn(List.of(r1, r2));

        mockMvc.perform(get("/api/v1/roles/assignable")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.length()", is(2)))
                .andExpect(jsonPath("$.data[?(@.roleName == 'PLATFORM_ADMIN')]").doesNotExist());
    }
}
