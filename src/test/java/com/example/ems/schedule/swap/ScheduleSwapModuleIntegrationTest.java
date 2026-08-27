package com.example.ems.schedule.swap;

import com.example.ems.approval.entity.ApprovalWorkflowDefinition;
import com.example.ems.approval.entity.ApprovalWorkflowStep;
import com.example.ems.approval.entity.ApproverType;
import com.example.ems.approval.entity.WorkflowType;
import com.example.ems.approval.repository.ApprovalWorkflowDefinitionRepository;
import com.example.ems.auth.entity.Role;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.RoleRepository;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.repository.OrganizationRepository;
import com.example.ems.schedule.entity.Schedule;
import com.example.ems.schedule.entity.ScheduleStatus;
import com.example.ems.schedule.repository.ScheduleRepository;
import com.example.ems.security.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ScheduleSwapModuleIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private ApprovalWorkflowDefinitionRepository workflowDefinitionRepository;

    @Autowired
    private JwtService jwtService;

    private Organization testOrg;
    private Employee emp1;
    private Employee emp2;
    private Employee emp3; // Manager
    private User user1;
    private User user2;
    private User user3;
    private String token1;
    private String token2;
    private String token3;

    private Schedule sch1;
    private Schedule sch5;
    private Schedule sch2;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        testOrg = new Organization();
        testOrg.setName("Swap Test Org");
        testOrg.setOrganizationCode("ORG-SWAP-" + java.util.UUID.randomUUID().toString().substring(0, 8));
        testOrg = organizationRepository.save(testOrg);

        Role superAdminRole = roleRepository.findByName("SUPER_ADMIN")
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setName("SUPER_ADMIN");
                    r.setDescription("Super Admin Role");
                    return roleRepository.save(r);
                });

        // Emp 3 (Manager)
        emp3 = new Employee();
        emp3.setFirstName("Manager");
        emp3.setLastName("Boss");
        emp3.setEmployeeId("EMP-0003");
        emp3.setEmail("manager@swaptest.com");
        emp3.setOrganization(testOrg);
        emp3 = employeeRepository.save(emp3);

        user3 = new User();
        user3.setUserId(emp3.getEmployeeId());
        user3.setWorkEmail(emp3.getEmail());
        user3.setPassword("$2a$10$7Q9b9K1l1l1l1l1l1l1l1u");
        user3.setRole(superAdminRole);
        user3.setOrganization(testOrg);
        user3 = userRepository.save(user3);
        token3 = jwtService.generateAccessToken(user3.getUserId(), user3.getWorkEmail(), user3.getRole().getName());

        // Emp 1 (Requester)
        emp1 = new Employee();
        emp1.setFirstName("John");
        emp1.setLastName("Doe");
        emp1.setEmployeeId("EMP-0001");
        emp1.setEmail("john.doe@swaptest.com");
        emp1.setOrganization(testOrg);
        emp1.setManager(emp3);
        emp1 = employeeRepository.save(emp1);

        user1 = new User();
        user1.setUserId(emp1.getEmployeeId());
        user1.setWorkEmail(emp1.getEmail());
        user1.setPassword("$2a$10$7Q9b9K1l1l1l1l1l1l1l1u");
        user1.setRole(superAdminRole);
        user1.setOrganization(testOrg);
        user1 = userRepository.save(user1);
        token1 = jwtService.generateAccessToken(user1.getUserId(), user1.getWorkEmail(), user1.getRole().getName());

        // Emp 2 (Target Employee)
        emp2 = new Employee();
        emp2.setFirstName("Jane");
        emp2.setLastName("Smith");
        emp2.setEmployeeId("EMP-0002");
        emp2.setEmail("jane.smith@swaptest.com");
        emp2.setOrganization(testOrg);
        emp2.setManager(emp3);
        emp2 = employeeRepository.save(emp2);

        user2 = new User();
        user2.setUserId(emp2.getEmployeeId());
        user2.setWorkEmail(emp2.getEmail());
        user2.setPassword("$2a$10$7Q9b9K1l1l1l1l1l1l1l1u");
        user2.setRole(superAdminRole);
        user2.setOrganization(testOrg);
        user2 = userRepository.save(user2);
        token2 = jwtService.generateAccessToken(user2.getUserId(), user2.getWorkEmail(), user2.getRole().getName());

        // Ensure default SCHEDULE_SWAP workflow definition exists
        workflowDefinitionRepository.findActiveByWorkflowTypeAndOrganization(WorkflowType.SCHEDULE_SWAP, testOrg.getId())
                .orElseGet(() -> {
                    ApprovalWorkflowDefinition def = new ApprovalWorkflowDefinition();
                    def.setWorkflowType(WorkflowType.SCHEDULE_SWAP);
                    def.setName("Schedule Swap Integration Workflow");
                    def.setOrganization(null); // global fallback
                    def.setStatus("ACTIVE");

                    ApprovalWorkflowStep step1 = new ApprovalWorkflowStep();
                    step1.setStepOrder(1);
                    step1.setStepName("Target Employee Consent");
                    step1.setApproverType(ApproverType.TARGET_EMPLOYEE);

                    ApprovalWorkflowStep step2 = new ApprovalWorkflowStep();
                    step2.setStepOrder(2);
                    step2.setStepName("Direct Manager Approval");
                    step2.setApproverType(ApproverType.DIRECT_MANAGER);

                    def.addStep(step1);
                    def.addStep(step2);

                    return workflowDefinitionRepository.save(def);
                });

        // Create Schedules
        sch1 = new Schedule();
        sch1.setScheduleId("SCH-0001");
        sch1.setOrganization(testOrg);
        sch1.setEmployee(emp1);
        sch1.setDate(LocalDate.of(2026, 8, 25));
        sch1.setStartTime(LocalTime.of(9, 0));
        sch1.setEndTime(LocalTime.of(18, 0));
        sch1.setLocation("Main Office");
        sch1.setNotes("John original shift");
        sch1.setStatus(ScheduleStatus.SCHEDULED);
        sch1 = scheduleRepository.save(sch1);

        sch5 = new Schedule();
        sch5.setScheduleId("SCH-0005");
        sch5.setOrganization(testOrg);
        sch5.setEmployee(emp2);
        sch5.setDate(LocalDate.of(2026, 8, 27));
        sch5.setStartTime(LocalTime.of(10, 0));
        sch5.setEndTime(LocalTime.of(19, 0));
        sch5.setLocation("Branch Office");
        sch5.setNotes("Jane original shift");
        sch5.setStatus(ScheduleStatus.SCHEDULED);
        sch5 = scheduleRepository.save(sch5);

        sch2 = new Schedule();
        sch2.setScheduleId("SCH-0002");
        sch2.setOrganization(testOrg);
        sch2.setEmployee(emp1);
        sch2.setDate(LocalDate.of(2026, 8, 28));
        sch2.setStartTime(LocalTime.of(9, 0));
        sch2.setEndTime(LocalTime.of(18, 0));
        sch2.setLocation("Main Office");
        sch2.setStatus(ScheduleStatus.SCHEDULED);
        sch2 = scheduleRepository.save(sch2);
    }

    @Test
    public void testCreateSwapRequest_Success() throws Exception {
        String payload = """
            {
              "sourceScheduleId": "SCH-0001",
              "targetScheduleId": "SCH-0005",
              "reason": "Personal commitment"
            }
            """;

        mockMvc.perform(post("/api/v1/schedule-swap-requests")
                .header("Authorization", "Bearer " + token1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.sourceSchedule.scheduleId", is("SCH-0001")))
                .andExpect(jsonPath("$.data.targetSchedule.scheduleId", is("SCH-0005")))
                .andExpect(jsonPath("$.data.status", is("PENDING_APPROVAL")));
    }

    @Test
    public void testCreateSwapRequest_SameEmployee_Fails() throws Exception {
        String payload = """
            {
              "sourceScheduleId": "SCH-0001",
              "targetScheduleId": "SCH-0002",
              "reason": "Self swap attempt"
            }
            """;

        mockMvc.perform(post("/api/v1/schedule-swap-requests")
                .header("Authorization", "Bearer " + token1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.code", is("SWAP_002")))
                .andExpect(jsonPath("$.error.message", containsString("Source and target schedules belong to the same employee")));
    }

    @Test
    public void testCreateSwapRequest_DuplicateActiveRequest_Fails() throws Exception {
        String payload1 = """
            {
              "sourceScheduleId": "SCH-0001",
              "targetScheduleId": "SCH-0005",
              "reason": "First swap request"
            }
            """;

        mockMvc.perform(post("/api/v1/schedule-swap-requests")
                .header("Authorization", "Bearer " + token1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload1))
                .andExpect(status().isCreated());

        String payload2 = """
            {
              "sourceScheduleId": "SCH-0001",
              "targetScheduleId": "SCH-0005",
              "reason": "Duplicate swap attempt"
            }
            """;

        mockMvc.perform(post("/api/v1/schedule-swap-requests")
                .header("Authorization", "Bearer " + token1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload2))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.code", is("SWAP_003")))
                .andExpect(jsonPath("$.error.message", containsString("An active swap request already exists")));
    }

    @Test
    public void testFullApprovalAndAtomicSwapWorkflow() throws Exception {
        // 1. Employee 1 creates swap request
        String createPayload = """
            {
              "sourceScheduleId": "SCH-0001",
              "targetScheduleId": "SCH-0005",
              "reason": "Need Aug 25 off"
            }
            """;

        String createResp = mockMvc.perform(post("/api/v1/schedule-swap-requests")
                .header("Authorization", "Bearer " + token1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createPayload))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        assertTrue(createResp.contains("SSR-"));

        // 2. Step 1: Target Employee (Emp 2) checks inbox and approves
        String inboxEmp2Resp = mockMvc.perform(get("/api/v1/approvals/inbox?workflowType=SCHEDULE_SWAP&status=PENDING")
                .header("Authorization", "Bearer " + token2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andReturn().getResponse().getContentAsString();

        String taskId1 = inboxEmp2Resp.split("\"approvalTaskId\":\"")[1].split("\"")[0];

        mockMvc.perform(post("/api/v1/approvals/" + taskId1 + "/approve")
                .header("Authorization", "Bearer " + token2)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"comment\":\"I agree to the swap\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is("APPROVED")));

        // 3. Step 2: Direct Manager (Emp 3) checks inbox and approves
        String inboxManagerResp = mockMvc.perform(get("/api/v1/approvals/inbox?workflowType=SCHEDULE_SWAP&status=PENDING")
                .header("Authorization", "Bearer " + token3))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andReturn().getResponse().getContentAsString();

        String taskId2 = inboxManagerResp.split("\"approvalTaskId\":\"")[1].split("\"")[0];

        mockMvc.perform(post("/api/v1/approvals/" + taskId2 + "/approve")
                .header("Authorization", "Bearer " + token3)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"comment\":\"Approved by Manager\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is("APPROVED")));

        // 4. Verify Atomic Swap Execution
        Schedule updatedSch1 = scheduleRepository.findByScheduleIdAndOrganizationId("SCH-0001", testOrg.getId()).orElseThrow();
        Schedule updatedSch5 = scheduleRepository.findByScheduleIdAndOrganizationId("SCH-0005", testOrg.getId()).orElseThrow();

        // Employee IDs must NOT be swapped
        assertEquals(emp1.getId(), updatedSch1.getEmployee().getId());
        assertEquals(emp2.getId(), updatedSch5.getEmployee().getId());

        // Shift details MUST be exchanged
        assertEquals(LocalDate.of(2026, 8, 27), updatedSch1.getDate());
        assertEquals(LocalTime.of(10, 0), updatedSch1.getStartTime());
        assertEquals(LocalTime.of(19, 0), updatedSch1.getEndTime());
        assertEquals("Branch Office", updatedSch1.getLocation());

        assertEquals(LocalDate.of(2026, 8, 25), updatedSch5.getDate());
        assertEquals(LocalTime.of(9, 0), updatedSch5.getStartTime());
        assertEquals(LocalTime.of(18, 0), updatedSch5.getEndTime());
        assertEquals("Main Office", updatedSch5.getLocation());
    }

    @Test
    public void testRejectSwapRequest() throws Exception {
        String createPayload = """
            {
              "sourceScheduleId": "SCH-0001",
              "targetScheduleId": "SCH-0005",
              "reason": "Personal commitment"
            }
            """;

        mockMvc.perform(post("/api/v1/schedule-swap-requests")
                .header("Authorization", "Bearer " + token1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createPayload))
                .andExpect(status().isCreated());

        String inboxEmp2Resp = mockMvc.perform(get("/api/v1/approvals/inbox?workflowType=SCHEDULE_SWAP&status=PENDING")
                .header("Authorization", "Bearer " + token2))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String taskId = inboxEmp2Resp.split("\"approvalTaskId\":\"")[1].split("\"")[0];

        mockMvc.perform(post("/api/v1/approvals/" + taskId + "/reject")
                .header("Authorization", "Bearer " + token2)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"comment\":\"Cannot swap shift on this day\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is("REJECTED")));
    }

    @Test
    public void testCancelSwapRequest() throws Exception {
        String createPayload = """
            {
              "sourceScheduleId": "SCH-0001",
              "targetScheduleId": "SCH-0005",
              "reason": "Personal commitment"
            }
            """;

        String createResp = mockMvc.perform(post("/api/v1/schedule-swap-requests")
                .header("Authorization", "Bearer " + token1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createPayload))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String reqId = createResp.split("\"requestId\":\"")[1].split("\"")[0];

        mockMvc.perform(post("/api/v1/schedule-swap-requests/" + reqId + "/cancel")
                .header("Authorization", "Bearer " + token1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is("CANCELLED")));
    }
}
