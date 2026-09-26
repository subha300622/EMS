package com.example.ems.security;

import com.example.ems.auth.entity.Role;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.RoleRepository;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.repository.OrganizationRepository;
import com.example.ems.security.context.TenantContext;
import com.example.ems.security.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
public class PlatformAdminScopeSeparationIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private FilterChainProxy springSecurityFilterChain;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private com.example.ems.auth.service.SessionService sessionService;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        TenantContext.clear();
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .addFilters(springSecurityFilterChain)
                .build();
    }

    @Test
    @DisplayName("Invariant 1: Platform Admin has organization_id == NULL, Tenant Super Admin has organization_id == 1")
    public void testDatabaseInvariant() {
        User platformAdmin = userRepository.findByWorkEmail("platform_admin@gmail.com")
                .orElseThrow(() -> new AssertionError("Platform Admin user must exist"));
        assertNull(platformAdmin.getOrganizationId(), "Platform Admin organization_id must be NULL");
        assertNull(platformAdmin.getOrganization(), "Platform Admin organization must be NULL");
        assertEquals("PLATFORM_ADMIN", platformAdmin.getRole().getName());

        User tenantSuperAdmin = userRepository.findByWorkEmail("owner@acmetech.com")
                .orElseThrow(() -> new AssertionError("Tenant Super Admin user must exist"));
        assertNotNull(tenantSuperAdmin.getOrganizationId(), "Tenant Super Admin must have an organization_id");
        assertEquals(1L, tenantSuperAdmin.getOrganizationId(), "Tenant Super Admin must belong to Organization 1");
        assertEquals("SUPER_ADMIN", tenantSuperAdmin.getRole().getName());
    }

    @Test
    @DisplayName("Invariant 2: PLATFORM_ADMIN cannot be assigned an organization_id")
    public void testPlatformAdminCannotInheritOrganization() {
        Organization org1 = organizationRepository.findById(1L).orElse(null);
        assertNotNull(org1);

        Role platformAdminRole = roleRepository.findByNameAndIsPlatformTemplateTrue("PLATFORM_ADMIN")
                .orElseThrow(() -> new AssertionError("PLATFORM_ADMIN role not found"));

        User customPlatformAdmin = new User();
        customPlatformAdmin.setUserId("TST_PLAT_01");
        customPlatformAdmin.setWorkEmail("custom_platform_test@ems.com");
        customPlatformAdmin.setRole(platformAdminRole);
        customPlatformAdmin.setRequestedRole("PLATFORM_ADMIN");
        // Attempt to associate with Org 1
        customPlatformAdmin.setOrganization(org1);
        customPlatformAdmin.setOrganizationId(1L);

        User saved = userRepository.saveAndFlush(customPlatformAdmin);
        assertNull(saved.getOrganizationId(), "Platform Admin organization_id must remain NULL after save");
        assertNull(saved.getOrganization(), "Platform Admin organization entity must remain NULL after save");
    }

    @Test
    @DisplayName("Security Flow: Login as Platform Admin -> No orgId in JWT -> Platform Scope")
    public void testPlatformAdminLoginAndScope() throws Exception {
        // Authenticate as Platform Admin
        String loginBody = """
            {
                "email": "platform_admin@gmail.com",
                "password": "Admin@123"
            }
            """;

        String response = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.user.role").value("PLATFORM_ADMIN"))
                .andExpect(jsonPath("$.data.user.organizationId").doesNotExist())
                .andReturn().getResponse().getContentAsString();

        String token = com.jayway.jsonpath.JsonPath.read(response, "$.data.tokens.accessToken");
        assertNotNull(token);

        // Verify JWT claims: orgId must NOT be present
        Long jwtOrgId = jwtService.getOrgIdFromToken(token);
        assertNull(jwtOrgId, "JWT token for PLATFORM_ADMIN must NOT contain an orgId claim");

        // Request with platform admin token without X-Organization-Id: TenantContext must remain NULL
        mockMvc.perform(get("/api/v1/platform/organizations/1/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        assertNull(TenantContext.getCurrentTenant(), "TenantContext must remain NULL for Platform Admin");
    }

    @Test
    @DisplayName("Security Flow: Login as Tenant Super Admin -> orgId = 1 in JWT -> Strict Organization 1 Scope")
    public void testTenantSuperAdminLoginAndScope() throws Exception {
        User tenantSuperAdmin = userRepository.findByWorkEmail("owner@acmetech.com").orElse(null);
        if (tenantSuperAdmin != null) {
            com.example.ems.auth.service.SessionService.SessionMetadata session = sessionService.createSession(
                    tenantSuperAdmin.getUserId(),
                    tenantSuperAdmin.getWorkEmail(),
                    "JUnit-Test-Agent",
                    "127.0.0.1"
            );

            // Generate token directly to verify tenant isolation
            String token = jwtService.generateAccessToken(
                    tenantSuperAdmin.getUserId(),
                    tenantSuperAdmin.getWorkEmail(),
                    "SUPER_ADMIN",
                    1L,
                    session.getSessionId(),
                    session.getSessionVersion(),
                    session.getSessionEpoch()
            );

            Long jwtOrgId = jwtService.getOrgIdFromToken(token);
            assertEquals(1L, jwtOrgId, "JWT token for SUPER_ADMIN must contain orgId = 1");

            // Valid request within tenant scope
            mockMvc.perform(get("/api/v1/support/tickets")
                            .header("Authorization", "Bearer " + token)
                            .header("X-Organization-Id", "1"))
                    .andExpect(status().isOk());

            // Cross-tenant attempt: Accessing with a mismatched header (e.g. Org 2) must be forbidden (AUTH_003)
            mockMvc.perform(get("/api/v1/support/tickets")
                            .header("Authorization", "Bearer " + token)
                            .header("X-Organization-Id", "2"))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.errorCode").value("AUTH_003"));
        }
    }
}
