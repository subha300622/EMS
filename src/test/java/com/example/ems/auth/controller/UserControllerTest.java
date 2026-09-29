package com.example.ems.auth.controller;

import com.example.ems.auth.dto.UserManagementDtos.*;
import com.example.ems.auth.entity.Role;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.RoleRepository;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.repository.OrganizationRepository;
import com.example.ems.security.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
public class UserControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private UserController userController;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private JwtService jwtService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private Organization testOrg;
    private User adminUser;
    private User regularUser;
    private Role hrManagerRole;
    private String adminToken;
    private String userToken;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();

        // 1. Create Organization
        testOrg = new Organization();
        testOrg.setName("ABC Hospital");
        testOrg.setOrganizationCode("ORG-001");
        testOrg = organizationRepository.save(testOrg);

        // 2. Create Role
        hrManagerRole = new Role();
        hrManagerRole.setName("HR_MANAGER");
        hrManagerRole.setDescription("HR Manager Role");
        hrManagerRole.setOrganization(testOrg);
        hrManagerRole = roleRepository.save(hrManagerRole);

        // 3. Create Admin User with SUPER_ADMIN role
        Role superAdminRole = roleRepository.findByName("SUPER_ADMIN").orElseGet(() -> {
            Role r = new Role();
            r.setName("SUPER_ADMIN");
            r.setPlatformTemplate(true);
            return roleRepository.save(r);
        });

        adminUser = new User();
        adminUser.setUserId("USR-1000");
        adminUser.setEmployeeId("EMP-1000");
        adminUser.setFullName("Admin User");
        adminUser.setWorkEmail("admin@abc.com");
        adminUser.setMobileNumber("+919876543210");
        adminUser.setOrganization(testOrg);
        adminUser.setRole(superAdminRole);
        adminUser.setStatus("ACTIVE");
        adminUser = userRepository.save(adminUser);

        // 4. Create Regular User
        regularUser = new User();
        regularUser.setUserId("USR-1001");
        regularUser.setEmployeeId("EMP-1001");
        regularUser.setFullName("John Doe");
        regularUser.setWorkEmail("john@abc.com");
        regularUser.setMobileNumber("+919876543210");
        regularUser.setOrganization(testOrg);
        regularUser.setRole(hrManagerRole);
        regularUser.setStatus("ACTIVE");
        regularUser = userRepository.save(regularUser);

        adminToken = jwtService.generateAccessToken(adminUser.getUserId(), adminUser.getWorkEmail(), "SUPER_ADMIN");
        userToken = jwtService.generateAccessToken(regularUser.getUserId(), regularUser.getWorkEmail(), "HR_MANAGER");
    }

    @Test
    public void testGetUserById() throws Exception {
        mockMvc.perform(get("/api/v1/users/" + regularUser.getUserId())
                        .header("Authorization", "Bearer " + adminToken)
                        .header("X-Organization-Id", "ORG-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value("USR-1001"))
                .andExpect(jsonPath("$.data.employeeId").value("EMP-1001"))
                .andExpect(jsonPath("$.data.fullName").value("John Doe"))
                .andExpect(jsonPath("$.data.email").value("john@abc.com"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andExpect(jsonPath("$.data.roles[0].roleName").value("HR_MANAGER"))
                .andExpect(jsonPath("$.data.permissions").doesNotExist());
    }

    @Test
    public void testUpdateUser() throws Exception {
        UpdateUserRequest req = new UpdateUserRequest("John Michael Doe", "john.doe@abc.com", "+919876543210");

        mockMvc.perform(put("/api/v1/users/" + regularUser.getUserId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User updated successfully"))
                .andExpect(jsonPath("$.data.fullName").value("John Michael Doe"))
                .andExpect(jsonPath("$.data.email").value("john.doe@abc.com"));

        User updated = userRepository.findByUserId(regularUser.getUserId()).orElseThrow();
        assertEquals("John Michael Doe", updated.getFullName());
        assertEquals("john.doe@abc.com", updated.getWorkEmail());
    }

    @Test
    public void testDeleteUser() throws Exception {
        mockMvc.perform(delete("/api/v1/users/" + regularUser.getUserId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User deleted successfully"));

        User deleted = userRepository.findByUserId(regularUser.getUserId()).orElseThrow();
        assertEquals("INACTIVE", deleted.getStatus());
    }

    @Test
    public void testResetPassword() throws Exception {
        ResetPasswordAdminRequest req = new ResetPasswordAdminRequest();
        req.setSendTemporaryPassword(true);
        req.setForceChangeOnNextLogin(true);

        mockMvc.perform(put("/api/v1/users/" + regularUser.getUserId() + "/password/reset")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Password reset successfully"));
    }

    @Test
    public void testUpdateUserRole() throws Exception {
        UpdateRoleRequest req = new UpdateRoleRequest("ROLE-HR-MANAGER");

        mockMvc.perform(put("/api/v1/users/" + regularUser.getUserId() + "/role")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User role updated successfully"))
                .andExpect(jsonPath("$.data.role.roleName").value("HR_MANAGER"));
    }

    @Test
    public void testRemoveUserRole() throws Exception {
        mockMvc.perform(delete("/api/v1/users/" + regularUser.getUserId() + "/role")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Role removed successfully"));

        User updated = userRepository.findByUserId(regularUser.getUserId()).orElseThrow();
        assertNull(updated.getRole());
    }

    @Test
    public void testGetUserRoles() throws Exception {
        mockMvc.perform(get("/api/v1/users/" + regularUser.getUserId() + "/roles")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.roles[0].roleName").value("HR_MANAGER"));
    }

    @Test
    public void testAssignMultipleRoles() throws Exception {
        AssignMultipleRolesRequest req = new AssignMultipleRolesRequest(List.of(String.valueOf(hrManagerRole.getId())));

        mockMvc.perform(put("/api/v1/users/" + regularUser.getUserId() + "/roles")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Roles assigned successfully"))
                .andExpect(jsonPath("$.data.roles[0].roleName").value("HR_MANAGER"));
    }

    @Test
    public void testUpdateUserStatus() throws Exception {
        UpdateStatusRequest req = new UpdateStatusRequest("SUSPENDED", "Employee suspended by administrator");

        mockMvc.perform(put("/api/v1/users/" + regularUser.getUserId() + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User status updated successfully"))
                .andExpect(jsonPath("$.data.status").value("SUSPENDED"));

        User updated = userRepository.findByUserId(regularUser.getUserId()).orElseThrow();
        assertEquals("SUSPENDED", updated.getStatus());
    }

    @Test
    public void testExportUsers() throws Exception {
        mockMvc.perform(get("/api/v1/users/export")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/csv"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("User ID,Employee ID,Full Name,Email,Status,Role")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("USR-1001")));
    }

    @Test
    public void testCurrentUserBootstrap() throws Exception {
        mockMvc.perform(get("/api/v1/users/me/bootstrap")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.user.userId").value("USR-1001"))
                .andExpect(jsonPath("$.data.organization.organizationName").value("ABC Hospital"))
                .andExpect(jsonPath("$.data.roles[0].roleName").value("HR_MANAGER"))
                .andExpect(jsonPath("$.data.permissions").doesNotExist())
                .andExpect(jsonPath("$.data.auth").doesNotExist());
    }

    @Test
    public void testCurrentUserContext() throws Exception {
        mockMvc.perform(get("/api/v1/users/me/context")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value("USR-1001"))
                .andExpect(jsonPath("$.data.scope").value("ORGANIZATION"));
    }

    @Test
    public void testCurrentUserProfile() throws Exception {
        mockMvc.perform(get("/api/v1/users/me/profile")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value("USR-1001"))
                .andExpect(jsonPath("$.data.fullName").value("John Doe"))
                .andExpect(jsonPath("$.data.roles[0].roleName").value("HR_MANAGER"))
                .andExpect(jsonPath("$.data.permissions").doesNotExist());
    }

    @Test
    public void testPendingUsers() throws Exception {
        User pending = new User();
        pending.setUserId("USR-1005");
        pending.setFullName("Jane Doe");
        pending.setWorkEmail("jane@abc.com");
        pending.setStatus("PENDING");
        pending.setOrganization(testOrg);
        userRepository.save(pending);

        mockMvc.perform(get("/api/v1/users/pending")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].userId").value("USR-1005"))
                .andExpect(jsonPath("$.data[0].status").value("PENDING"));
    }

    @Test
    public void testSearchUsers() throws Exception {
        mockMvc.perform(get("/api/v1/users/search?query=john@abc.com&status=ACTIVE&page=0&size=20")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].userId").value("USR-1001"))
                .andExpect(jsonPath("$.data.content[0].fullName").value("John Doe"))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }
}
