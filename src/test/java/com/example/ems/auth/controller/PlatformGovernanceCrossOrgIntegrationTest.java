package com.example.ems.auth.controller;

import com.example.ems.audit.entity.AuditLog;
import com.example.ems.audit.repository.AuditLogRepository;
import com.example.ems.auth.dto.*;
import com.example.ems.auth.entity.Permission;
import com.example.ems.auth.entity.Role;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.PermissionRepository;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
public class PlatformGovernanceCrossOrgIntegrationTest {

        private MockMvc dashboardMvc;
        private MockMvc platformOrgMvc;
        private MockMvc platformUserMvc;
        private MockMvc orgDashboardMvc;
        private MockMvc userRoleMvc;

        @Autowired
        private PlatformDashboardController platformDashboardController;

        @Autowired
        private PlatformOrganizationController platformOrganizationController;

        @Autowired
        private PlatformOrganizationUserController platformOrganizationUserController;

        @Autowired
        private OrganizationDashboardController organizationDashboardController;

        @Autowired
        private UserRoleController userRoleController;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private RoleRepository roleRepository;

        @Autowired
        private PermissionRepository permissionRepository;

        @Autowired
        private OrganizationRepository organizationRepository;

        @Autowired
        private AuditLogRepository auditLogRepository;

        @Autowired
        private JwtService jwtService;

        private final ObjectMapper objectMapper = new ObjectMapper()
                        .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule())
                        .configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                                        false);

        private User platformAdmin;
        private User orgAdmin;
        private User regularUser;
        private Organization testOrg;

        private String platformAdminToken;
        private String orgAdminToken;

        @BeforeEach
        public void setUp() {
                dashboardMvc = MockMvcBuilders.standaloneSetup(platformDashboardController).build();
                platformOrgMvc = MockMvcBuilders.standaloneSetup(platformOrganizationController).build();
                platformUserMvc = MockMvcBuilders.standaloneSetup(platformOrganizationUserController).build();
                orgDashboardMvc = MockMvcBuilders.standaloneSetup(organizationDashboardController).build();
                userRoleMvc = MockMvcBuilders.standaloneSetup(userRoleController).build();

                // Clean test users
                userRepository.findByWorkEmail("plat-gov-admin@company.com").ifPresent(userRepository::delete);
                userRepository.findByWorkEmail("org-gov-admin@company.com").ifPresent(userRepository::delete);
                userRepository.findByWorkEmail("emp-gov@company.com").ifPresent(userRepository::delete);

                // Create test Organization
                testOrg = new Organization();
                testOrg.setName("Platform Governance Org");
                testOrg.setOrganizationCode("GOV-ORG-01");
                testOrg.setEmail("gov-org@company.com");
                testOrg = organizationRepository.save(testOrg);

                // Set up template roles
                Role superAdminTemplate = roleRepository.findByName("SUPER_ADMIN")
                                .map(r -> {
                                        r.setPlatformTemplate(true);
                                        r.setSystemRole(true);
                                        return roleRepository.save(r);
                                })
                                .orElseGet(() -> {
                                        Role r = new Role();
                                        r.setName("SUPER_ADMIN");
                                        r.setDescription("Template Super Admin");
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
                superAdminTemplate.getPermissions().add(systemManage);

                Permission pView = permissionRepository.findByName("platform.dashboard.view")
                                .orElseGet(() -> {
                                        Permission p = new Permission();
                                        p.setName("platform.dashboard.view");
                                        p.setDescription("View platform dashboard");
                                        return permissionRepository.save(p);
                                });
                superAdminTemplate.getPermissions().add(pView);
                roleRepository.save(superAdminTemplate);

                // Ensure ADMIN platform template role exists
                roleRepository.findByName("ADMIN")
                                .filter(r -> r.getOrganization() == null)
                                .map(r -> {
                                        r.setPlatformTemplate(true);
                                        r.setSystemRole(true);
                                        return roleRepository.save(r);
                                })
                                .orElseGet(() -> {
                                        Role r = new Role();
                                        r.setName("ADMIN");
                                        r.setDescription("Template Admin");
                                        r.setPlatformTemplate(true);
                                        r.setSystemRole(true);
                                        r.setOrganization(null);
                                        return roleRepository.save(r);
                                });

                // Setup tenant roles
                Role tenantAdminRole = new Role();
                tenantAdminRole.setName("ADMIN");
                tenantAdminRole.setOrganization(testOrg);
                tenantAdminRole.setPlatformTemplate(false);
                tenantAdminRole.setSystemRole(true);

                Permission roleManage = permissionRepository.findByName("role.manage")
                                .orElseGet(() -> {
                                        Permission p = new Permission();
                                        p.setName("role.manage");
                                        p.setDescription("Manage Organization Roles");
                                        return permissionRepository.save(p);
                                });
                tenantAdminRole.getPermissions().add(roleManage);

                Permission userRead = permissionRepository.findByName("user.read")
                                .orElseGet(() -> {
                                        Permission p = new Permission();
                                        p.setName("user.read");
                                        p.setDescription("Read Users");
                                        return permissionRepository.save(p);
                                });
                tenantAdminRole.getPermissions().add(userRead);

                Permission userRoleAssign = permissionRepository.findByName("user.role.assign")
                                .orElseGet(() -> {
                                        Permission p = new Permission();
                                        p.setName("user.role.assign");
                                        p.setDescription("Assign User Roles");
                                        return permissionRepository.save(p);
                                });
                tenantAdminRole.getPermissions().add(userRoleAssign);

                roleRepository.save(tenantAdminRole);

                Role tenantEmpRole = new Role();
                tenantEmpRole.setName("EMPLOYEE");
                tenantEmpRole.setOrganization(testOrg);
                tenantEmpRole.setPlatformTemplate(false);
                tenantEmpRole.setSystemRole(true);
                roleRepository.save(tenantEmpRole);

                // Assign platform permissions to Platform Admin (Super Admin)
                platformAdmin = new User();
                platformAdmin.setUserId("USR_PLAT_GOV");
                platformAdmin.setFullName("Platform Admin Gov");
                platformAdmin.setWorkEmail("plat-gov-admin@company.com");
                platformAdmin.setPassword("password");
                platformAdmin.setRole(superAdminTemplate);
                platformAdmin.setOrganization(null); // Platform level
                platformAdmin.setStatus("ACTIVE");
                platformAdmin = userRepository.save(platformAdmin);

                // Org Admin
                orgAdmin = new User();
                orgAdmin.setUserId("USR_ORG_GOV_ADMIN");
                orgAdmin.setFullName("Org Admin Gov");
                orgAdmin.setWorkEmail("org-gov-admin@company.com");
                orgAdmin.setPassword("password");
                orgAdmin.setRole(tenantAdminRole);
                orgAdmin.setOrganization(testOrg);
                orgAdmin.setStatus("ACTIVE");
                orgAdmin = userRepository.save(orgAdmin);

                // Regular employee
                regularUser = new User();
                regularUser.setUserId("USR_EMP_GOV");
                regularUser.setFullName("Regular User Gov");
                regularUser.setWorkEmail("emp-gov@company.com");
                regularUser.setPassword("password");
                regularUser.setRole(tenantEmpRole);
                regularUser.setOrganization(testOrg);
                regularUser.setStatus("ACTIVE");
                regularUser = userRepository.save(regularUser);

                // Generate tokens
                platformAdminToken = "Bearer " + jwtService.generateAccessToken(
                                platformAdmin.getUserId(), platformAdmin.getWorkEmail(), "SUPER_ADMIN", null, null, 1,
                                0);

                orgAdminToken = "Bearer " + jwtService.generateAccessToken(
                                orgAdmin.getUserId(), orgAdmin.getWorkEmail(), "ADMIN", testOrg.getId(), null, 1, 0);
        }

        @Test
        public void testPlatformDashboardSuccess() throws Exception {
                dashboardMvc.perform(get("/api/v1/platform/dashboard")
                                .header(HttpHeaders.AUTHORIZATION, platformAdminToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.organizations").exists())
                                .andExpect(jsonPath("$.data.activeOrganizations").exists())
                                .andExpect(jsonPath("$.data.newOrganizationsThisMonth").exists());
        }

        @Test
        public void testPlatformOrganizationListAndSummary() throws Exception {
                // List organizations
                platformOrgMvc.perform(get("/api/v1/platform/organizations")
                                .header(HttpHeaders.AUTHORIZATION, platformAdminToken)
                                .param("status", "ACTIVE")
                                .param("search", "Governance"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.content").isArray());

                // Org summary statistics
                platformOrgMvc.perform(get("/api/v1/platform/organizations/" + testOrg.getId() + "/summary")
                                .header(HttpHeaders.AUTHORIZATION, platformAdminToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.name").value("Platform Governance Org"))
                                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
        }

        @Test
        public void testPlatformOrganizationRbacSummary() throws Exception {
                platformOrgMvc.perform(get("/api/v1/platform/organizations/" + testOrg.getId() + "/rbac-summary")
                                .header(HttpHeaders.AUTHORIZATION, platformAdminToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.users").value(2)) // orgAdmin, regularUser
                                .andExpect(jsonPath("$.data.roles").value(2));
        }

        @Test
        public void testPlatformOrganizationAuditLogsAndActivities() throws Exception {
                // Add a mock audit log for regular employee email
                AuditLog testLog = new AuditLog(
                                regularUser.getUserId(),
                                regularUser.getWorkEmail(),
                                "test-action",
                                "User",
                                String.valueOf(regularUser.getId()),
                                "127.0.0.1",
                                "Details description");
                auditLogRepository.save(testLog);

                // Fetch activities
                platformOrgMvc.perform(get("/api/v1/platform/organizations/" + testOrg.getId() + "/activities")
                                .header(HttpHeaders.AUTHORIZATION, platformAdminToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data").isArray())
                                .andExpect(jsonPath("$.data[0].action").value("test-action"));

                // Fetch audit logs
                platformOrgMvc.perform(get("/api/v1/platform/organizations/" + testOrg.getId() + "/audit-logs")
                                .header(HttpHeaders.AUTHORIZATION, platformAdminToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data").isArray());
        }

        @Test
        public void testPlatformOrganizationUserViewAndOverride() throws Exception {
                // List users
                platformUserMvc.perform(get("/api/v1/platform/organizations/" + testOrg.getId() + "/users")
                                .header(HttpHeaders.AUTHORIZATION, platformAdminToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data").isArray());

                // View single user details
                platformUserMvc.perform(get(
                                "/api/v1/platform/organizations/" + testOrg.getId() + "/users/" + regularUser.getId())
                                .header(HttpHeaders.AUTHORIZATION, platformAdminToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.fullName").value("Regular User Gov"));

                // View resolved effective permissions
                platformUserMvc.perform(get("/api/v1/platform/organizations/" + testOrg.getId() + "/users/"
                                + regularUser.getId() + "/permissions")
                                .header(HttpHeaders.AUTHORIZATION, platformAdminToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data").isArray());

                // Override user's role: verify that omitting reason throws bad request
                OverrideUserRoleRequest reqNoReason = new OverrideUserRoleRequest("ADMIN", null, "");
                platformUserMvc.perform(put("/api/v1/platform/organizations/" + testOrg.getId() + "/users/"
                                + regularUser.getId() + "/role")
                                .header(HttpHeaders.AUTHORIZATION, platformAdminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(reqNoReason)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.message")
                                                .value("Reason is required for auditing platform admin overrides."));

                // Successful override with reason
                OverrideUserRoleRequest req = new OverrideUserRoleRequest("ADMIN", null, "Security governance update");
                platformUserMvc.perform(put("/api/v1/platform/organizations/" + testOrg.getId() + "/users/"
                                + regularUser.getId() + "/role")
                                .header(HttpHeaders.AUTHORIZATION, platformAdminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true));

                // Confirm new role is set
                User reloadedUser = userRepository.findById(regularUser.getId()).orElseThrow();
                assertEquals("ADMIN", reloadedUser.getRole().getName());
        }

        @Test
        public void testSuspendedOrganizationRejection() throws Exception {
                // Suspend the organization
                testOrg.setDeleted(true);
                organizationRepository.save(testOrg);

                // Verify that viewing org details fails with 403 Organization is suspended
                platformOrgMvc.perform(get("/api/v1/platform/organizations/" + testOrg.getId())
                                .header(HttpHeaders.AUTHORIZATION, platformAdminToken))
                                .andExpect(status().isForbidden())
                                .andExpect(jsonPath("$.message").value("Organization is suspended."))
                                .andExpect(jsonPath("$.errorCode").value("ORG_001"));
        }

        @Test
        public void testOrganizationDashboardAndStats() throws Exception {
                // Organization Dashboard Summary
                orgDashboardMvc.perform(get("/api/v1/organizations/dashboard")
                                .header(HttpHeaders.AUTHORIZATION, orgAdminToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.users").value(2))
                                .andExpect(jsonPath("$.data.roles").value(2));

                // Role Stats (Org Admin self-service)
                orgDashboardMvc.perform(get("/api/v1/organizations/role-stats")
                                .header(HttpHeaders.AUTHORIZATION, orgAdminToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        public void testUserRoleControllerEndpoints() throws Exception {
                // Get user role details
                userRoleMvc.perform(get("/api/v1/users/" + regularUser.getId() + "/role")
                                .header(HttpHeaders.AUTHORIZATION, orgAdminToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.name").value("EMPLOYEE"));

                // Assign role to user
                Role tenantAdminRole = roleRepository.findByOrganizationIdAndName(testOrg.getId(), "ADMIN")
                                .orElseThrow();
                AssignRoleRequest assignReq = new AssignRoleRequest();
                assignReq.setRoleId(tenantAdminRole.getId());

                userRoleMvc.perform(put("/api/v1/users/" + regularUser.getId() + "/role")
                                .header(HttpHeaders.AUTHORIZATION, orgAdminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(assignReq)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        public void testPermissionsLeastPrivilegeAccessDenied() throws Exception {
                // 1. Dashboard: non-admin gets 403 Forbidden
                dashboardMvc.perform(get("/api/v1/platform/dashboard")
                                .header(HttpHeaders.AUTHORIZATION, orgAdminToken))
                                .andExpect(status().isForbidden())
                                .andExpect(jsonPath("$.message").value(
                                                "Access Denied: Requires 'platform.dashboard.view' permission."));

                // 2. Organization List: non-admin gets 403 Forbidden
                platformOrgMvc.perform(get("/api/v1/platform/organizations")
                                .header(HttpHeaders.AUTHORIZATION, orgAdminToken))
                                .andExpect(status().isForbidden())
                                .andExpect(jsonPath("$.message").value(
                                                "Access Denied: Requires 'platform.organization.view' permission."));

                // 3. Audit Logs: lack of 'platform.audit.view' gets 403 Forbidden
                platformOrgMvc.perform(get("/api/v1/platform/organizations/" + testOrg.getId() + "/audit-logs")
                                .header(HttpHeaders.AUTHORIZATION, orgAdminToken))
                                .andExpect(status().isForbidden())
                                .andExpect(jsonPath("$.message")
                                                .value("Access Denied: Requires 'platform.audit.view' permission."));

                // 4. Override User Role: lack of 'platform.role.override' gets 403 Forbidden
                OverrideUserRoleRequest req = new OverrideUserRoleRequest("ADMIN", null, "Security update");
                platformUserMvc.perform(put("/api/v1/platform/organizations/" + testOrg.getId() + "/users/"
                                + regularUser.getId() + "/role")
                                .header(HttpHeaders.AUTHORIZATION, orgAdminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                                .andExpect(status().isForbidden())
                                .andExpect(jsonPath("$.message")
                                                .value("Access Denied: Requires 'platform.role.override' permission."));
        }
}
