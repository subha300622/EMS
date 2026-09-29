package com.example.ems.maintenance;

import com.example.ems.auth.entity.Role;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.RoleRepository;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.SessionService;
import com.example.ems.auth.service.SessionService.SessionMetadata;
import com.example.ems.maintenance.dto.MaintenanceEnableRequest;
import com.example.ems.maintenance.dto.MaintenanceUpdateRequest;
import com.example.ems.maintenance.entity.MaintenanceConfig;
import com.example.ems.maintenance.repository.MaintenanceConfigRepository;
import com.example.ems.maintenance.service.MaintenanceService;
import com.example.ems.security.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.OffsetDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
class MaintenanceApiIntegrationTest {

        private MockMvc mockMvc;

        @Autowired
        private WebApplicationContext webApplicationContext;

        @Autowired
        private jakarta.servlet.Filter springSecurityFilterChain;

        @Autowired
        private JwtService jwtService;

        @Autowired
        private MaintenanceService maintenanceService;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private RoleRepository roleRepository;

        private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

        @Autowired
        private SessionService sessionService;

        @Autowired
        private MaintenanceConfigRepository maintenanceConfigRepository;

        private User adminUser;
        private User empUser;
        private String platformAdminToken;
        private String employeeToken;

        @BeforeEach
        void setUp() {
                SecurityContextHolder.clearContext();

                // Start each test with maintenance disabled
                maintenanceService.disable();

                mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                                .addFilters(springSecurityFilterChain)
                                .build();

                // Ensure PLATFORM_ADMIN role & user exist
                Role platformAdminRole = roleRepository.findByName("PLATFORM_ADMIN").orElseGet(() -> {
                        Role r = new Role();
                        r.setName("PLATFORM_ADMIN");
                        r.setDescription("Platform Admin Role");
                        return roleRepository.save(r);
                });

                adminUser = userRepository.findByWorkEmail("maint_platform_admin@ems.com").orElseGet(() -> {
                        User u = new User();
                        u.setFullName("Platform Administrator");
                        u.setWorkEmail("maint_platform_admin@ems.com");
                        u.setPassword("encodedPassword123");
                        u.setRole(platformAdminRole);
                        u.setStatus("ACTIVE");
                        u.setUserId("PADM999");
                        return userRepository.save(u);
                });

                // Ensure SUPER_ADMIN role & tenant user exist
                Role superAdminRole = roleRepository.findByName("SUPER_ADMIN").orElseGet(() -> {
                        Role r = new Role();
                        r.setName("SUPER_ADMIN");
                        r.setDescription("Tenant Super Admin Role");
                        return roleRepository.save(r);
                });

                empUser = userRepository.findByWorkEmail("maint_tenant_admin@ems.com").orElseGet(() -> {
                        User u = new User();
                        u.setFullName("Tenant Super Admin");
                        u.setWorkEmail("maint_tenant_admin@ems.com");
                        u.setPassword("encodedPassword123");
                        u.setRole(superAdminRole);
                        u.setRoleId(superAdminRole.getId());
                        u.setRequestedRole("SUPER_ADMIN");
                        u.setOrganizationId(1L);
                        u.setStatus("ACTIVE");
                        u.setUserId("TADM999");
                        return userRepository.save(u);
                });
                empUser.setRole(superAdminRole);
                empUser.setRoleId(superAdminRole.getId());
                empUser.setRequestedRole("SUPER_ADMIN");
                empUser.setOrganizationId(1L);
                empUser = userRepository.save(empUser);

                SessionMetadata adminSession = sessionService.createSession(adminUser.getUserId(),
                                adminUser.getWorkEmail(), "TestAgent", "127.0.0.1");
                platformAdminToken = "Bearer " + jwtService.generateAccessToken(
                                adminUser.getUserId(), adminUser.getWorkEmail(), "PLATFORM_ADMIN", null,
                                adminSession.getSessionId(), adminSession.getSessionVersion(),
                                adminSession.getSessionEpoch());

                SessionMetadata empSession = sessionService
                                .createSession(empUser.getUserId(), empUser.getWorkEmail(), "TestAgent", "127.0.0.1");
                employeeToken = "Bearer " + jwtService.generateAccessToken(
                                empUser.getUserId(), empUser.getWorkEmail(), "SUPER_ADMIN", 1L,
                                empSession.getSessionId(), empSession.getSessionVersion(),
                                empSession.getSessionEpoch());
        }

        @AfterEach
        void tearDown() {
                maintenanceService.disable();
                SecurityContextHolder.clearContext();
        }

        @Test
        @DisplayName("End-to-End Test: Full Maintenance Mode API Lifecycle")
        void testMaintenanceLifecycleAndApiContract() throws Exception {
                // Step 1: Initial state - public maintenance endpoint returns false
                mockMvc.perform(get("/api/v1/public/maintenance"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.maintenance").value(false));

                // Step 2: Enable maintenance mode via POST /api/v1/platform/maintenance/enable
                MaintenanceEnableRequest enableRequest = new MaintenanceEnableRequest(
                                "Scheduled maintenance is in progress. Please try again after 1:00 AM.",
                                null, null, true, false);

                mockMvc.perform(post("/api/v1/platform/maintenance/enable")
                                .header("Authorization", platformAdminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(enableRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.enabled").value(true))
                                .andExpect(jsonPath("$.effective").value(true))
                                .andExpect(jsonPath("$.allowAdminAccess").value(true))
                                .andExpect(jsonPath("$.message").value(
                                                "Scheduled maintenance is in progress. Please try again after 1:00 AM."));

                // Step 3: Public endpoint now returns active status
                mockMvc.perform(get("/api/v1/public/maintenance"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.maintenance").value(true))
                                .andExpect(jsonPath("$.message").value(
                                                "Scheduled maintenance is in progress. Please try again after 1:00 AM."));

                // Step 4: Normal tenant user request is BLOCKED with HTTP 503 and
                // MAINTENANCE_MODE contract
                mockMvc.perform(get("/api/v1/employees")
                                .header("Authorization", employeeToken))
                                .andExpect(status().isServiceUnavailable())
                                .andExpect(jsonPath("$.code").value("MAINTENANCE_MODE"))
                                .andExpect(jsonPath("$.message")
                                                .value("The EMS platform is currently under maintenance."))
                                .andExpect(jsonPath("$.maintenanceMessage").value(
                                                "Scheduled maintenance is in progress. Please try again after 1:00 AM."));

                // Step 5: Platform Admin accesses platform endpoint -> ALLOWED bypass
                mockMvc.perform(get("/api/v1/platform/maintenance")
                                .header("Authorization", platformAdminToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.enabled").value(true))
                                .andExpect(jsonPath("$.effective").value(true));

                // Step 6: Excluded endpoints (actuator) remain accessible
                mockMvc.perform(get("/actuator/health"))
                                .andExpect(status().isOk());

                // Step 7: Update maintenance settings via PUT /api/v1/platform/maintenance
                MaintenanceUpdateRequest updateRequest = new MaintenanceUpdateRequest(
                                true, false, "Emergency maintenance - all access blocked.", null, null, false);

                mockMvc.perform(put("/api/v1/platform/maintenance")
                                .header("Authorization", platformAdminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.enabled").value(true))
                                .andExpect(jsonPath("$.allowAdminAccess").value(false))
                                .andExpect(jsonPath("$.message").value("Emergency maintenance - all access blocked."));

                // When allowAdminAccess = false, even Platform Admin cannot access non-exempt
                // endpoints
                mockMvc.perform(get("/api/v1/employees")
                                .header("Authorization", platformAdminToken))
                                .andExpect(status().isServiceUnavailable())
                                .andExpect(jsonPath("$.code").value("MAINTENANCE_MODE"));

                // Step 8: Disable maintenance mode via POST /api/v1/platform/maintenance/disable
                mockMvc.perform(post("/api/v1/platform/maintenance/disable")
                                .header("Authorization", platformAdminToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.enabled").value(false))
                                .andExpect(jsonPath("$.effective").value(false))
                                .andExpect(jsonPath("$.message").value("Maintenance mode disabled."));

                // Step 9: Public endpoint returns inactive
                mockMvc.perform(get("/api/v1/public/maintenance"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.maintenance").value(false));
        }

        @Test
        @DisplayName("Scheduled Maintenance: inactive before startAt, active between startAt and endAt")
        void testScheduledMaintenanceWindow() throws Exception {
                OffsetDateTime now = OffsetDateTime.now();

                // Schedule maintenance in the future (starts in 2 hours)
                MaintenanceEnableRequest scheduledRequest = new MaintenanceEnableRequest(
                                "Maintenance planned for tonight",
                                now.plusHours(2),
                                now.plusHours(5),
                                true, false);

                mockMvc.perform(post("/api/v1/platform/maintenance/enable")
                                .header("Authorization", platformAdminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(scheduledRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.enabled").value(true))
                                .andExpect(jsonPath("$.effective").value(false))
                                .andExpect(jsonPath("$.status").value("MAINTENANCE_SCHEDULED"));

                // Since it is before startAt, normal requests are NOT blocked
                mockMvc.perform(get("/api/v1/public/maintenance"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.maintenance").value(false));
        }

        @Test
        @DisplayName("Security RBAC: Administrative maintenance APIs require PLATFORM_ADMIN (401 unauthenticated, 403 tenant, 200 platform admin)")
        void testNonPlatformAdminCallsMaintenanceApisForbidden() throws Exception {
                // 1. GET /api/v1/public/maintenance -> 200 OK for everyone
                mockMvc.perform(get("/api/v1/public/maintenance"))
                                .andExpect(status().isOk());
                mockMvc.perform(get("/api/v1/public/maintenance")
                                .header("Authorization", employeeToken))
                                .andExpect(status().isOk());
                mockMvc.perform(get("/api/v1/public/maintenance")
                                .header("Authorization", platformAdminToken))
                                .andExpect(status().isOk());

                // 2. GET /api/v1/platform/maintenance: 401 unauthenticated, 403 tenant, 200 platform admin
                mockMvc.perform(get("/api/v1/platform/maintenance"))
                                .andExpect(status().isUnauthorized());
                mockMvc.perform(get("/api/v1/platform/maintenance")
                                .header("Authorization", employeeToken))
                                .andExpect(status().isForbidden());
                mockMvc.perform(get("/api/v1/platform/maintenance")
                                .header("Authorization", platformAdminToken))
                                .andExpect(status().isOk());

                // 3. PUT /api/v1/platform/maintenance: 401 unauthenticated, 403 tenant
                MaintenanceUpdateRequest updateReq = new MaintenanceUpdateRequest(
                                true, true, "Unauthorized modification", null, null, false);
                mockMvc.perform(put("/api/v1/platform/maintenance")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateReq)))
                                .andExpect(status().isUnauthorized());
                mockMvc.perform(put("/api/v1/platform/maintenance")
                                .header("Authorization", employeeToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateReq)))
                                .andExpect(status().isForbidden());

                // 4. POST /api/v1/platform/maintenance/enable: 401 unauthenticated, 403 tenant
                MaintenanceEnableRequest enableReq = new MaintenanceEnableRequest(
                                "Unauthorized enable", null, null, true, false);
                mockMvc.perform(post("/api/v1/platform/maintenance/enable")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(enableReq)))
                                .andExpect(status().isUnauthorized());
                mockMvc.perform(post("/api/v1/platform/maintenance/enable")
                                .header("Authorization", employeeToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(enableReq)))
                                .andExpect(status().isForbidden());

                // 5. POST /api/v1/platform/maintenance/disable: 401 unauthenticated, 403 tenant
                mockMvc.perform(post("/api/v1/platform/maintenance/disable"))
                                .andExpect(status().isUnauthorized());
                mockMvc.perform(post("/api/v1/platform/maintenance/disable")
                                .header("Authorization", employeeToken))
                                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Validation: Invalid date order (endAt < startAt) and blank message return 400 Bad Request")
        void testValidationRejectsInvalidDatesAndBlankMessage() throws Exception {
                OffsetDateTime now = OffsetDateTime.now();

                // 1. endAt before startAt on enable
                MaintenanceEnableRequest invalidDateEnable = new MaintenanceEnableRequest(
                                "Scheduled window",
                                now.plusHours(3),
                                now.plusHours(1),
                                true, false);
                mockMvc.perform(post("/api/v1/platform/maintenance/enable")
                                .header("Authorization", platformAdminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidDateEnable)))
                                .andExpect(status().isBadRequest());

                // 2. endAt before startAt on update
                MaintenanceUpdateRequest invalidDateUpdate = new MaintenanceUpdateRequest(
                                true, true, "Scheduled window",
                                now.plusHours(3),
                                now.plusHours(1),
                                false);
                mockMvc.perform(put("/api/v1/platform/maintenance")
                                .header("Authorization", platformAdminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidDateUpdate)))
                                .andExpect(status().isBadRequest());

                // 3. Blank message when enabled on update
                MaintenanceUpdateRequest blankMessageUpdate = new MaintenanceUpdateRequest(
                                true, true, "   ", null, null, false);
                mockMvc.perform(put("/api/v1/platform/maintenance")
                                .header("Authorization", platformAdminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(blankMessageUpdate)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Information Leak Prevention: Public maintenance endpoint never exposes internal fields")
        void testPublicMaintenanceEndpointNeverExposesInternalFields() throws Exception {
                MaintenanceEnableRequest enableRequest = new MaintenanceEnableRequest(
                                "Planned system maintenance from 10 PM to 2 AM.",
                                null, null, true, true);
                mockMvc.perform(post("/api/v1/platform/maintenance/enable")
                                .header("Authorization", platformAdminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(enableRequest)))
                                .andExpect(status().isOk());

                mockMvc.perform(get("/api/v1/public/maintenance"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.maintenance").value(true))
                                .andExpect(jsonPath("$.message")
                                                .value("Planned system maintenance from 10 PM to 2 AM."))
                                .andExpect(jsonPath("$.allowAdminAccess").doesNotExist())
                                .andExpect(jsonPath("$.logoutActiveSessions").doesNotExist())
                                .andExpect(jsonPath("$.updatedBy").doesNotExist())
                                .andExpect(jsonPath("$.version").doesNotExist())
                                .andExpect(jsonPath("$.id").doesNotExist());
        }

        @Test
        @DisplayName("Security: Unauthenticated request to protected API receives 503 during maintenance")
        void testUnauthenticatedProtectedApiDuringMaintenance() throws Exception {
                maintenanceService.enable(new MaintenanceEnableRequest(
                                "Maintenance active for all non-admins", null, null, true, false));

                mockMvc.perform(get("/api/v1/employees"))
                                .andExpect(status().isServiceUnavailable())
                                .andExpect(jsonPath("$.code").value("MAINTENANCE_MODE"))
                                .andExpect(jsonPath("$.maintenanceMessage")
                                                .value("Maintenance active for all non-admins"));
        }

        @Test
        @DisplayName("Admin Bypass: Platform Admin allowed bypass on maintenance and platform endpoints")
        void testPlatformAdminBypassOnMultipleEndpoints() throws Exception {
                maintenanceService.enable(new MaintenanceEnableRequest(
                                "Restricted to Platform Admins", null, null, true, false));

                // Platform Admin can access maintenance config
                mockMvc.perform(get("/api/v1/platform/maintenance")
                                .header("Authorization", platformAdminToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.effective").value(true));

                // Actuator health remains accessible
                mockMvc.perform(get("/actuator/health"))
                                .andExpect(status().isOk());

                // Regular employee is blocked with 503
                mockMvc.perform(get("/api/v1/employees")
                                .header("Authorization", employeeToken))
                                .andExpect(status().isServiceUnavailable())
                                .andExpect(jsonPath("$.code").value("MAINTENANCE_MODE"));
        }

        @Test
        @DisplayName("Critical Security Test: logoutActiveSessions=true invalidates active sessions permanently")
        void testSessionRevocationWhenLogoutActiveSessionsTrue() throws Exception {
                // Step 1: Normal access works before maintenance
                mockMvc.perform(get("/api/v1/employees")
                                .header("Authorization", employeeToken))
                                .andExpect(status().isOk());

                // Step 2: Platform admin enables maintenance mode with logoutActiveSessions =
                // true
                MaintenanceEnableRequest enableRequest = new MaintenanceEnableRequest(
                                "Maintenance with active session revocation",
                                null, null, true, true);

                mockMvc.perform(post("/api/v1/platform/maintenance/enable")
                                .header("Authorization", platformAdminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(enableRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.enabled").value(true))
                                .andExpect(jsonPath("$.logoutActiveSessions").value(true));

                // Step 3: During maintenance with logoutActiveSessions=true, the session is already invalidated in DB
                mockMvc.perform(get("/api/v1/employees")
                                .header("Authorization", employeeToken))
                                .andExpect(status().isUnauthorized());

                // Step 4: Platform admin disables maintenance mode
                maintenanceService.disable();

                // Step 5: Normal access has resumed for the platform, BUT the employee's
                // existing JWT
                // must now be rejected because its backing session was REVOKED in the database
                mockMvc.perform(get("/api/v1/employees")
                                .header("Authorization", employeeToken))
                                .andExpect(status().isUnauthorized());

                // Step 6: When the employee logs in again (new session created), access
                // succeeds
                SessionMetadata newSession = sessionService.createSession(empUser.getUserId(), empUser.getWorkEmail(),
                                "TestAgent", "127.0.0.1");
                String newEmployeeToken = "Bearer " + jwtService.generateAccessToken(
                                empUser.getUserId(), empUser.getWorkEmail(), "EMPLOYEE", null,
                                newSession.getSessionId(), newSession.getSessionVersion(),
                                newSession.getSessionEpoch());

                mockMvc.perform(get("/api/v1/employees")
                                .header("Authorization", newEmployeeToken))
                                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Lifecycle: Expired maintenance window allows normal tenant access to resume")
        void testExpiredMaintenanceWindowResumesNormalAccess() throws Exception {
                OffsetDateTime now = OffsetDateTime.now();
                // Window ended 1 hour ago
                MaintenanceConfig config = maintenanceConfigRepository.findDefaultConfig()
                                .orElseGet(MaintenanceConfig::new);
                config.setEnabled(true);
                config.setAllowAdminAccess(false);
                config.setMessage("Expired maintenance window");
                config.setStartAt(now.minusHours(4));
                config.setEndAt(now.minusHours(1));
                maintenanceConfigRepository.save(config);

                // Public maintenance status returns false
                mockMvc.perform(get("/api/v1/public/maintenance"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.maintenance").value(false));

                // Normal employee request is NOT blocked
                mockMvc.perform(get("/api/v1/employees")
                                .header("Authorization", employeeToken))
                                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Performance & Consistency: Cache invalidation immediately reflects new state")
        void testCacheImmediateReflection() throws Exception {
                // Initially disabled -> employee access works
                mockMvc.perform(get("/api/v1/employees")
                                .header("Authorization", employeeToken))
                                .andExpect(status().isOk());

                // Immediately enable maintenance -> employee access blocked instantly
                maintenanceService.enable(new MaintenanceEnableRequest(
                                "Immediate downtime", null, null, true, false));
                mockMvc.perform(get("/api/v1/employees")
                                .header("Authorization", employeeToken))
                                .andExpect(status().isServiceUnavailable())
                                .andExpect(jsonPath("$.code").value("MAINTENANCE_MODE"));

                // Immediately disable maintenance -> employee access restored instantly
                maintenanceService.disable();
                mockMvc.perform(get("/api/v1/employees")
                                .header("Authorization", employeeToken))
                                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Data Integrity: Concurrent maintenance updates trigger optimistic locking protection")
        void testConcurrentMaintenanceUpdatesOptimisticLocking() {
                MaintenanceConfig config1 = maintenanceConfigRepository.findDefaultConfig().orElseThrow();
                MaintenanceConfig config2 = new MaintenanceConfig();
                config2.setId(config1.getId());
                config2.setEnabled(config1.getEnabled());
                config2.setAllowAdminAccess(config1.getAllowAdminAccess());
                config2.setMessage("Stale copy");
                config2.setVersion(config1.getVersion());

                assertEquals(config1.getVersion(), config2.getVersion());

                // First update succeeds and increments version
                config1.setMessage("Update by Thread 1");
                maintenanceConfigRepository.saveAndFlush(config1);

                // Second update with stale version must fail with optimistic lock failure
                config2.setMessage("Update by Thread 2 with stale version");
                assertThrows(
                                org.springframework.orm.ObjectOptimisticLockingFailureException.class,
                                () -> maintenanceConfigRepository.saveAndFlush(config2));
        }
}
