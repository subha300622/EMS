package com.example.ems.common.controller;

import com.example.ems.approval.entity.WorkflowType;
import com.example.ems.approval.service.ApprovalWorkflowEngineService;
import com.example.ems.auth.entity.Permission;
import com.example.ems.auth.entity.Role;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.entity.UserSession;
import com.example.ems.auth.repository.PermissionRepository;
import com.example.ems.auth.repository.RoleRepository;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.DatabaseSessionStore;
import com.example.ems.common.dto.manager.NotificationPreferenceDto;
import com.example.ems.common.dto.notification.NotificationCommand;
import com.example.ems.common.entity.NotificationCategory;
import com.example.ems.common.entity.NotificationPriority;
import com.example.ems.common.entity.NotificationType;
import com.example.ems.common.repository.NotificationRepository;
import com.example.ems.common.service.NotificationService;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.onboarding.dto.notification.OnboardingNotificationRemindRequest;
import com.example.ems.onboarding.dto.notification.OnboardingNotificationResendRequest;
import com.example.ems.onboarding.entity.Onboarding;
import com.example.ems.onboarding.repository.OnboardingRepository;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.entity.OrganizationStatus;
import com.example.ems.organization.repository.OrganizationRepository;
import com.example.ems.security.context.TenantContext;
import com.example.ems.security.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.Filter;
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
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
public class ModuleNotificationEndToEndIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private Filter springSecurityFilterChain;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private ApprovalWorkflowEngineService workflowEngineService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private OnboardingRepository onboardingRepository;

    @Autowired
    private DatabaseSessionStore databaseSessionStore;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private Organization org;
    private Employee employee;
    private User testUser;
    private String authToken;
    private String rawToken;
    private Role fullRole;

    private Permission getOrCreatePermission(String name) {
        return permissionRepository.findByName(name)
                .map(p -> {
                    if (!Boolean.TRUE.equals(p.getActive())) {
                        p.setActive(true);
                        return permissionRepository.save(p);
                    }
                    return p;
                })
                .orElseGet(() -> {
                    Permission p = new Permission();
                    p.setName(name);
                    p.setDescription(name);
                    p.setActive(true);
                    return permissionRepository.save(p);
                });
    }

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        TenantContext.clear();

        long timestamp = System.currentTimeMillis();

        // Permissions
        Permission permSched = getOrCreatePermission("schedule.self.notification.read");
        Permission permDoc = getOrCreatePermission("document.self.read");
        Permission permGoal = getOrCreatePermission("GOAL_CONFIG_VIEW");
        Permission permSuper = getOrCreatePermission("superadmin.access");
        Permission permDash = getOrCreatePermission("employee.dashboard.read");

        fullRole = new Role();
        fullRole.setName("ROLE_ADMIN_" + timestamp);
        fullRole.setPermissions(new HashSet<>(Set.of(permSched, permDoc, permGoal, permSuper, permDash)));
        fullRole.setDirectPermissions(new HashSet<>(Set.of(permSched, permDoc, permGoal, permSuper, permDash)));
        fullRole = roleRepository.save(fullRole);

        // Organization
        org = new Organization();
        org.setName("Notif Test Org " + timestamp);
        org.setOrganizationCode("ORG-NTF-" + timestamp);
        org.setStatus(OrganizationStatus.ACTIVE);
        org = organizationRepository.save(org);

        // Employee
        employee = new Employee();
        employee.setFullName("Notification Tester");
        employee.setEmail("notif.tester." + timestamp + "@company.com");
        employee.setEmployeeId("EMP-" + timestamp);
        employee.setOrganization(org);
        employee.setStatus("ACTIVE");
        employee = employeeRepository.save(employee);

        // User
        testUser = new User();
        testUser.setUserId(employee.getEmployeeId());
        testUser.setWorkEmail(employee.getEmail());
        testUser.setFullName(employee.getFullName());
        testUser.setRole(fullRole);
        testUser.setOrganization(org);
        testUser.setOrganizationId(org.getId());
        testUser.setStatus("ACTIVE");
        testUser = userRepository.save(testUser);

        // Session & Token
        String sessionId = "sess-" + timestamp;
        databaseSessionStore.save(new UserSession(sessionId, employee.getEmployeeId(), testUser.getWorkEmail(),
                "Agent", "127.0.0.1", "rt-" + timestamp, LocalDateTime.now(), LocalDateTime.now().plusHours(2),
                false, 1, 1L, "ACTIVE"));

        rawToken = jwtService.generateAccessToken(employee.getEmployeeId(), testUser.getWorkEmail(),
                fullRole.getName(), org.getId(), sessionId, 1, 1L);
        authToken = "Bearer " + rawToken;

        TenantContext.setCurrentTenant(org.getId());

        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .addFilters(springSecurityFilterChain)
                .build();
    }

    @Test
    @DisplayName("Verify Central Notification Management APIs work and show created notifications")
    void testNotificationFeedAndLifecycle() throws Exception {
        // 1. Initial State: Unread count should be 0
        mockMvc.perform(get("/api/v1/notifications/unread-count")
                        .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.count").value(0));

        // 2. Create notification directly via NotificationService
        NotificationCommand command = NotificationCommand.builder()
                .organizationId(org.getId())
                .recipientUserId(testUser.getId())
                .type(NotificationType.LEAVE_APPROVED)
                .category(NotificationCategory.LEAVE)
                .priority(NotificationPriority.HIGH)
                .title("Leave Request Approved")
                .message("Your leave request for next week has been approved.")
                .actionUrl("/leaves/101")
                .build();
        notificationService.create(command);

        // 3. Verify notification shows in the feed
        mockMvc.perform(get("/api/v1/notifications")
                        .header("Authorization", authToken)
                        .param("page", "0")
                        .param("size", "10")
                        .param("type", "ALL")
                        .param("status", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].title").value("Leave Request Approved"))
                .andExpect(jsonPath("$.data.content[0].read").value(false));

        // 4. Verify unread count is now 1
        mockMvc.perform(get("/api/v1/notifications/unread-count")
                        .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.count").value(1));

        // 5. Verify stats endpoint shows total 1 and unread 1
        mockMvc.perform(get("/api/v1/notifications/stats")
                        .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.unread").value(1));

        // 6. Verify page-data consolidated endpoint returns stats and notification
        mockMvc.perform(get("/api/v1/notifications/page-data")
                        .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.unreadCount").value(1))
                .andExpect(jsonPath("$.data.notifications", hasSize(1)))
                .andExpect(jsonPath("$.data.notifications[0].title").value("Leave Request Approved"));

        // 7. Get notification ID and mark as read
        Long notifId = notificationRepository.findByUserIdOrderByCreatedAtDesc(testUser.getId()).get(0).getId();

        mockMvc.perform(patch("/api/v1/notifications/" + notifId + "/read")
                        .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 8. Verify unread count drops to 0
        mockMvc.perform(get("/api/v1/notifications/unread-count")
                        .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.count").value(0));

        // 9. Verify filter by status=READ shows the notification, but status=UNREAD returns empty
        mockMvc.perform(get("/api/v1/notifications")
                        .header("Authorization", authToken)
                        .param("status", "READ"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].read").value(true));

        mockMvc.perform(get("/api/v1/notifications")
                        .header("Authorization", authToken)
                        .param("status", "UNREAD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(0)));

        // 10. Delete notification
        mockMvc.perform(delete("/api/v1/notifications/" + notifId)
                        .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // Verify feed is now empty
        mockMvc.perform(get("/api/v1/notifications")
                        .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(0)));
    }

    @Test
    @DisplayName("Verify Approval Workflow module triggers notification and shows in notification feed")
    void testApprovalWorkflowModuleNotificationShows() throws Exception {
        // Start approval workflow assigned to testEmployee
        transactionTemplate.execute(status ->
                workflowEngineService.startWorkflow(
                        WorkflowType.LEAVE_APPROVAL,
                        "LEAVE",
                        "LV-TEST-8801",
                        employee,
                        null
                )
        );

        // Verify In-App Notification was generated for testUser
        var notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(testUser.getId());
        assertFalse(notifications.isEmpty(), "Approval notification should be created");

        // Verify the notification shows up via the REST API
        mockMvc.perform(get("/api/v1/notifications")
                        .header("Authorization", authToken)
                        .param("type", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].title", containsString("Approval Required")))
                .andExpect(jsonPath("$.data.content[0].message", containsString("LV-TEST-8801")))
                .andExpect(jsonPath("$.data.content[0].priority").value("HIGH"))
                .andExpect(jsonPath("$.data.content[0].read").value(false));

        // Verify stats reflects APPROVAL notification
        mockMvc.perform(get("/api/v1/notifications/stats")
                        .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.approvals").value(1))
                .andExpect(jsonPath("$.data.unread").value(1));

        // Mark all as read
        mockMvc.perform(patch("/api/v1/notifications/read-all")
                        .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // Verify unread count is now 0
        mockMvc.perform(get("/api/v1/notifications/unread-count")
                        .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.count").value(0));
    }

    @Test
    @DisplayName("Verify Support module notifications show in feed")
    void testSupportModuleNotificationShows() throws Exception {
        // Send a support ticket notification
        notificationService.sendNotification(testUser, "Support Ticket Assigned: TKT-10492", "Ticket regarding network access has been assigned to you.");

        // Verify it shows in GET /api/v1/notifications
        mockMvc.perform(get("/api/v1/notifications")
                        .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].title").value("Support Ticket Assigned: TKT-10492"))
                .andExpect(jsonPath("$.data.content[0].message", containsString("network access")));

        // Verify unread count shows 1
        mockMvc.perform(get("/api/v1/notifications/unread-count")
                        .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.count").value(1));
    }

    @Test
    @DisplayName("Verify Notification Preferences GET and PUT endpoints")
    void testNotificationPreferencesEndpoints() throws Exception {
        // GET preferences
        mockMvc.perform(get("/api/v1/notifications/preferences")
                        .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.emailNotifications").value(true));

        // PUT preferences
        NotificationPreferenceDto updateDto = new NotificationPreferenceDto(
                false, false, true, true, false
        );

        mockMvc.perform(put("/api/v1/notifications/preferences")
                        .header("Authorization", authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.emailNotifications").value(false))
                .andExpect(jsonPath("$.data.pushNotifications").value(false))
                .andExpect(jsonPath("$.data.approvalAlerts").value(true));
    }

    @Test
    @DisplayName("Verify Onboarding Module Notification APIs show notifications log and handle reminder/resend")
    void testOnboardingModuleNotificationApis() throws Exception {
        // Create an onboarding record
        Onboarding onboarding = new Onboarding();
        onboarding.setEmployee(employee);
        onboarding.setStatus("IN_PROGRESS");
        onboarding = onboardingRepository.save(onboarding);

        // GET /api/v1/onboarding/{id}/notifications
        mockMvc.perform(get("/api/v1/onboarding/" + onboarding.getId() + "/notifications")
                        .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", notNullValue()));

        // POST /api/v1/onboarding/{id}/notifications/remind
        OnboardingNotificationRemindRequest remindReq = new OnboardingNotificationRemindRequest();
        remindReq.setTaskId(1L);
        remindReq.setChannel("EMAIL");

        mockMvc.perform(post("/api/v1/onboarding/" + onboarding.getId() + "/notifications/remind")
                        .header("Authorization", authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(remindReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // POST /api/v1/onboarding/{id}/notifications/resend
        OnboardingNotificationResendRequest resendReq = new OnboardingNotificationResendRequest();
        resendReq.setNotificationType("WELCOME_EMAIL");
        resendReq.setChannel("EMAIL");

        mockMvc.perform(post("/api/v1/onboarding/" + onboarding.getId() + "/notifications/resend")
                        .header("Authorization", authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resendReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("Verify My-Schedule Module Notification API returns notifications list")
    void testScheduleModuleNotificationApi() throws Exception {
        mockMvc.perform(get("/api/v1/my-schedule/notifications")
                        .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.notifications").isArray());
    }

    @Test
    @DisplayName("Verify My-Document Module Notification API returns expiry alerts")
    void testDocumentModuleNotificationApi() throws Exception {
        mockMvc.perform(get("/api/v1/my-documents/notifications")
                        .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.notifications").isArray());
    }

    @Test
    @DisplayName("Verify Goal Module Notification Settings API returns tenant notification config")
    void testGoalModuleNotificationSettingsApi() throws Exception {
        mockMvc.perform(get("/api/v1/goal-config/notifications")
                        .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("Verify Employee Dashboard reflects unread notifications count")
    void testEmployeeDashboardReflectsNotificationCount() throws Exception {
        // Notification count should start at 0
        mockMvc.perform(get("/api/v1/me/dashboard")
                        .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.notifications.unread").value(0));

        // Create a notification for the user
        notificationService.sendNotification(testUser, "System Notice", "A new company policy has been released.");

        // Dashboard unread notification count should now be 1
        mockMvc.perform(get("/api/v1/me/dashboard")
                        .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.notifications.unread").value(1));
    }
}
