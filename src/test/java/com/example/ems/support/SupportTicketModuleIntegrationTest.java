package com.example.ems.support;

import com.example.ems.auth.entity.Role;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.entity.UserSession;
import com.example.ems.auth.repository.PermissionRepository;
import com.example.ems.auth.repository.RoleRepository;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.DatabaseSessionStore;
import com.example.ems.auth.service.PermissionRegistry;
import com.example.ems.config.GlobalExceptionHandler;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.entity.OrganizationStatus;
import com.example.ems.organization.repository.OrganizationRepository;
import com.example.ems.security.JwtAuthenticationFilter;
import com.example.ems.security.context.TenantContext;
import com.example.ems.security.service.JwtService;
import com.example.ems.support.controller.SupportDashboardController;
import com.example.ems.support.controller.SupportSlaController;
import com.example.ems.support.controller.SupportTicketController;
import com.example.ems.support.dto.*;
import com.example.ems.support.entity.*;
import com.example.ems.support.repository.*;
import com.example.ems.support.scheduler.SupportEscalationScheduler;
import com.example.ems.common.dto.ApiResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class SupportTicketModuleIntegrationTest {

        private MockMvc mockMvc;

        @Autowired
        private SupportTicketController supportTicketController;

        @Autowired
        private SupportSlaController supportSlaController;

        @Autowired
        private SupportDashboardController supportDashboardController;

        @Autowired
        private SupportEscalationScheduler escalationScheduler;

        @Autowired
        private AuthenticationManager authenticationManager;

        @Autowired
        private AuthenticationEntryPoint authenticationEntryPoint;

        @Autowired
        private Environment environment;

        @Autowired
        private JwtService jwtService;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private DatabaseSessionStore databaseSessionStore;

        @Autowired
        private EmployeeRepository employeeRepository;

        @Autowired
        private OrganizationRepository organizationRepository;

        @Autowired
        private RoleRepository roleRepository;

        @Autowired
        private PermissionRepository permissionRepository;

        @Autowired
        private MySupportCategoryRepository categoryRepository;

        @Autowired
        private MySupportTicketRepository ticketRepository;

        @Autowired
        private SupportEscalationHistoryRepository escalationHistoryRepository;

        @Autowired
        private ObjectMapper objectMapper;

        private Organization orgA;
        private Organization orgB;

        private Employee employeeA;
        private Employee engineerA1;
        private Employee engineerA2;
        private Employee employeeB;

        private User userA;
        private User engineerUserA1;
        private User userB;

        private String tokenUserA;
        private String tokenEngineerA1;
        private String tokenUserB;

        private MySupportCategory categoryPayroll;
        private MySupportCategory categoryIT;
        private MySupportCategory categoryOrgB;

        private com.example.ems.auth.entity.Permission getOrCreatePermission(String name) {
                return permissionRepository.findByName(name)
                                .map(p -> {
                                        if (!Boolean.TRUE.equals(p.getActive())) {
                                                p.setActive(true);
                                                return permissionRepository.save(p);
                                        }
                                        return p;
                                })
                                .orElseGet(() -> {
                                        com.example.ems.auth.entity.Permission p = new com.example.ems.auth.entity.Permission();
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

                JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter(
                                authenticationManager, authenticationEntryPoint, environment, jwtService,
                                userRepository);

                mockMvc = MockMvcBuilders
                                .standaloneSetup(supportTicketController, supportSlaController,
                                                supportDashboardController)
                                .addFilters(jwtFilter)
                                .setControllerAdvice(new GlobalExceptionHandler())
                                .build();

                // Ensure support permissions exist
                var pCreate = getOrCreatePermission(PermissionRegistry.SUPPORT_TICKET_CREATE);
                var pView = getOrCreatePermission(PermissionRegistry.SUPPORT_TICKET_VIEW);
                var pReview = getOrCreatePermission(PermissionRegistry.SUPPORT_TICKET_REVIEW);
                var pAssign = getOrCreatePermission(PermissionRegistry.SUPPORT_TICKET_ASSIGN);
                var pReassign = getOrCreatePermission(PermissionRegistry.SUPPORT_TICKET_REASSIGN);
                var pPriority = getOrCreatePermission(PermissionRegistry.SUPPORT_TICKET_PRIORITY_UPDATE);
                var pSla = getOrCreatePermission(PermissionRegistry.SUPPORT_TICKET_SLA_MANAGE);
                var pEscalate = getOrCreatePermission(PermissionRegistry.SUPPORT_TICKET_ESCALATE);
                var pResolve = getOrCreatePermission(PermissionRegistry.SUPPORT_TICKET_RESOLVE);
                var pClose = getOrCreatePermission(PermissionRegistry.SUPPORT_TICKET_CLOSE);
                var pReport = getOrCreatePermission(PermissionRegistry.SUPPORT_TICKET_REPORT);

                Set<com.example.ems.auth.entity.Permission> allSupportPerms = Set.of(
                                pCreate, pView, pReview, pAssign, pReassign, pPriority, pSla, pEscalate, pResolve,
                                pClose, pReport);

                Role supportFullRole = new Role();
                supportFullRole.setName("SUPPORT_ADMIN_ROLE_" + System.currentTimeMillis());
                supportFullRole.setPermissions(new HashSet<>(allSupportPerms));
                supportFullRole.setDirectPermissions(new HashSet<>(allSupportPerms));
                supportFullRole = roleRepository.save(supportFullRole);

                // 1. Create Org A and Org B
                orgA = new Organization();
                orgA.setName("Org Alpha Support " + System.currentTimeMillis());
                orgA.setOrganizationCode("ORGA-SUP-" + System.currentTimeMillis());
                orgA.setStatus(OrganizationStatus.ACTIVE);
                orgA = organizationRepository.save(orgA);

                orgB = new Organization();
                orgB.setName("Org Beta Support " + System.currentTimeMillis());
                orgB.setOrganizationCode("ORGB-SUP-" + System.currentTimeMillis());
                orgB.setStatus(OrganizationStatus.ACTIVE);
                orgB = organizationRepository.save(orgB);

                // Employees for Org A
                employeeA = new Employee();
                employeeA.setFullName("Alice Requester");
                employeeA.setEmail("alice.requester@orga.com");
                employeeA.setEmployeeId("EMP-REQ-" + System.currentTimeMillis());
                employeeA.setDepartment("Finance");
                employeeA.setStatus("ACTIVE");
                employeeA.setOrganization(orgA);
                employeeA = employeeRepository.save(employeeA);

                engineerA1 = new Employee();
                engineerA1.setFullName("David Engineer");
                engineerA1.setEmail("david.engineer@orga.com");
                engineerA1.setEmployeeId("EMP-ENG1-" + System.currentTimeMillis());
                engineerA1.setDepartment("IT Support");
                engineerA1.setStatus("ACTIVE");
                engineerA1.setOrganization(orgA);
                engineerA1 = employeeRepository.save(engineerA1);

                engineerA2 = new Employee();
                engineerA2.setFullName("Samantha Support");
                engineerA2.setEmail("samantha.support@orga.com");
                engineerA2.setEmployeeId("EMP-ENG2-" + System.currentTimeMillis());
                engineerA2.setDepartment("IT Support");
                engineerA2.setStatus("ACTIVE");
                engineerA2.setOrganization(orgA);
                engineerA2 = employeeRepository.save(engineerA2);

                // Employee for Org B
                employeeB = new Employee();
                employeeB.setFullName("Bob Beta");
                employeeB.setEmail("bob.beta@orgb.com");
                employeeB.setEmployeeId("EMP-BETA-" + System.currentTimeMillis());
                employeeB.setDepartment("HR");
                employeeB.setStatus("ACTIVE");
                employeeB.setOrganization(orgB);
                employeeB = employeeRepository.save(employeeB);

                // User accounts
                userA = new User();
                userA.setUserId(employeeA.getEmployeeId());
                userA.setWorkEmail(employeeA.getEmail());
                userA.setFullName(employeeA.getFullName());
                userA.setRole(supportFullRole);
                userA.setOrganization(orgA);
                userA.setOrganizationId(orgA.getId());
                userA.setStatus("ACTIVE");
                userA = userRepository.save(userA);

                engineerUserA1 = new User();
                engineerUserA1.setUserId(engineerA1.getEmployeeId());
                engineerUserA1.setWorkEmail(engineerA1.getEmail());
                engineerUserA1.setFullName(engineerA1.getFullName());
                engineerUserA1.setRole(supportFullRole);
                engineerUserA1.setOrganization(orgA);
                engineerUserA1.setOrganizationId(orgA.getId());
                engineerUserA1.setStatus("ACTIVE");
                engineerUserA1 = userRepository.save(engineerUserA1);

                userB = new User();
                userB.setUserId(employeeB.getEmployeeId());
                userB.setWorkEmail(employeeB.getEmail());
                userB.setFullName(employeeB.getFullName());
                userB.setRole(supportFullRole);
                userB.setOrganization(orgB);
                userB.setOrganizationId(orgB.getId());
                userB.setStatus("ACTIVE");
                userB = userRepository.save(userB);

                // Sessions & Tokens
                String sA = "session-a-" + System.currentTimeMillis();
                databaseSessionStore.save(new UserSession(sA, employeeA.getEmployeeId(), userA.getWorkEmail(),
                                "Agent", "127.0.0.1", "rt-a", LocalDateTime.now(), LocalDateTime.now().plusHours(1),
                                false, 1, 1L, "ACTIVE"));
                tokenUserA = jwtService.generateAccessToken(employeeA.getEmployeeId(), userA.getWorkEmail(),
                                supportFullRole.getName(), orgA.getId(), sA, 1, 1L);

                String sEng = "session-eng-" + System.currentTimeMillis();
                databaseSessionStore.save(new UserSession(sEng, engineerA1.getEmployeeId(),
                                engineerUserA1.getWorkEmail(),
                                "Agent", "127.0.0.1", "rt-eng", LocalDateTime.now(), LocalDateTime.now().plusHours(1),
                                false, 1, 1L, "ACTIVE"));
                tokenEngineerA1 = jwtService.generateAccessToken(engineerA1.getEmployeeId(),
                                engineerUserA1.getWorkEmail(),
                                supportFullRole.getName(), orgA.getId(), sEng, 1, 1L);

                String sB = "session-b-" + System.currentTimeMillis();
                databaseSessionStore.save(new UserSession(sB, employeeB.getEmployeeId(), userB.getWorkEmail(),
                                "Agent", "127.0.0.1", "rt-b", LocalDateTime.now(), LocalDateTime.now().plusHours(1),
                                false, 1, 1L, "ACTIVE"));
                tokenUserB = jwtService.generateAccessToken(employeeB.getEmployeeId(), userB.getWorkEmail(),
                                supportFullRole.getName(), orgB.getId(), sB, 1, 1L);

                // Categories
                categoryPayroll = new MySupportCategory();
                categoryPayroll.setName("Payroll Support " + System.currentTimeMillis());
                categoryPayroll.setIcon("currency");
                categoryPayroll.setOrganization(orgA);
                categoryPayroll = categoryRepository.save(categoryPayroll);

                categoryIT = new MySupportCategory();
                categoryIT.setName("IT Infrastructure " + System.currentTimeMillis());
                categoryIT.setIcon("laptop");
                categoryIT.setOrganization(orgA);
                categoryIT = categoryRepository.save(categoryIT);

                categoryOrgB = new MySupportCategory();
                categoryOrgB.setName("Org B Support " + System.currentTimeMillis());
                categoryOrgB.setIcon("building");
                categoryOrgB.setOrganization(orgB);
                categoryOrgB = categoryRepository.save(categoryOrgB);
        }

        @AfterEach
        void tearDown() {
                SecurityContextHolder.clearContext();
                TenantContext.clear();
        }

        @Test
        void testCompleteSupportTicketLifecycleWorkflow() throws Exception {
                // 1. Create Ticket (POST /api/v1/support/tickets)
                CreateSupportTicketRequest createReq = new CreateSupportTicketRequest();
                createReq.setSubject("Unable to access payroll module");
                createReq.setDescription("Employee cannot access payroll after role update.");
                createReq.setCategoryId(categoryPayroll.getId());
                createReq.setPriority("HIGH");
                createReq.setSource("WEB");

                String createJson = objectMapper.writeValueAsString(createReq);

                String createResStr = mockMvc.perform(post("/api/v1/support/tickets")
                                .header("Authorization", "Bearer " + tokenUserA)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createJson))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.organizationId").isNotEmpty())
                                .andExpect(jsonPath("$.data.id").isNotEmpty())
                                .andExpect(jsonPath("$.data.ticketNumber", startsWith("ST-")))
                                .andExpect(jsonPath("$.data.subject").value("Unable to access payroll module"))
                                .andExpect(jsonPath("$.data.priority").value("HIGH"))
                                .andExpect(jsonPath("$.data.status").value("NEW"))
                                .andExpect(jsonPath("$.data.isOverdue").value(false))
                                .andExpect(jsonPath("$.data.isEscalated").value(false))
                                .andReturn().getResponse().getContentAsString();

                ApiResponse<SupportTicketDetailResponse> createApiResp = objectMapper.readValue(createResStr,
                                new TypeReference<ApiResponse<SupportTicketDetailResponse>>() {});
                SupportTicketDetailResponse ticketDetail = createApiResp.getData();
                Long ticketId = ticketDetail.getId();
                assertNotNull(ticketId);

                // 2. Get Ticket (GET /api/v1/support/tickets/{ticketId})
                mockMvc.perform(get("/api/v1/support/tickets/" + ticketId)
                                .header("Authorization", "Bearer " + tokenUserA))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.id").value(ticketId))
                                .andExpect(jsonPath("$.data.category.name").value(categoryPayroll.getName()))
                                .andExpect(jsonPath("$.data.status").value("NEW"));

                // 3. Manager Review: ACCEPT Ticket (POST
                // /api/v1/support/tickets/{ticketId}/review)
                SupportTicketReviewRequest acceptReq = new SupportTicketReviewRequest();
                acceptReq.setAction("ACCEPT");
                acceptReq.setPriority("HIGH");
                acceptReq.setEstimatedHours(4.0);
                acceptReq.setCategoryId(categoryIT.getId());

                mockMvc.perform(post("/api/v1/support/tickets/" + ticketId + "/review")
                                .header("Authorization", "Bearer " + tokenUserA)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(acceptReq)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.status").value("NEW"))
                                .andExpect(jsonPath("$.data.estimatedHours").value(4.0))
                                .andExpect(jsonPath("$.data.slaHours").value(4))
                                .andExpect(jsonPath("$.data.category.id").value(categoryIT.getId()))
                                .andExpect(jsonPath("$.data.category.name").value(categoryIT.getName()));

                // 4. Assign Engineer (PUT /api/v1/support/tickets/{ticketId}/assignment) ->
                // status becomes IN_PROGRESS
                SupportTicketAssignRequest assignReq = new SupportTicketAssignRequest(engineerA1.getId(), null);

                mockMvc.perform(put("/api/v1/support/tickets/" + ticketId + "/assignment")
                                .header("Authorization", "Bearer " + tokenUserA)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(assignReq)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"))
                                .andExpect(jsonPath("$.data.assignedTo.id").value(engineerA1.getId()))
                                .andExpect(jsonPath("$.data.assignedTo.name").value(engineerA1.getFullName()));

                // 5. Reassign Ticket
                SupportTicketAssignRequest reassignReq = new SupportTicketAssignRequest(engineerA2.getId(),
                                "Engineer unavailable for the remaining SLA window.");

                mockMvc.perform(put("/api/v1/support/tickets/" + ticketId + "/assignment")
                                .header("Authorization", "Bearer " + tokenUserA)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(reassignReq)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.assignedTo.id").value(engineerA2.getId()));

                // 6. Update Priority (PATCH /api/v1/support/tickets/{ticketId}/priority)
                SupportTicketPriorityUpdateRequest priorityReq = new SupportTicketPriorityUpdateRequest(
                                "CRITICAL", "Payroll processing is blocked for multiple employees.");

                mockMvc.perform(patch("/api/v1/support/tickets/" + ticketId + "/priority")
                                .header("Authorization", "Bearer " + tokenUserA)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(priorityReq)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.priority").value("CRITICAL"))
                                .andExpect(jsonPath("$.data.slaHours").value(2));

                // 7. Add Comment (POST /api/v1/support/tickets/{ticketId}/comments)
                SupportTicketCommentRequest commentReq = new SupportTicketCommentRequest(
                                "I have started investigating the payroll access issue.", false);

                mockMvc.perform(post("/api/v1/support/tickets/" + ticketId + "/comments")
                                .header("Authorization", "Bearer " + tokenUserA)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(commentReq)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.data.comment")
                                                .value("I have started investigating the payroll access issue."));

                // Get Comments (GET /api/v1/support/tickets/{ticketId}/comments)
                mockMvc.perform(get("/api/v1/support/tickets/" + ticketId + "/comments")
                                .header("Authorization", "Bearer " + tokenUserA))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))));

                // 8. Add Work Log (POST /api/v1/support/tickets/{ticketId}/work-logs)
                LocalDateTime startLog = LocalDateTime.now().minusHours(2);
                LocalDateTime endLog = LocalDateTime.now().minusMinutes(30);
                SupportWorkLogRequest workLogReq = new SupportWorkLogRequest(startLog, endLog,
                                "Investigated payroll permission synchronization.");

                mockMvc.perform(post("/api/v1/support/tickets/" + ticketId + "/work-logs")
                                .header("Authorization", "Bearer " + tokenEngineerA1)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(workLogReq)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.data.actualHours").value(1.5))
                                .andExpect(jsonPath("$.data.engineerId").value(engineerA1.getId()))
                                .andExpect(jsonPath("$.data.engineerName").value(engineerA1.getFullName()));

                // Get Work Logs (GET /api/v1/support/tickets/{ticketId}/work-logs)
                mockMvc.perform(get("/api/v1/support/tickets/" + ticketId + "/work-logs")
                                .header("Authorization", "Bearer " + tokenUserA))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data", hasSize(1)));

                // 9. Manual Escalation (POST /api/v1/support/tickets/{ticketId}/escalate)
                SupportTicketEscalateRequest escalateReq = new SupportTicketEscalateRequest(
                                "Business-critical payroll issue requires immediate attention.");

                mockMvc.perform(post("/api/v1/support/tickets/" + ticketId + "/escalate")
                                .header("Authorization", "Bearer " + tokenUserA)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(escalateReq)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.isEscalated").value(true))
                                .andExpect(jsonPath("$.data.escalationLevel").value(1));

                // 10. Resolve Ticket (POST /api/v1/support/tickets/{ticketId}/resolve)
                SupportTicketResolveRequest resolveReq = new SupportTicketResolveRequest(
                                "Payroll access restored after permission synchronization.", 2.5);

                mockMvc.perform(post("/api/v1/support/tickets/" + ticketId + "/resolve")
                                .header("Authorization", "Bearer " + tokenUserA)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(resolveReq)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.status").value("RESOLVED"))
                                .andExpect(jsonPath("$.data.actualHours").value(2.5));

                // 11. Close Ticket (POST /api/v1/support/tickets/{ticketId}/close)
                SupportTicketCloseRequest closeReq = new SupportTicketCloseRequest("Issue confirmed resolved.");

                mockMvc.perform(post("/api/v1/support/tickets/" + ticketId + "/close")
                                .header("Authorization", "Bearer " + tokenUserA)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(closeReq)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.status").value("CLOSED"));

                // 12. Check Status History (GET /api/v1/support/tickets/{ticketId}/history)
                mockMvc.perform(get("/api/v1/support/tickets/" + ticketId + "/history")
                                .header("Authorization", "Bearer " + tokenUserA))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(3))))
                                .andExpect(jsonPath("$.data[0].toStatus").value("NEW"));

                // 13. Check Escalation History (GET
                // /api/v1/support/tickets/{ticketId}/escalations)
                mockMvc.perform(get("/api/v1/support/tickets/" + ticketId + "/escalations")
                                .header("Authorization", "Bearer " + tokenUserA))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data", hasSize(1)))
                                .andExpect(jsonPath("$.data[0].level").value(1));
        }

        @Test
        void testManagerReviewRejectAndDuplicateFlows() throws Exception {
                // Create ticket 1
                CreateSupportTicketRequest req1 = new CreateSupportTicketRequest();
                req1.setSubject("Ticket to Reject");
                req1.setDescription("Will be rejected");
                req1.setCategoryId(categoryPayroll.getId());
                req1.setPriority("LOW");

                String res1 = mockMvc.perform(post("/api/v1/support/tickets")
                                .header("Authorization", "Bearer " + tokenUserA)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req1)))
                                .andExpect(status().isCreated())
                                .andReturn().getResponse().getContentAsString();
                Long t1Id = objectMapper.readValue(res1,
                                new TypeReference<ApiResponse<SupportTicketDetailResponse>>() {}).getData().getId();

                // Reject ticket 1
                SupportTicketReviewRequest rejectReq = new SupportTicketReviewRequest();
                rejectReq.setAction("REJECT");
                rejectReq.setReason("Request is outside the scope of employee support.");

                mockMvc.perform(post("/api/v1/support/tickets/" + t1Id + "/review")
                                .header("Authorization", "Bearer " + tokenUserA)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(rejectReq)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.status").value("REJECTED"));

                // Create ticket 2 and ticket 3 to test DUPLICATE
                CreateSupportTicketRequest req2 = new CreateSupportTicketRequest();
                req2.setSubject("Original ticket");
                req2.setDescription("First instance");
                req2.setCategoryId(categoryPayroll.getId());
                String res2 = mockMvc.perform(post("/api/v1/support/tickets")
                                .header("Authorization", "Bearer " + tokenUserA)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req2)))
                                .andExpect(status().isCreated())
                                .andReturn().getResponse().getContentAsString();
                Long t2Id = objectMapper.readValue(res2,
                                new TypeReference<ApiResponse<SupportTicketDetailResponse>>() {}).getData().getId();

                CreateSupportTicketRequest req3 = new CreateSupportTicketRequest();
                req3.setSubject("Duplicate ticket");
                req3.setDescription("Same as first");
                req3.setCategoryId(categoryPayroll.getId());
                String res3 = mockMvc.perform(post("/api/v1/support/tickets")
                                .header("Authorization", "Bearer " + tokenUserA)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req3)))
                                .andExpect(status().isCreated())
                                .andReturn().getResponse().getContentAsString();
                Long t3Id = objectMapper.readValue(res3,
                                new TypeReference<ApiResponse<SupportTicketDetailResponse>>() {}).getData().getId();

                // Mark ticket 3 duplicate of ticket 2
                SupportTicketReviewRequest dupReq = new SupportTicketReviewRequest();
                dupReq.setAction("DUPLICATE");
                dupReq.setDuplicateOfTicketId(t2Id);
                dupReq.setReason("Same payroll access issue already reported.");

                mockMvc.perform(post("/api/v1/support/tickets/" + t3Id + "/review")
                                .header("Authorization", "Bearer " + tokenUserA)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dupReq)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.status").value("DUPLICATE"));
        }

        @Test
        void testSlaAndEscalationConfigurationEndpoints() throws Exception {
                // GET /api/v1/support/sla
                mockMvc.perform(get("/api/v1/support/sla")
                                .header("Authorization", "Bearer " + tokenUserA))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.organizationId").isNotEmpty())
                                .andExpect(jsonPath("$.data.enabled").value(true))
                                .andExpect(jsonPath("$.data.rules", hasSize(4)));

                // PUT /api/v1/support/sla
                SupportSlaConfigDto slaUpdate = new SupportSlaConfigDto(true, List.of(
                                new SupportSlaConfigDto.SlaRuleItem("CRITICAL", 1),
                                new SupportSlaConfigDto.SlaRuleItem("HIGH", 3),
                                new SupportSlaConfigDto.SlaRuleItem("MEDIUM", 6),
                                new SupportSlaConfigDto.SlaRuleItem("LOW", 12)));

                mockMvc.perform(put("/api/v1/support/sla")
                                .header("Authorization", "Bearer " + tokenUserA)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(slaUpdate)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.rules[0].slaHours").value(1));

                // GET /api/v1/support/sla/escalations
                mockMvc.perform(get("/api/v1/support/sla/escalations")
                                .header("Authorization", "Bearer " + tokenUserA))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.rules", hasSize(greaterThanOrEqualTo(1))));

                // PUT /api/v1/support/sla/escalations
                SupportEscalationRulesDto escUpdate = new SupportEscalationRulesDto(List.of(
                                new SupportEscalationRulesDto.EscalationRuleItem(1, 20, "NOTIFY_MANAGER"),
                                new SupportEscalationRulesDto.EscalationRuleItem(2, 50, "NOTIFY_SUPPORT_HEAD"),
                                new SupportEscalationRulesDto.EscalationRuleItem(3, 100, "REASSIGN")));

                mockMvc.perform(put("/api/v1/support/sla/escalations")
                                .header("Authorization", "Bearer " + tokenUserA)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(escUpdate)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.rules", hasSize(3)))
                                .andExpect(jsonPath("$.data.rules[0].triggerAfterMinutes").value(20));
        }

        @Test
        void testSupportDashboardSummaryAndMultiTenantIsolation() throws Exception {
                // Create 2 tickets in Org A
                CreateSupportTicketRequest reqA = new CreateSupportTicketRequest();
                reqA.setSubject("Org A Ticket 1");
                reqA.setDescription("Desc");
                reqA.setCategoryId(categoryPayroll.getId());
                reqA.setPriority("CRITICAL");

                String resA = mockMvc.perform(post("/api/v1/support/tickets")
                                .header("Authorization", "Bearer " + tokenUserA)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(reqA)))
                                .andExpect(status().isCreated())
                                .andReturn().getResponse().getContentAsString();
                Long tAId = objectMapper.readValue(resA,
                                new TypeReference<ApiResponse<SupportTicketDetailResponse>>() {}).getData().getId();

                // Org A Dashboard
                mockMvc.perform(get("/api/v1/support/dashboard")
                                .header("Authorization", "Bearer " + tokenUserA))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.organizationId").isNotEmpty())
                                .andExpect(jsonPath("$.data.total").value(greaterThanOrEqualTo(1)))
                                .andExpect(jsonPath("$.data.critical").value(greaterThanOrEqualTo(1)));

                // Org B Dashboard should be 0 tickets for Org B
                mockMvc.perform(get("/api/v1/support/dashboard")
                                .header("Authorization", "Bearer " + tokenUserB))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.total").value(0));

                // Cross-tenant access: User B cannot access Org A's ticket (404/AccessDenied)
                mockMvc.perform(get("/api/v1/support/tickets/" + tAId)
                                .header("Authorization", "Bearer " + tokenUserB))
                                .andExpect(status().isNotFound());

                // Cross-tenant review: User B cannot review Org A's ticket
                SupportTicketReviewRequest rejectReq = new SupportTicketReviewRequest();
                rejectReq.setAction("REJECT");
                rejectReq.setReason("Malicious cross tenant reject");

                mockMvc.perform(post("/api/v1/support/tickets/" + tAId + "/review")
                                .header("Authorization", "Bearer " + tokenUserB)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(rejectReq)))
                                .andExpect(status().isNotFound());
        }

        @Test
        void testSupportEscalationSchedulerEvaluation() {
                // Create a ticket that is already past its due date
                MySupportTicket overdueTicket = new MySupportTicket();
                overdueTicket.setTicketNumber("ST-TEST-OVERDUE-" + System.currentTimeMillis());
                overdueTicket.setSubject("Overdue Ticket Test");
                overdueTicket.setDescription("Overdue test");
                overdueTicket.setCategory(categoryPayroll);
                overdueTicket.setEmployee(employeeA);
                overdueTicket.setOrganization(orgA);
                overdueTicket.setPriority(SupportTicketPriority.CRITICAL);
                overdueTicket.setStatus(SupportTicketStatus.IN_PROGRESS);
                overdueTicket.setCreatedAt(LocalDateTime.now().minusHours(3));
                overdueTicket.setDueDate(LocalDateTime.now().minusHours(1)); // 60 mins overdue
                overdueTicket.setOverdue(false);
                overdueTicket.setEscalated(false);
                overdueTicket.setEscalationLevel(0);
                overdueTicket = ticketRepository.save(overdueTicket);

                // Run scheduler
                escalationScheduler.evaluateOverdueTicketsAndEscalations();

                // Verify ticket state updated
                MySupportTicket evaluated = ticketRepository.findById(overdueTicket.getId()).orElseThrow();
                assertTrue(evaluated.isOverdue(), "Ticket must be marked as overdue");
                assertTrue(evaluated.isEscalated(), "Ticket must be marked as escalated");
                assertTrue(evaluated.getEscalationLevel() >= 1, "Ticket escalation level should be >= 1");

                // Verify escalation history recorded
                List<SupportEscalationHistory> history = escalationHistoryRepository
                                .findByTicketIdOrderByTriggeredAtAsc(overdueTicket.getId());
                assertFalse(history.isEmpty());
                assertEquals(1, history.get(0).getLevel());

                // Run scheduler again -> idempotency check must ensure duplicate escalation
                // history is NOT created
                int historyCountBefore = history.size();
                escalationScheduler.evaluateOverdueTicketsAndEscalations();
                List<SupportEscalationHistory> historyAfter = escalationHistoryRepository
                                .findByTicketIdOrderByTriggeredAtAsc(overdueTicket.getId());
                assertEquals(historyCountBefore, historyAfter.size(),
                                "Scheduler must be idempotent and not create duplicate escalation history");
        }

        @Test
        void testSupportTicketNegativeValidationAndFilteringRules() throws Exception {
                // 1. Invalid ticket ID (404 Not Found)
                mockMvc.perform(get("/api/v1/support/tickets/999999999")
                                .header("Authorization", "Bearer " + tokenUserA))
                                .andExpect(status().isNotFound());

                // 2. Create ticket with empty subject (400 Bad Request)
                CreateSupportTicketRequest emptySubjReq = new CreateSupportTicketRequest();
                emptySubjReq.setSubject("");
                emptySubjReq.setDescription("Valid description");
                emptySubjReq.setCategoryId(categoryPayroll.getId());
                emptySubjReq.setPriority("HIGH");
                mockMvc.perform(post("/api/v1/support/tickets")
                                .header("Authorization", "Bearer " + tokenUserA)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(emptySubjReq)))
                                .andExpect(status().isBadRequest());

                // 3. Create ticket with invalid priority (400 Bad Request)
                CreateSupportTicketRequest invalidPriorityReq = new CreateSupportTicketRequest();
                invalidPriorityReq.setSubject("Invalid Priority Ticket");
                invalidPriorityReq.setDescription("Testing invalid priority");
                invalidPriorityReq.setCategoryId(categoryPayroll.getId());
                invalidPriorityReq.setPriority("URGENT"); // Unsupported priority
                mockMvc.perform(post("/api/v1/support/tickets")
                                .header("Authorization", "Bearer " + tokenUserA)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidPriorityReq)))
                                .andExpect(status().isBadRequest());

                // 4. Create a valid NEW ticket to test state transition restrictions
                CreateSupportTicketRequest validReq = new CreateSupportTicketRequest();
                validReq.setSubject("State Machine Transition Test");
                validReq.setDescription("Testing invalid transitions");
                validReq.setCategoryId(categoryPayroll.getId());
                validReq.setPriority("HIGH");
                String createdRes = mockMvc.perform(post("/api/v1/support/tickets")
                                .header("Authorization", "Bearer " + tokenUserA)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(validReq)))
                                .andExpect(status().isCreated())
                                .andReturn().getResponse().getContentAsString();
                Long testTicketId = objectMapper.readValue(createdRes,
                                new TypeReference<ApiResponse<SupportTicketDetailResponse>>() {}).getData().getId();

                // 5. Attempt to Resolve NEW ticket (409 Conflict)
                SupportTicketResolveRequest resolveReq = new SupportTicketResolveRequest("Resolved early", 1.0);
                mockMvc.perform(post("/api/v1/support/tickets/" + testTicketId + "/resolve")
                                .header("Authorization", "Bearer " + tokenUserA)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(resolveReq)))
                                .andExpect(status().isConflict());

                // 6. Review ticket with negative estimated hours (400 Bad Request)
                SupportTicketReviewRequest negHoursReq = new SupportTicketReviewRequest();
                negHoursReq.setAction("ACCEPT");
                negHoursReq.setPriority("HIGH");
                negHoursReq.setEstimatedHours(-5.0);
                mockMvc.perform(post("/api/v1/support/tickets/" + testTicketId + "/review")
                                .header("Authorization", "Bearer " + tokenUserA)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(negHoursReq)))
                                .andExpect(status().isBadRequest());

                // 7. Accept ticket and move to IN_PROGRESS
                SupportTicketReviewRequest acceptReq = new SupportTicketReviewRequest();
                acceptReq.setAction("ACCEPT");
                acceptReq.setPriority("HIGH");
                acceptReq.setEstimatedHours(3.0);
                mockMvc.perform(post("/api/v1/support/tickets/" + testTicketId + "/review")
                                .header("Authorization", "Bearer " + tokenUserA)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(acceptReq)))
                                .andExpect(status().isOk());

                SupportTicketAssignRequest assignReq = new SupportTicketAssignRequest(engineerA1.getId(), null);
                mockMvc.perform(put("/api/v1/support/tickets/" + testTicketId + "/assignment")
                                .header("Authorization", "Bearer " + tokenUserA)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(assignReq)))
                                .andExpect(status().isOk());

                // 8. Attempt to Close IN_PROGRESS ticket (409 Conflict)
                SupportTicketCloseRequest closeReq = new SupportTicketCloseRequest("Closing directly from IN_PROGRESS");
                mockMvc.perform(post("/api/v1/support/tickets/" + testTicketId + "/close")
                                .header("Authorization", "Bearer " + tokenUserA)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(closeReq)))
                                .andExpect(status().isConflict());

                // 9. Add work log with invalid time where endedAt <= startedAt (400 Bad Request)
                LocalDateTime now = LocalDateTime.now();
                SupportWorkLogRequest invalidWorkLog = new SupportWorkLogRequest(now, now.minusHours(1), "Invalid timeframe");
                mockMvc.perform(post("/api/v1/support/tickets/" + testTicketId + "/work-logs")
                                .header("Authorization", "Bearer " + tokenEngineerA1)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidWorkLog)))
                                .andExpect(status().isBadRequest());

                // 10. Filter checks
                mockMvc.perform(get("/api/v1/support/tickets?status=IN_PROGRESS")
                                .header("Authorization", "Bearer " + tokenUserA))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.content", hasSize(greaterThanOrEqualTo(1))));

                mockMvc.perform(get("/api/v1/support/tickets?priority=HIGH")
                                .header("Authorization", "Bearer " + tokenUserA))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.content", hasSize(greaterThanOrEqualTo(1))));
        }

        @Test
        void testComprehensiveSupportTicketValidations() throws Exception {
                // ==========================================
                // 1. CREATE TICKET VALIDATIONS
                // ==========================================

                // 1a. Missing subject (null)
                CreateSupportTicketRequest reqNullSubj = new CreateSupportTicketRequest();
                reqNullSubj.setDescription("Valid description");
                reqNullSubj.setCategoryId(categoryPayroll.getId());
                mockMvc.perform(post("/api/v1/support/tickets")
                                .header("Authorization", "Bearer " + tokenUserA)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(reqNullSubj)))
                                .andExpect(status().isBadRequest());

                // 1b. Blank subject
                CreateSupportTicketRequest reqBlankSubj = new CreateSupportTicketRequest();
                reqBlankSubj.setSubject("   ");
                reqBlankSubj.setDescription("Valid description");
                reqBlankSubj.setCategoryId(categoryPayroll.getId());
                mockMvc.perform(post("/api/v1/support/tickets")
                                .header("Authorization", "Bearer " + tokenUserA)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(reqBlankSubj)))
                                .andExpect(status().isBadRequest());

                // 1c. Subject exceeding 250 characters
                CreateSupportTicketRequest reqLongSubj = new CreateSupportTicketRequest();
                reqLongSubj.setSubject("A".repeat(251));
                reqLongSubj.setDescription("Valid description");
                reqLongSubj.setCategoryId(categoryPayroll.getId());
                mockMvc.perform(post("/api/v1/support/tickets")
                                .header("Authorization", "Bearer " + tokenUserA)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(reqLongSubj)))
                                .andExpect(status().isBadRequest());

                // 1d. Missing description (null)
                CreateSupportTicketRequest reqNullDesc = new CreateSupportTicketRequest();
                reqNullDesc.setSubject("Valid subject");
                reqNullDesc.setCategoryId(categoryPayroll.getId());
                mockMvc.perform(post("/api/v1/support/tickets")
                                .header("Authorization", "Bearer " + tokenUserA)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(reqNullDesc)))
                                .andExpect(status().isBadRequest());

                // 1e. Blank description
                CreateSupportTicketRequest reqBlankDesc = new CreateSupportTicketRequest();
                reqBlankDesc.setSubject("Valid subject");
                reqBlankDesc.setDescription("   ");
                reqBlankDesc.setCategoryId(categoryPayroll.getId());
                mockMvc.perform(post("/api/v1/support/tickets")
                                .header("Authorization", "Bearer " + tokenUserA)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(reqBlankDesc)))
                                .andExpect(status().isBadRequest());

                // 1f. Missing categoryId
                CreateSupportTicketRequest reqNullCat = new CreateSupportTicketRequest();
                reqNullCat.setSubject("Valid subject");
                reqNullCat.setDescription("Valid description");
                mockMvc.perform(post("/api/v1/support/tickets")
                                .header("Authorization", "Bearer " + tokenUserA)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(reqNullCat)))
                                .andExpect(status().isBadRequest());

                // 1g. Non-existent categoryId
                CreateSupportTicketRequest reqNonExistCat = new CreateSupportTicketRequest();
                reqNonExistCat.setSubject("Valid subject");
                reqNonExistCat.setDescription("Valid description");
                reqNonExistCat.setCategoryId(999999999L);
                mockMvc.perform(post("/api/v1/support/tickets")
                                .header("Authorization", "Bearer " + tokenUserA)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(reqNonExistCat)))
                                .andExpect(status().isBadRequest());

                // 1h. Category belonging to Org B used by Org A (Cross-tenant category validation)
                CreateSupportTicketRequest reqCrossTenantCat = new CreateSupportTicketRequest();
                reqCrossTenantCat.setSubject("Valid subject");
                reqCrossTenantCat.setDescription("Valid description");
                reqCrossTenantCat.setCategoryId(categoryOrgB.getId());
                mockMvc.perform(post("/api/v1/support/tickets")
                                .header("Authorization", "Bearer " + tokenUserA)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(reqCrossTenantCat)))
                                .andExpect(status().isBadRequest());

                // 1i. Invalid priority enum
                CreateSupportTicketRequest reqInvalidPri = new CreateSupportTicketRequest();
                reqInvalidPri.setSubject("Valid subject");
                reqInvalidPri.setDescription("Valid description");
                reqInvalidPri.setCategoryId(categoryPayroll.getId());
                reqInvalidPri.setPriority("SUPER_URGENT");
                mockMvc.perform(post("/api/v1/support/tickets")
                                .header("Authorization", "Bearer " + tokenUserA)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(reqInvalidPri)))
                                .andExpect(status().isBadRequest());

                // ==========================================
                // 2. REVIEW VALIDATIONS
                // ==========================================
                // Create a valid NEW ticket in Org A
                CreateSupportTicketRequest validTicketReq = new CreateSupportTicketRequest();
                validTicketReq.setSubject("Ticket for Review Validations");
                validTicketReq.setDescription("Testing all review edge cases");
                validTicketReq.setCategoryId(categoryPayroll.getId());
                validTicketReq.setPriority("MEDIUM");
                String createdRes = mockMvc.perform(post("/api/v1/support/tickets")
                                .header("Authorization", "Bearer " + tokenUserA)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(validTicketReq)))
                                .andExpect(status().isCreated())
                                .andReturn().getResponse().getContentAsString();
                Long reviewTicketId = objectMapper.readValue(createdRes,
                                new TypeReference<ApiResponse<SupportTicketDetailResponse>>() {}).getData().getId();

                // Create a valid ticket in Org B for cross-tenant testing
                CreateSupportTicketRequest orgBTicketReq = new CreateSupportTicketRequest();
                orgBTicketReq.setSubject("Org B Ticket");
                orgBTicketReq.setDescription("Org B Ticket Description");
                orgBTicketReq.setCategoryId(categoryOrgB.getId());
                String orgBRes = mockMvc.perform(post("/api/v1/support/tickets")
                                .header("Authorization", "Bearer " + tokenUserB)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(orgBTicketReq)))
                                .andExpect(status().isCreated())
                                .andReturn().getResponse().getContentAsString();
                Long orgBTicketId = objectMapper.readValue(orgBRes,
                                new TypeReference<ApiResponse<SupportTicketDetailResponse>>() {}).getData().getId();

                // 2a. Action is blank
                SupportTicketReviewRequest revBlankAction = new SupportTicketReviewRequest();
                revBlankAction.setAction("   ");
                mockMvc.perform(post("/api/v1/support/tickets/" + reviewTicketId + "/review")
                                .header("Authorization", "Bearer " + tokenUserA)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(revBlankAction)))
                                .andExpect(status().isBadRequest());

                // 2b. Action is invalid
                SupportTicketReviewRequest revInvalidAction = new SupportTicketReviewRequest();
                revInvalidAction.setAction("DISMISS");
                mockMvc.perform(post("/api/v1/support/tickets/" + reviewTicketId + "/review")
                                .header("Authorization", "Bearer " + tokenUserA)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(revInvalidAction)))
                                .andExpect(status().isBadRequest());

                // 2c. ACCEPT with missing priority
                SupportTicketReviewRequest revAcceptNoPri = new SupportTicketReviewRequest();
                revAcceptNoPri.setAction("ACCEPT");
                revAcceptNoPri.setEstimatedHours(4.0);
                mockMvc.perform(post("/api/v1/support/tickets/" + reviewTicketId + "/review")
                                .header("Authorization", "Bearer " + tokenUserA)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(revAcceptNoPri)))
                                .andExpect(status().isBadRequest());

                // 2d. ACCEPT with invalid priority
                SupportTicketReviewRequest revAcceptBadPri = new SupportTicketReviewRequest();
                revAcceptBadPri.setAction("ACCEPT");
                revAcceptBadPri.setPriority("SUPER_CRITICAL");
                revAcceptBadPri.setEstimatedHours(4.0);
                mockMvc.perform(post("/api/v1/support/tickets/" + reviewTicketId + "/review")
                                .header("Authorization", "Bearer " + tokenUserA)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(revAcceptBadPri)))
                                .andExpect(status().isBadRequest());

                // 2e. ACCEPT with null estimatedHours
                SupportTicketReviewRequest revAcceptNullHours = new SupportTicketReviewRequest();
                revAcceptNullHours.setAction("ACCEPT");
                revAcceptNullHours.setPriority("HIGH");
                mockMvc.perform(post("/api/v1/support/tickets/" + reviewTicketId + "/review")
                                .header("Authorization", "Bearer " + tokenUserA)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(revAcceptNullHours)))
                                .andExpect(status().isBadRequest());

                // 2f. ACCEPT with zero / negative estimatedHours
                SupportTicketReviewRequest revAcceptZeroHours = new SupportTicketReviewRequest();
                revAcceptZeroHours.setAction("ACCEPT");
                revAcceptZeroHours.setPriority("HIGH");
                revAcceptZeroHours.setEstimatedHours(0.0);
                mockMvc.perform(post("/api/v1/support/tickets/" + reviewTicketId + "/review")
                                .header("Authorization", "Bearer " + tokenUserA)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(revAcceptZeroHours)))
                                .andExpect(status().isBadRequest());

                // 2g. REJECT with missing / blank reason
                SupportTicketReviewRequest revRejectBlankReason = new SupportTicketReviewRequest();
                revRejectBlankReason.setAction("REJECT");
                revRejectBlankReason.setReason("   ");
                mockMvc.perform(post("/api/v1/support/tickets/" + reviewTicketId + "/review")
                                .header("Authorization", "Bearer " + tokenUserA)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(revRejectBlankReason)))
                                .andExpect(status().isBadRequest());

                // 2h. DUPLICATE with missing duplicateOfTicketId
                SupportTicketReviewRequest revDupNoTarget = new SupportTicketReviewRequest();
                revDupNoTarget.setAction("DUPLICATE");
                revDupNoTarget.setReason("Duplicate issue");
                mockMvc.perform(post("/api/v1/support/tickets/" + reviewTicketId + "/review")
                                .header("Authorization", "Bearer " + tokenUserA)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(revDupNoTarget)))
                                .andExpect(status().isBadRequest());

                // 2i. DUPLICATE referencing itself
                SupportTicketReviewRequest revDupSelf = new SupportTicketReviewRequest();
                revDupSelf.setAction("DUPLICATE");
                revDupSelf.setDuplicateOfTicketId(reviewTicketId);
                revDupSelf.setReason("Duplicate of itself");
                mockMvc.perform(post("/api/v1/support/tickets/" + reviewTicketId + "/review")
                                .header("Authorization", "Bearer " + tokenUserA)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(revDupSelf)))
                                .andExpect(status().isBadRequest());

                // 2j. DUPLICATE referencing cross-tenant ticket from Org B -> 404
                SupportTicketReviewRequest revDupCrossTenant = new SupportTicketReviewRequest();
                revDupCrossTenant.setAction("DUPLICATE");
                revDupCrossTenant.setDuplicateOfTicketId(orgBTicketId);
                revDupCrossTenant.setReason("Duplicate from another org");
                mockMvc.perform(post("/api/v1/support/tickets/" + reviewTicketId + "/review")
                                .header("Authorization", "Bearer " + tokenUserA)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(revDupCrossTenant)))
                                .andExpect(status().isNotFound());

                // ==========================================
                // 3. COMMENT VALIDATIONS
                // ==========================================

                // 3a. Blank comment
                SupportTicketCommentRequest commBlank = new SupportTicketCommentRequest("   ", false);
                mockMvc.perform(post("/api/v1/support/tickets/" + reviewTicketId + "/comments")
                                .header("Authorization", "Bearer " + tokenUserA)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(commBlank)))
                                .andExpect(status().isBadRequest());

                // 3b. Comment on non-existent ticket -> 404
                SupportTicketCommentRequest commValid = new SupportTicketCommentRequest("Legitimate comment", false);
                mockMvc.perform(post("/api/v1/support/tickets/999999999/comments")
                                .header("Authorization", "Bearer " + tokenUserA)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(commValid)))
                                .andExpect(status().isNotFound());

                // 3c. Comment on cross-tenant ticket (User A commenting on Org B's ticket) -> 404
                mockMvc.perform(post("/api/v1/support/tickets/" + orgBTicketId + "/comments")
                                .header("Authorization", "Bearer " + tokenUserA)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(commValid)))
                                .andExpect(status().isNotFound());

                // ==========================================
                // 4. PRIORITY UPDATE VALIDATIONS
                // ==========================================

                // 4a. Blank priority
                SupportTicketPriorityUpdateRequest priBlank = new SupportTicketPriorityUpdateRequest("   ", "Reason");
                mockMvc.perform(patch("/api/v1/support/tickets/" + reviewTicketId + "/priority")
                                .header("Authorization", "Bearer " + tokenUserA)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(priBlank)))
                                .andExpect(status().isBadRequest());

                // 4b. Invalid priority enum
                SupportTicketPriorityUpdateRequest priInvalid = new SupportTicketPriorityUpdateRequest("TOP_PRIORITY", "Reason");
                mockMvc.perform(patch("/api/v1/support/tickets/" + reviewTicketId + "/priority")
                                .header("Authorization", "Bearer " + tokenUserA)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(priInvalid)))
                                .andExpect(status().isBadRequest());

                // 4c. Blank reason
                SupportTicketPriorityUpdateRequest priNoReason = new SupportTicketPriorityUpdateRequest("HIGH", "   ");
                mockMvc.perform(patch("/api/v1/support/tickets/" + reviewTicketId + "/priority")
                                .header("Authorization", "Bearer " + tokenUserA)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(priNoReason)))
                                .andExpect(status().isBadRequest());

                // 4d. Priority update on non-existent ticket -> 404
                SupportTicketPriorityUpdateRequest priValid = new SupportTicketPriorityUpdateRequest("HIGH", "Legitimate adjustment");
                mockMvc.perform(patch("/api/v1/support/tickets/999999999/priority")
                                .header("Authorization", "Bearer " + tokenUserA)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(priValid)))
                                .andExpect(status().isNotFound());

                // 4e. Priority update on cross-tenant ticket (Org B's ticket updated by Org A) -> 404
                mockMvc.perform(patch("/api/v1/support/tickets/" + orgBTicketId + "/priority")
                                .header("Authorization", "Bearer " + tokenUserA)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(priValid)))
                                .andExpect(status().isNotFound());

                // ==========================================
                // 5. ESCALATION VALIDATIONS
                // ==========================================

                // 5a. Blank reason
                SupportTicketEscalateRequest escBlank = new SupportTicketEscalateRequest("   ");
                mockMvc.perform(post("/api/v1/support/tickets/" + reviewTicketId + "/escalate")
                                .header("Authorization", "Bearer " + tokenUserA)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(escBlank)))
                                .andExpect(status().isBadRequest());

                // 5b. Escalate non-existent ticket -> 404
                SupportTicketEscalateRequest escValid = new SupportTicketEscalateRequest("SLA breach imminent");
                mockMvc.perform(post("/api/v1/support/tickets/999999999/escalate")
                                .header("Authorization", "Bearer " + tokenUserA)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(escValid)))
                                .andExpect(status().isNotFound());

                // 5c. Escalate cross-tenant ticket -> 404
                mockMvc.perform(post("/api/v1/support/tickets/" + orgBTicketId + "/escalate")
                                .header("Authorization", "Bearer " + tokenUserA)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(escValid)))
                                .andExpect(status().isNotFound());

                // ==========================================
                // 6. SECURITY / AUTH NEGATIVE VALIDATIONS
                // ==========================================

                // 6a. 401 Unauthorized when no Authorization header
                mockMvc.perform(get("/api/v1/support/tickets"))
                                .andExpect(status().isUnauthorized());

                mockMvc.perform(post("/api/v1/support/tickets")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(validTicketReq)))
                                .andExpect(status().isUnauthorized());

                // 6b. 401 Unauthorized when invalid / malformed token
                mockMvc.perform(get("/api/v1/support/tickets")
                                .header("Authorization", "Bearer invalid-garbage-token"))
                                .andExpect(status().isUnauthorized());
        }
}
