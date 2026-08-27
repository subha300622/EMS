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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
public class PlatformRoleAndPermissionIntegrationTest {

        private MockMvc catalogMvc;
        private MockMvc userMvc;

        @Autowired
        private PermissionCatalogController permissionCatalogController;

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

        private String orgAdminToken;

        @BeforeEach
        public void setUp() {
                catalogMvc = MockMvcBuilders.standaloneSetup(permissionCatalogController).build();
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

                // 2. Create Test Organization
                testOrg = new Organization();
                testOrg.setName("Test Multi-Tenant Org");
                testOrg.setOrganizationCode("TMTORG");
                testOrg = organizationRepository.save(testOrg);

                // Provision organization roles using event publisher (simulates Org register
                // hook)
                eventPublisher.publishEvent(
                                new OrganizationCreatedEvent(testOrg.getId(), testOrg.getOrganizationCode()));

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
                                1L);

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
        public void testPermissionCatalogFetch() throws Exception {
                catalogMvc.perform(get("/api/v1/permissions/catalog"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.groups").isArray());
        }

        @Test
        public void testUserRoleAssignmentAndPermissionsQueries() throws Exception {
                RoleRequest roleReq = new RoleRequest();
                roleReq.setName("Contractor");
                roleReq.setDescription("Contractor role");
                Role customOrgRole = roleService.createTenantRole(testOrg.getId(), roleReq);

                // 1. Assign role to regular user
                com.example.ems.auth.dto.UserManagementDtos.UpdateRoleRequest updateRoleReq = 
                        new com.example.ems.auth.dto.UserManagementDtos.UpdateRoleRequest(String.valueOf(customOrgRole.getId()));

                userMvc.perform(put("/api/v1/users/" + regularUser.getUserId() + "/role")
                                .header("Authorization", "Bearer " + orgAdminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateRoleReq)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true));

                // Verify assignment in DB
                Optional<User> updatedUser = userRepository.findById(regularUser.getId());
                assertTrue(updatedUser.isPresent());
                assertEquals("Contractor", updatedUser.get().getRole().getName());

                // 2. Fetch user roles
                userMvc.perform(get("/api/v1/users/" + regularUser.getUserId() + "/roles")
                                .header("Authorization", "Bearer " + orgAdminToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.roles").isArray());
        }
}
