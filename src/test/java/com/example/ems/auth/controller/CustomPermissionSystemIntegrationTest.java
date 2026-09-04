package com.example.ems.auth.controller;

import com.example.ems.auth.entity.*;
import com.example.ems.auth.repository.*;
import com.example.ems.auth.service.RoleService;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.repository.OrganizationRepository;
import com.example.ems.security.context.TenantContext;
import com.example.ems.security.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
public class CustomPermissionSystemIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private PermissionMasterController permissionMasterController;

    @Autowired
    private PermissionGroupController permissionGroupController;

    @Autowired
    private CustomRoleController customRoleController;

    @Autowired
    private MeController meController;

    @Autowired
    private UserRoleController userRoleController;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private PermissionGroupRepository permissionGroupRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private RoleService roleService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    private Organization organization;
    private User platformAdminUser;
    private User orgAdminUser;
    private User regularUser;

    private String platformAdminToken;
    private String orgAdminToken;
    private String regularUserToken;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
                permissionMasterController,
                permissionGroupController,
                customRoleController,
                meController,
                userRoleController
        ).setControllerAdvice(new com.example.ems.config.GlobalExceptionHandler()).build();

        organization = new Organization();
        organization.setName("Permission Test Corp " + System.currentTimeMillis());
        organization.setOrganizationCode("PERM_CORP_" + System.currentTimeMillis());
        organization = organizationRepository.save(organization);

        Role platformAdminRole = roleRepository.findByNameAndIsPlatformTemplateTrue("PLATFORM_ADMIN")
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setName("PLATFORM_ADMIN");
                    r.setPlatformTemplate(true);
                    return roleRepository.save(r);
                });

        Role superAdminRole = roleRepository.findByNameAndIsPlatformTemplateTrue("SUPER_ADMIN")
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setName("SUPER_ADMIN");
                    r.setPlatformTemplate(true);
                    return roleRepository.save(r);
                });

        platformAdminUser = new User();
        platformAdminUser.setWorkEmail("pladmin@test.com");
        platformAdminUser.setUserId("PL_ADMIN_1");
        platformAdminUser.setFullName("Platform Admin");
        platformAdminUser.setRole(platformAdminRole);
        platformAdminUser = userRepository.save(platformAdminUser);

        orgAdminUser = new User();
        orgAdminUser.setWorkEmail("orgadmin@testcorp.com");
        orgAdminUser.setUserId("ORG_ADMIN_1");
        orgAdminUser.setFullName("Org Admin");
        orgAdminUser.setOrganization(organization);
        orgAdminUser.setRole(superAdminRole);
        orgAdminUser = userRepository.save(orgAdminUser);

        regularUser = new User();
        regularUser.setWorkEmail("john.recruiter@testcorp.com");
        regularUser.setUserId("EMP_REC_1");
        regularUser.setFullName("John Recruiter");
        regularUser.setOrganization(organization);
        regularUser = userRepository.save(regularUser);

        platformAdminToken = jwtService.generateAccessToken(platformAdminUser.getUserId(), platformAdminUser.getWorkEmail(), "PLATFORM_ADMIN");
        orgAdminToken = jwtService.generateAccessToken(orgAdminUser.getUserId(), orgAdminUser.getWorkEmail(), "SUPER_ADMIN");
        regularUserToken = jwtService.generateAccessToken(regularUser.getUserId(), regularUser.getWorkEmail(), "EMPLOYEE");
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("Group 1: Permission Master Catalog & Platform Admin Restriction")
    void testPermissionMasterApis() throws Exception {
        // 1. Regular user creates permission -> Forbidden
        mockMvc.perform(post("/api/v1/permissions")
                        .header("Authorization", "Bearer " + regularUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "candidate.create",
                                  "description": "Create new candidates"
                                }
                                """))
                .andExpect(status().isForbidden());

        // 2. Platform Admin creates permission -> Success
        mockMvc.perform(post("/api/v1/permissions")
                        .header("Authorization", "Bearer " + platformAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "candidate.create",
                                  "description": "Create new candidates"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("candidate.create"));

        // 3. List Permissions
        mockMvc.perform(get("/api/v1/permissions")
                        .header("Authorization", "Bearer " + orgAdminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasItem(hasEntry("name", "candidate.create"))));
    }

    @Test
    @DisplayName("Group 2: Permission Group Creation & Management")
    void testPermissionGroupApis() throws Exception {
        Permission p1 = permissionRepository.findByName("candidate.read").orElseGet(() -> permissionRepository.save(new Permission(null, "candidate.read", "Read candidates")));
        Permission p2 = permissionRepository.findByName("candidate.create").orElseGet(() -> permissionRepository.save(new Permission(null, "candidate.create", "Create candidates")));

        String groupCode = "RECRUITMENT_" + System.currentTimeMillis();
        // 1. Platform Admin creates Permission Group
        String groupResp = mockMvc.perform(post("/api/v1/permission-groups")
                        .header("Authorization", "Bearer " + platformAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {
                                  "code": "%s",
                                  "name": "Recruitment Management",
                                  "description": "Recruitment permissions group"
                                }
                                """, groupCode)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.code").value(groupCode))
                .andReturn().getResponse().getContentAsString();

        Long groupId = objectMapper.readTree(groupResp).path("data").path("groupId").asLong();

        // 2. Add permissions to group
        mockMvc.perform(post("/api/v1/permission-groups/" + groupId + "/permissions")
                        .header("Authorization", "Bearer " + platformAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {
                                  "permissionIds": [%d, %d]
                                }
                                """, p1.getId(), p2.getId())))
                .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    @DisplayName("Group 3-8: Complete End-to-End Workflow (Custom Role -> Effective Union -> Assign -> Auth -> Check)")
    void testEndToEndCustomPermissionWorkflow() throws Exception {
        TenantContext.setCurrentTenant(organization.getId());

        // 1. Setup Master Permissions & Group
        Permission pRead = permissionRepository.findByName("candidate.read").orElseGet(() -> permissionRepository.save(new Permission(null, "candidate.read", "Read candidates")));
        Permission pCreate = permissionRepository.findByName("candidate.create").orElseGet(() -> permissionRepository.save(new Permission(null, "candidate.create", "Create candidates")));
        Permission pDirect = permissionRepository.findByName("interview.schedule").orElseGet(() -> permissionRepository.save(new Permission(null, "interview.schedule", "Schedule interviews")));

        String groupCode = "RECRUITMENT_GRP_" + System.currentTimeMillis();
        PermissionGroup group = new PermissionGroup(null, groupCode, "Recruitment Group", "Desc", Set.of(pRead, pCreate));
        group = permissionGroupRepository.save(group);

        String roleName = "Recruitment Lead " + System.currentTimeMillis();
        // 2. Organization Admin creates Custom Role combining Group + Direct Permission
        String roleResp = mockMvc.perform(post("/api/v1/roles")
                        .header("Authorization", "Bearer " + orgAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {
                                  "name": "%s",
                                  "description": "Recruitment Lead Custom Role",
                                  "permissionGroupIds": [%d],
                                  "permissionIds": [%d]
                                }
                                """, roleName, group.getId(), pDirect.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value(roleName))
                .andExpect(jsonPath("$.data.permissions.length()").value(3))
                .andReturn().getResponse().getContentAsString();

        Long customRoleId = objectMapper.readTree(roleResp).path("data").path("roleId").asLong();

        // 3. Assign Custom Role to Regular User
        mockMvc.perform(put("/api/v1/users/" + regularUser.getId() + "/role")
                        .header("Authorization", "Bearer " + orgAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {
                                  "roleId": %d
                                }
                                """, customRoleId)))
                .andExpect(status().isOk());

        // 4. Verify Logged-in User Permission endpoint (/api/v1/auth/me/permissions)
        mockMvc.perform(get("/api/v1/auth/me/permissions")
                        .header("Authorization", "Bearer " + regularUserToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.roles[0].name").value(roleName))
                .andExpect(jsonPath("$.data.permissions", hasItems("candidate.read", "candidate.create", "interview.schedule")));

        // 5. Verify Permission Check API (/api/v1/permissions/check)
        mockMvc.perform(post("/api/v1/permissions/check")
                        .header("Authorization", "Bearer " + regularUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "permission": "candidate.create"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.allowed").value(true));

        mockMvc.perform(post("/api/v1/permissions/check")
                        .header("Authorization", "Bearer " + regularUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "permission": "payroll.delete"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.allowed").value(false));

        // 6. Test Soft Deactivation of Permission
        pCreate.setActive(false);
        permissionRepository.save(pCreate);

        roleService.evictRolePermissionsCache(customRoleId);

        // Effective permissions should no longer include deactivated permission
        List<String> effectiveAfterDeactivation = roleService.getPermissionsForUserId(regularUser.getUserId());
        assertFalse(effectiveAfterDeactivation.contains("candidate.create"), "Soft-deactivated permission must be excluded from effective permissions");
        assertTrue(effectiveAfterDeactivation.contains("candidate.read"));
        assertTrue(effectiveAfterDeactivation.contains("interview.schedule"));
    }
}
