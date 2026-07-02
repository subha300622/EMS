package com.example.ems.auth.controller;

import com.example.ems.auth.dto.RoleRequest;
import com.example.ems.auth.entity.Permission;
import com.example.ems.auth.entity.Role;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.PermissionRepository;
import com.example.ems.auth.repository.RoleRepository;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.event.OrganizationEvents.OrganizationCreatedEvent;
import com.example.ems.organization.repository.OrganizationRepository;
import com.example.ems.security.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
public class PlatformRoleAndPermissionIntegrationTest {

    private MockMvc platformRoleMvc;
    private MockMvc permissionMvc;
    private MockMvc orgRoleMvc;
    private MockMvc userMvc;

    @Autowired
    private PlatformRoleController platformRoleController;

    @Autowired
    private PermissionController permissionController;

    @Autowired
    private OrganizationRoleController orgRoleController;

    @Autowired
    private UserController userController;

    @Autowired
    private RoleService roleService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private User platformAdmin;
    private User orgAdmin;
    private User regularUser;
    private Organization testOrg;

    private String platformAdminToken;
    private String orgAdminToken;
    private String regularUserToken;

    @BeforeEach
    public void setUp() {
        platformRoleMvc = MockMvcBuilders.standaloneSetup(platformRoleController).build();
        permissionMvc = MockMvcBuilders.standaloneSetup(permissionController).build();
        orgRoleMvc = MockMvcBuilders.standaloneSetup(orgRoleController).build();
        userMvc = MockMvcBuilders.standaloneSetup(userController).build();

        // Clean up any existing test users to prevent unique constraint failures
        userRepository.findByWorkEmail("plat-admin-tst@company.com").ifPresent(userRepository::delete);
        userRepository.findByWorkEmail("org-admin-tst@company.com").ifPresent(userRepository::delete);
        userRepository.findByWorkEmail("emp-tst@company.com").ifPresent(userRepository::delete);
        userRepository.findByUserId("EMP_PL_TST").ifPresent(userRepository::delete);
        userRepository.findByUserId("EMP_ORG_TST").ifPresent(userRepository::delete);
        userRepository.findByUserId("EMP_REG_TST").ifPresent(userRepository::delete);

        // 1. Create Platform Admin
        Role superAdminRole = roleRepository.findByName("SUPER_ADMIN")
                .map(r -> {
                    r.setPlatformTemplate(true);
                    r.setSystemRole(true);
                    return roleRepository.save(r);
                })
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setName("SUPER_ADMIN");
                    r.setPlatformTemplate(true);
                    r.setSystemRole(true);
                    return roleRepository.save(r);
                });

        // Seed system.manage permission to bypass checks
        Permission systemManage = permissionRepository.findByName("system.manage")
                .orElseGet(() -> {
                    Permission p = new Permission();
                    p.setName("system.manage");
                    p.setDescription("Super Admin System Management");
                    return permissionRepository.save(p);
                });
        superAdminRole.getPermissions().add(systemManage);
        roleRepository.save(superAdminRole);

        platformAdmin = new User();
        platformAdmin.setFullName("Platform Admin User");
        platformAdmin.setWorkEmail("plat-admin-tst@company.com");
        platformAdmin.setUserId("EMP_PL_TST");
        platformAdmin.setRole(superAdminRole);
        platformAdmin.setStatus("ACTIVE");
        platformAdmin = userRepository.save(platformAdmin);

        platformAdminToken = jwtService.generateAccessToken(
                platformAdmin.getUserId(),
                platformAdmin.getWorkEmail(),
                "SUPER_ADMIN"
        );

        // 2. Create Test Organization
        testOrg = new Organization();
        testOrg.setName("Test Multi-Tenant Org");
        testOrg.setOrganizationCode("TMTORG");
        testOrg = organizationRepository.save(testOrg);

        // Provision organization roles using event publisher (simulates Org register hook)
        eventPublisher.publishEvent(new OrganizationCreatedEvent(testOrg.getId(), testOrg.getOrganizationCode()));

        // 3. Create Org Admin & User
        Role tenantAdminRole = roleRepository.findByOrganizationIdAndName(testOrg.getId(), "ADMIN")
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setName("ADMIN");
                    r.setOrganization(testOrg);
                    r.setPlatformTemplate(false);
                    return roleRepository.save(r);
                });

        Permission roleManage = permissionRepository.findByName("role.manage")
                .orElseGet(() -> {
                    Permission p = new Permission();
                    p.setName("role.manage");
                    p.setDescription("Manage Roles");
                    return permissionRepository.save(p);
                });
        tenantAdminRole.getPermissions().add(roleManage);

        Permission userManage = permissionRepository.findByName("user.manage")
                .orElseGet(() -> {
                    Permission p = new Permission();
                    p.setName("user.manage");
                    p.setDescription("Manage Users");
                    return permissionRepository.save(p);
                });
        tenantAdminRole.getPermissions().add(userManage);
        roleRepository.save(tenantAdminRole);

        orgAdmin = new User();
        orgAdmin.setFullName("Org Admin User");
        orgAdmin.setWorkEmail("org-admin-tst@company.com");
        orgAdmin.setUserId("EMP_ORG_TST");
        orgAdmin.setOrganization(testOrg);
        orgAdmin.setRole(tenantAdminRole);
        orgAdmin.setStatus("ACTIVE");
        orgAdmin = userRepository.save(orgAdmin);

        orgAdminToken = jwtService.generateAccessToken(
                orgAdmin.getUserId(),
                orgAdmin.getWorkEmail(),
                "ADMIN",
                testOrg.getId(),
                null,
                1,
                1L
        );

        Role tenantEmployeeRole = roleRepository.findByOrganizationIdAndName(testOrg.getId(), "EMPLOYEE")
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setName("EMPLOYEE");
                    r.setOrganization(testOrg);
                    r.setPlatformTemplate(false);
                    return roleRepository.save(r);
                });

        regularUser = new User();
        regularUser.setFullName("Regular Employee User");
        regularUser.setWorkEmail("emp-tst@company.com");
        regularUser.setUserId("EMP_REG_TST");
        regularUser.setOrganization(testOrg);
        regularUser.setRole(tenantEmployeeRole);
        regularUser.setStatus("ACTIVE");
        regularUser = userRepository.save(regularUser);

        regularUserToken = jwtService.generateAccessToken(
                regularUser.getUserId(),
                regularUser.getWorkEmail(),
                "EMPLOYEE",
                testOrg.getId(),
                null,
                1,
                1L
        );
    }

    @Test
    public void testOrgCreatedProvisionsTenantRolesAndIsolatedPermissions() {
        // Assert that tenant-scoped roles were provisioned
        List<Role> tenantRoles = roleRepository.findByOrganizationId(testOrg.getId());
        assertFalse(tenantRoles.isEmpty());

        Optional<Role> tenantEmployee = roleRepository.findByOrganizationIdAndName(testOrg.getId(), "EMPLOYEE");
        assertTrue(tenantEmployee.isPresent());
        assertFalse(tenantEmployee.get().isPlatformTemplate());
        assertEquals(testOrg.getId(), tenantEmployee.get().getOrganization().getId());
    }

    @Test
    public void testPlatformRoleTemplatesCRUDAndRoleRestrictions() throws Exception {
        // 1. List platform templates
        platformRoleMvc.perform(get("/api/v1/platform/roles")
                        .header("Authorization", "Bearer " + platformAdminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());

        // 2. Reject non-admin access to platform templates
        platformRoleMvc.perform(get("/api/v1/platform/roles")
                        .header("Authorization", "Bearer " + orgAdminToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));

        // 3. Create platform template
        RoleRequest request = new RoleRequest();
        request.setName("Temp Platform Template");
        request.setDescription("Dynamic template");

        platformRoleMvc.perform(post("/api/v1/platform/roles")
                        .header("Authorization", "Bearer " + platformAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Temp Platform Template"));
    }

    @Test
    public void testPermissionCRUDPlatformAdmin() throws Exception {
        Permission customPerm = new Permission();
        customPerm.setName("custom.test.permission");
        customPerm.setDescription("Test mapping");

        // 1. Create permission
        permissionMvc.perform(post("/api/v1/platform/permissions")
                        .header("Authorization", "Bearer " + platformAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customPerm)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("custom.test.permission"));

        // 2. List system permissions
        permissionMvc.perform(get("/api/v1/platform/permissions")
                        .header("Authorization", "Bearer " + platformAdminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testOrganizationRolesCustomization() throws Exception {
        // 1. List organization roles
        orgRoleMvc.perform(get("/api/v1/organizations/roles")
                        .header("Authorization", "Bearer " + orgAdminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 2. Create custom organization role
        RoleRequest request = new RoleRequest();
        request.setName("Org Specific Role");
        request.setDescription("Tenant custom role definition");

        orgRoleMvc.perform(post("/api/v1/organizations/roles")
                        .header("Authorization", "Bearer " + orgAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Org Specific Role"));
    }

    @Test
    public void testUserRoleAssignmentAndPermissionsQueries() throws Exception {
        RoleRequest roleReq = new RoleRequest();
        roleReq.setName("Contractor");
        roleReq.setDescription("Contractor role");
        Role customOrgRole = roleService.createTenantRole(testOrg.getId(), roleReq);

        // 1. Assign role to regular user
        Map<String, List<Long>> request = new HashMap<>();
        request.put("roleIds", List.of(customOrgRole.getId()));

        userMvc.perform(put("/api/v1/users/" + regularUser.getUserId() + "/roles")
                        .header("Authorization", "Bearer " + orgAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User role assigned successfully"));

        // Verify assignment in DB
        Optional<User> updatedUser = userRepository.findByUserId(regularUser.getUserId());
        assertTrue(updatedUser.isPresent());
        assertEquals("Contractor", updatedUser.get().getRole().getName());

        // 2. Fetch effective user permissions
        userMvc.perform(get("/api/v1/users/" + regularUser.getUserId() + "/effective-permissions")
                        .header("Authorization", "Bearer " + orgAdminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }
}
