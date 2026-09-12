package com.example.ems.appraisal.controller;

import com.example.ems.appraisal.dto.CreateAppraisalCycleDto;
import com.example.ems.appraisal.dto.CreateEmployeeAppraisalRequestDto;
import com.example.ems.appraisal.dto.CreateRequestReasonDto;
import com.example.ems.appraisal.dto.CycleEligibilityCriteriaDto;
import com.example.ems.appraisal.dto.ReasonStatusDto;
import com.example.ems.appraisal.dto.ReviewStageConfigurationDto;
import com.example.ems.appraisal.dto.ReviewStageDto;
import com.example.ems.appraisal.dto.SaveAppraisalConfigurationRequest;
import com.example.ems.appraisal.dto.SelfAssessmentDto;
import com.example.ems.appraisal.entity.*;
import com.example.ems.appraisal.repository.*;
import com.example.ems.approval.dto.ApprovalTaskDto;
import com.example.ems.approval.entity.*;
import com.example.ems.approval.repository.ApprovalWorkflowDefinitionRepository;
import com.example.ems.approval.service.ApprovalWorkflowEngineService;
import com.example.ems.auth.entity.Role;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.RoleRepository;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.repository.OrganizationRepository;
import com.example.ems.security.context.TenantContext;
import com.example.ems.security.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
public class AppraisalFullEndToEndIntegrationTest {

    private MockMvc configMockMvc;
    private MockMvc requestMockMvc;
    private MockMvc cycleMockMvc;
    private MockMvc evalMockMvc;

    @Autowired
    private AppraisalConfigurationController configController;

    @Autowired
    private AppraisalRequestController requestController;

    @Autowired
    private AppraisalCycleController cycleController;

    @Autowired
    private AppraisalEvaluationController evalController;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private AppraisalRequestReasonRepository reasonRepository;

    @Autowired
    private AppraisalRequestRepository requestRepository;

    @Autowired
    private AppraisalRepository appraisalRepository;

    @Autowired
    private ApprovalWorkflowDefinitionRepository workflowDefinitionRepository;

    @Autowired
    private ApprovalWorkflowEngineService workflowEngineService;

    @Autowired
    private JwtService jwtService;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private Organization orgA;
    private Organization orgB;

    private User adminUserA;
    private String adminTokenA;

    private User empUserA;
    private Employee empA;
    private String empTokenA;

    private User leadUserA;
    private Employee leadEmpA;
    private String leadTokenA;

    private User mgrUserA;
    private Employee mgrEmpA;
    private String mgrTokenA;

    private User hrUserA;
    private Employee hrEmpA;
    private String hrTokenA;

    private User dirUserA;
    private Employee dirEmpA;
    private String dirTokenA;

    private User finUserA;
    private Employee finEmpA;
    private String finTokenA;

    private User userOrgB;
    private String tokenOrgB;

    private ApprovalWorkflowDefinition multiStageWorkflow;

    @BeforeEach
    public void setup() {
        configMockMvc = MockMvcBuilders.standaloneSetup(configController).build();
        requestMockMvc = MockMvcBuilders.standaloneSetup(requestController).build();
        cycleMockMvc = MockMvcBuilders.standaloneSetup(cycleController).build();
        evalMockMvc = MockMvcBuilders.standaloneSetup(evalController).build();

        // 1. Setup Tenant Organizations
        long now = System.currentTimeMillis();
        orgA = new Organization();
        orgA.setName("Enterprise Corp A " + now);
        orgA.setOrganizationCode("ORG-A-" + now);
        orgA = organizationRepository.save(orgA);

        orgB = new Organization();
        orgB.setName("Enterprise Corp B " + now);
        orgB.setOrganizationCode("ORG-B-" + now);
        orgB = organizationRepository.save(orgB);

        TenantContext.setCurrentTenant(orgA.getId());

        Role adminRole = roleRepository.findByName("PLATFORM_ADMIN")
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setName("PLATFORM_ADMIN");
                    return roleRepository.save(r);
                });

        Role empRole = roleRepository.findByName("EMPLOYEE")
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setName("EMPLOYEE");
                    return roleRepository.save(r);
                });

        // 2. Setup System Administrator in Org A
        adminUserA = createUser("admin_" + now + "@orgA.com", "ADM-" + now, adminRole, orgA);
        createEmployee("System Admin", adminUserA.getWorkEmail(), "ADMIN-" + now, orgA, null, LocalDate.now().minusYears(3));
        adminTokenA = "Bearer " + jwtService.generateAccessToken(adminUserA.getUserId(), adminUserA.getWorkEmail(), "PLATFORM_ADMIN");

        // 3. Setup Approvers Hierarchy: Team Lead -> Manager -> HR -> Director -> Finance
        leadUserA = createUser("lead_" + now + "@orgA.com", "LEAD-" + now, adminRole, orgA);
        leadEmpA = createEmployee("Team Lead", leadUserA.getWorkEmail(), "EMP-LEAD-" + now, orgA, "Engineering", LocalDate.now().minusYears(2));
        leadTokenA = "Bearer " + jwtService.generateAccessToken(leadUserA.getUserId(), leadUserA.getWorkEmail(), "PLATFORM_ADMIN");

        mgrUserA = createUser("mgr_" + now + "@orgA.com", "MGR-" + now, adminRole, orgA);
        mgrEmpA = createEmployee("Engineering Manager", mgrUserA.getWorkEmail(), "EMP-MGR-" + now, orgA, "Engineering", LocalDate.now().minusYears(3));
        mgrTokenA = "Bearer " + jwtService.generateAccessToken(mgrUserA.getUserId(), mgrUserA.getWorkEmail(), "PLATFORM_ADMIN");

        hrUserA = createUser("hr_" + now + "@orgA.com", "HR-" + now, adminRole, orgA);
        hrEmpA = createEmployee("HR Executive", hrUserA.getWorkEmail(), "EMP-HR-" + now, orgA, "Human Resources", LocalDate.now().minusYears(2));
        hrTokenA = "Bearer " + jwtService.generateAccessToken(hrUserA.getUserId(), hrUserA.getWorkEmail(), "PLATFORM_ADMIN");

        dirUserA = createUser("dir_" + now + "@orgA.com", "DIR-" + now, adminRole, orgA);
        dirEmpA = createEmployee("Managing Director", dirUserA.getWorkEmail(), "EMP-DIR-" + now, orgA, "Executive", LocalDate.now().minusYears(5));
        dirTokenA = "Bearer " + jwtService.generateAccessToken(dirUserA.getUserId(), dirUserA.getWorkEmail(), "PLATFORM_ADMIN");

        finUserA = createUser("fin_" + now + "@orgA.com", "FIN-" + now, adminRole, orgA);
        finEmpA = createEmployee("Finance Officer", finUserA.getWorkEmail(), "EMP-FIN-" + now, orgA, "Finance", LocalDate.now().minusYears(4));
        finTokenA = "Bearer " + jwtService.generateAccessToken(finUserA.getUserId(), finUserA.getWorkEmail(), "PLATFORM_ADMIN");

        // 4. Setup Requester Employee in Org A
        empUserA = createUser("employee_" + now + "@orgA.com", "DEV-" + now, empRole, orgA);
        empA = createEmployee("Senior Developer", empUserA.getWorkEmail(), "EMP-DEV-" + now, orgA, "Engineering", LocalDate.now().minusMonths(12));
        empA.setManager(mgrEmpA);
        empA = employeeRepository.save(empA);
        empTokenA = "Bearer " + jwtService.generateAccessToken(empUserA.getUserId(), empUserA.getWorkEmail(), "EMPLOYEE");

        // 5. Setup Org B User for Cross-Tenant Validation
        userOrgB = createUser("user_" + now + "@orgB.com", "B-USR-" + now, adminRole, orgB);
        createEmployee("Org B User", userOrgB.getWorkEmail(), "B-EMP-" + now, orgB, "Sales", LocalDate.now().minusYears(1));
        tokenOrgB = "Bearer " + jwtService.generateAccessToken(userOrgB.getUserId(), userOrgB.getWorkEmail(), "PLATFORM_ADMIN");

        // 6. Build 5-Stage Approval Workflow: Team Lead -> Manager -> HR -> Director -> Finance
        multiStageWorkflow = new ApprovalWorkflowDefinition();
        multiStageWorkflow.setOrganization(orgA);
        multiStageWorkflow.setName("Appraisal 5-Stage Approval Workflow");
        multiStageWorkflow.setWorkflowType(WorkflowType.APPRAISAL_REQUEST);
        multiStageWorkflow.setStatus("ACTIVE");

        ApprovalWorkflowStep step1 = new ApprovalWorkflowStep();
        step1.setStepOrder(1);
        step1.setStepName("Team Lead Approval");
        step1.setApproverType(ApproverType.SPECIFIC_USER);
        step1.setApproverConfig(String.valueOf(leadEmpA.getId()));
        multiStageWorkflow.addStep(step1);

        ApprovalWorkflowStep step2 = new ApprovalWorkflowStep();
        step2.setStepOrder(2);
        step2.setStepName("Manager Approval");
        step2.setApproverType(ApproverType.SPECIFIC_USER);
        step2.setApproverConfig(String.valueOf(mgrEmpA.getId()));
        multiStageWorkflow.addStep(step2);

        ApprovalWorkflowStep step3 = new ApprovalWorkflowStep();
        step3.setStepOrder(3);
        step3.setStepName("HR Approval");
        step3.setApproverType(ApproverType.SPECIFIC_USER);
        step3.setApproverConfig(String.valueOf(hrEmpA.getId()));
        multiStageWorkflow.addStep(step3);

        ApprovalWorkflowStep step4 = new ApprovalWorkflowStep();
        step4.setStepOrder(4);
        step4.setStepName("Director Approval");
        step4.setApproverType(ApproverType.SPECIFIC_USER);
        step4.setApproverConfig(String.valueOf(dirEmpA.getId()));
        multiStageWorkflow.addStep(step4);

        ApprovalWorkflowStep step5 = new ApprovalWorkflowStep();
        step5.setStepOrder(5);
        step5.setStepName("Finance Approval");
        step5.setApproverType(ApproverType.SPECIFIC_USER);
        step5.setApproverConfig(String.valueOf(finEmpA.getId()));
        multiStageWorkflow.addStep(step5);

        multiStageWorkflow = workflowDefinitionRepository.save(multiStageWorkflow);
    }

    @AfterEach
    public void cleanup() {
        TenantContext.clear();
    }

    private User createUser(String email, String userId, Role role, Organization org) {
        User u = new User();
        u.setWorkEmail(email);
        u.setUserId(userId);
        u.setPassword("password");
        u.setRole(role);
        u.setOrganization(org);
        return userRepository.save(u);
    }

    private Employee createEmployee(String name, String email, String empId, Organization org, String dept, LocalDate joiningDate) {
        Employee e = new Employee();
        e.setFullName(name);
        e.setEmail(email);
        e.setEmployeeId(empId);
        e.setOrganization(org);
        e.setDepartment(dept);
        e.setJoiningDate(joiningDate);
        return employeeRepository.save(e);
    }

    @Test
    @DisplayName("Complete End-To-End Appraisal LifeCycle, Multi-Level Generic Approval Engine Integration, Flow A/B & Tenant Isolation")
    public void testCompleteAppraisalEndToEndSuite() throws Exception {

        // =========================================================================
        // 1. CONFIGURATION & REASONS SETUP (Section 2)
        // =========================================================================
        SaveAppraisalConfigurationRequest configReq = new SaveAppraisalConfigurationRequest();
        configReq.setInitiationMode(AppraisalInitiationMode.HR_AND_EMPLOYEE);
        configReq.setEmployeeRequestEnabled(true);
        configReq.setApprovalWorkflowId(multiStageWorkflow.getId());
        configReq.setMinServiceMonths(6);
        configReq.setMinGapMonths(6);
        configReq.setAllowedRequestReasons(List.of(
                new CreateRequestReasonDto("PROMOTION", "Promotion", "Appraisal for promotion consideration"),
                new CreateRequestReasonDto("SALARY_REVISION", "Salary Revision", "Appraisal for salary revision"),
                new CreateRequestReasonDto("PROBATION_CONFIRMATION", "Probation Confirmation", "Probation confirmation review"),
                new CreateRequestReasonDto("ROLE_CHANGE", "Role Change", "Request due to role change"),
                new CreateRequestReasonDto("EXCEPTIONAL_PERFORMANCE", "Exceptional Performance", "Special performance recognition")
        ));
        configReq.setReviewStages(List.of(
                new ReviewStageConfigurationDto(1, "Team Lead Technical Review", "APPRAISAL_REVIEW", true),
                new ReviewStageConfigurationDto(2, "Manager Operational Review", "APPRAISAL_REVIEW", true),
                new ReviewStageConfigurationDto(3, "HR Compliance Review", "APPRAISAL_REVIEW", true),
                new ReviewStageConfigurationDto(4, "Director Strategic Approval", "APPRAISAL_APPROVE", true),
                new ReviewStageConfigurationDto(5, "Finance Budget Review", "APPRAISAL_REVIEW", true)
        ));

        // Save configuration
        configMockMvc.perform(put("/api/v1/appraisal/configuration")
                        .header("Authorization", adminTokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(configReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.initiationMode").value("HR_AND_EMPLOYEE"))
                .andExpect(jsonPath("$.data.employeeRequestEnabled").value(true))
                .andExpect(jsonPath("$.data.approvalWorkflowId").value(multiStageWorkflow.getId()))
                .andExpect(jsonPath("$.data.allowedRequestReasons", hasSize(5)))
                .andExpect(jsonPath("$.data.reviewStages", hasSize(5)));

        // Retrieve configuration & verify
        configMockMvc.perform(get("/api/v1/appraisal/configuration")
                        .header("Authorization", adminTokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.allowedRequestReasons", hasSize(5)))
                .andExpect(jsonPath("$.data.reviewStages", hasSize(5)));

        // =========================================================================
        // 2. CONFIGURATION VALIDATION (Section 3)
        // =========================================================================
        // Cross-tenant workflow linkage must be forbidden
        ApprovalWorkflowDefinition orgBWorkflow = new ApprovalWorkflowDefinition();
        orgBWorkflow.setOrganization(orgB);
        orgBWorkflow.setName("Org B Workflow");
        orgBWorkflow.setWorkflowType(WorkflowType.APPRAISAL_REQUEST);
        orgBWorkflow = workflowDefinitionRepository.save(orgBWorkflow);

        SaveAppraisalConfigurationRequest crossTenantReq = new SaveAppraisalConfigurationRequest();
        crossTenantReq.setInitiationMode(AppraisalInitiationMode.HR_AND_EMPLOYEE);
        crossTenantReq.setApprovalWorkflowId(orgBWorkflow.getId());

        configMockMvc.perform(put("/api/v1/appraisal/configuration")
                        .header("Authorization", adminTokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(crossTenantReq)))
                .andExpect(status().isForbidden());

        // =========================================================================
        // 3. REQUEST REASONS CRUD & SOFT-DEACTIVATION (Section 4)
        // =========================================================================
        AppraisalRequestReason reasonToDeactivate = reasonRepository
                .findByOrganizationIdAndCodeIgnoreCase(orgA.getId(), "ROLE_CHANGE").orElseThrow();

        // Deactivate reason
        ReasonStatusDto statusDto = new ReasonStatusDto();
        statusDto.setActive(false);
        configMockMvc.perform(patch("/api/v1/appraisal/request-reasons/" + reasonToDeactivate.getId() + "/status")
                        .header("Authorization", adminTokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.active").value(false));

        // Attempting to create request with inactive reason must fail
        CreateEmployeeAppraisalRequestDto invalidReqDto = new CreateEmployeeAppraisalRequestDto();
        invalidReqDto.setReasonId(reasonToDeactivate.getId());
        invalidReqDto.setJustification("Should fail because reason is inactive");

        assertThrows(Exception.class, () -> {
            requestMockMvc.perform(post("/api/v1/appraisal/requests")
                    .header("Authorization", empTokenA)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidReqDto)));
        });

        // =========================================================================
        // 4. FLOW A — EMPLOYEE APPRAISAL REQUEST (Section 5)
        // =========================================================================
        AppraisalRequestReason activeReason = reasonRepository
                .findByOrganizationIdAndCodeIgnoreCase(orgA.getId(), "EXCEPTIONAL_PERFORMANCE").orElseThrow();

        CreateEmployeeAppraisalRequestDto reqDto = new CreateEmployeeAppraisalRequestDto();
        reqDto.setReasonId(activeReason.getId());
        reqDto.setJustification("Delivered scalable high-throughput modules and architectural enhancements.");

        // Step 1: Create Draft Request
        String draftResp = requestMockMvc.perform(post("/api/v1/appraisal/requests")
                        .header("Authorization", empTokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("DRAFT"))
                .andExpect(jsonPath("$.data.employeeId").value(empA.getId()))
                .andReturn().getResponse().getContentAsString();

        Long requestId = objectMapper.readTree(draftResp).get("data").get("id").asLong();

        // Step 2: Submit Request -> Moves to UNDER_REVIEW & Approval Engine starts
        requestMockMvc.perform(post("/api/v1/appraisal/requests/" + requestId + "/submit")
                        .header("Authorization", empTokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("UNDER_REVIEW"));

        AppraisalRequest submittedReq = requestRepository.findById(requestId).orElseThrow();
        assertNotNull(submittedReq.getApprovalInstanceId());

        // =========================================================================
        // 5. MULTI-LEVEL APPROVAL WORKFLOW EXECUTION (Section 6, 7, 8)
        // =========================================================================
        // Negative test: Employee attempts to approve own request -> must be rejected
        assertThrows(Exception.class, () -> {
            workflowEngineService.approveInstanceTask(empUserA, submittedReq.getApprovalInstanceId(), "Self approval attempt");
        });

        // Negative test: Manager attempts Level 1 (Team Lead) approval -> must be rejected
        assertThrows(Exception.class, () -> {
            workflowEngineService.approveInstanceTask(mgrUserA, submittedReq.getApprovalInstanceId(), "Manager skipping Lead");
        });

        // Level 1: Team Lead Approves
        ApprovalTaskDto task1 = workflowEngineService.approveInstanceTask(leadUserA, submittedReq.getApprovalInstanceId(), "Team lead approved performance");
        assertEquals(ApprovalStatus.APPROVED, task1.getStatus());

        // Verify request is STILL UNDER_REVIEW and Appraisal is NOT created yet
        AppraisalRequest afterLead = requestRepository.findById(requestId).orElseThrow();
        assertEquals(AppraisalRequestStatus.UNDER_REVIEW, afterLead.getStatus());
        assertNull(afterLead.getAppraisalId());

        // Level 2: Manager Approves
        ApprovalTaskDto task2 = workflowEngineService.approveInstanceTask(mgrUserA, submittedReq.getApprovalInstanceId(), "Manager endorsed");
        assertEquals(ApprovalStatus.APPROVED, task2.getStatus());

        // Level 3: HR Approves
        ApprovalTaskDto task3 = workflowEngineService.approveInstanceTask(hrUserA, submittedReq.getApprovalInstanceId(), "HR verified eligibility");
        assertEquals(ApprovalStatus.APPROVED, task3.getStatus());

        // Level 4: Director Approves
        ApprovalTaskDto task4 = workflowEngineService.approveInstanceTask(dirUserA, submittedReq.getApprovalInstanceId(), "Director approved promotion consideration");
        assertEquals(ApprovalStatus.APPROVED, task4.getStatus());

        // Level 5: Finance Approves (Final Stage)
        ApprovalTaskDto task5 = workflowEngineService.approveInstanceTask(finUserA, submittedReq.getApprovalInstanceId(), "Finance budget approved");
        assertEquals(ApprovalStatus.APPROVED, task5.getStatus());

        // Verify that upon final approval: Request is APPROVED and Appraisal is automatically CREATED!
        AppraisalRequest finalApprovedReq = requestRepository.findById(requestId).orElseThrow();
        assertEquals(AppraisalRequestStatus.APPRAISAL_CREATED, finalApprovedReq.getStatus());
        assertNotNull(finalApprovedReq.getAppraisalId());

        Long flowAAppraisalId = finalApprovedReq.getAppraisalId();
        Appraisal flowAAppraisal = appraisalRepository.findById(flowAAppraisalId).orElseThrow();
        assertEquals(AppraisalStatus.CREATED, flowAAppraisal.getStatus());
        assertEquals(empA.getId(), flowAAppraisal.getEmployee().getId());

        // =========================================================================
        // 6. EVALUATION, MULTI-STAGE REVIEWS, FINAL RATING & PUBLISHING (Section 12, 13, 14, 15)
        // =========================================================================
        // Negative test: Out-of-order review before self-assessment is submitted -> must fail
        ReviewStageDto earlyReview = new ReviewStageDto();
        earlyReview.setStageOrder(1);
        earlyReview.setRating(4.5);
        assertThrows(Exception.class, () -> {
            evalMockMvc.perform(post("/api/v1/appraisals/" + flowAAppraisalId + "/review")
                    .header("Authorization", leadTokenA)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(earlyReview)));
        });

        // Negative test: Wrong employee attempting self-assessment -> must fail
        SelfAssessmentDto wrongSelfDto = new SelfAssessmentDto();
        wrongSelfDto.setOverallRating(5.0);
        assertThrows(Exception.class, () -> {
            evalMockMvc.perform(post("/api/v1/appraisals/" + flowAAppraisalId + "/self-assessment/submit")
                    .header("Authorization", leadTokenA) // Team Lead trying to submit Developer's self-assessment
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(wrongSelfDto)));
        });

        // Step 1: Employee Submits Self-Assessment
        SelfAssessmentDto selfDto = new SelfAssessmentDto();
        selfDto.setOverallRating(4.6);
        selfDto.setStrengths("Java concurrency, PostgreSQL optimization, Domain-driven architectures");
        selfDto.setAchievements("Delivered Central Approval Engine and Multi-Tenant Module Integrations");
        selfDto.setDevelopmentAreas("Public presentations");

        evalMockMvc.perform(post("/api/v1/appraisals/" + flowAAppraisalId + "/self-assessment/submit")
                        .header("Authorization", empTokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(selfDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.overallRating").value(4.6));

        // Step 2: Reviewer 1 (Team Lead) Submits Stage 1 Review
        ReviewStageDto review1 = new ReviewStageDto();
        review1.setStageOrder(1);
        review1.setStageName("Team Lead Technical Review");
        review1.setRating(4.8);
        review1.setComments("Top-tier architectural contributions and zero regression deliveries.");
        review1.setRecommendation("PROMOTION");

        evalMockMvc.perform(post("/api/v1/appraisals/" + flowAAppraisalId + "/review")
                        .header("Authorization", leadTokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(review1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.rating").value(4.8))
                .andExpect(jsonPath("$.data.recommendation").value("PROMOTION"));

        // Step 3: Reviewer 2 (Manager) Submits Stage 2 Review
        ReviewStageDto review2 = new ReviewStageDto();
        review2.setStageOrder(2);
        review2.setStageName("Manager Operational Review");
        review2.setRating(4.6);
        review2.setComments("Consistently delivers beyond expectations.");
        review2.setRecommendation("SALARY_INCREMENT");

        evalMockMvc.perform(post("/api/v1/appraisals/" + flowAAppraisalId + "/review")
                        .header("Authorization", mgrTokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(review2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.rating").value(4.6));

        // Step 4: Reviewer 3 (HR) Submits Stage 3 Review
        ReviewStageDto review3 = new ReviewStageDto();
        review3.setStageOrder(3);
        review3.setStageName("HR Compliance Review");
        review3.setRating(4.4);
        review3.setComments("Clean compliance record, exemplary peer leadership.");
        review3.setRecommendation("PROMOTION");

        evalMockMvc.perform(post("/api/v1/appraisals/" + flowAAppraisalId + "/review")
                        .header("Authorization", hrTokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(review3)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.rating").value(4.4));

        // Step 5: Reviewer 4 (Director) Submits Stage 4 Review
        ReviewStageDto review4 = new ReviewStageDto();
        review4.setStageOrder(4);
        review4.setStageName("Director Strategic Approval");
        review4.setRating(4.5);
        review4.setComments("Strategic asset to engineering roadmap.");
        review4.setRecommendation("PROMOTION");

        evalMockMvc.perform(post("/api/v1/appraisals/" + flowAAppraisalId + "/review")
                        .header("Authorization", dirTokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(review4)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.rating").value(4.5));

        // Step 6: Reviewer 5 (Finance) Submits Stage 5 Review (Final Configured Stage)
        ReviewStageDto review5 = new ReviewStageDto();
        review5.setStageOrder(5);
        review5.setStageName("Finance Budget Review");
        review5.setRating(4.2);
        review5.setComments("Budget approved for promotion and revision.");
        review5.setRecommendation("SALARY_INCREMENT");

        evalMockMvc.perform(post("/api/v1/appraisals/" + flowAAppraisalId + "/review")
                        .header("Authorization", finTokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(review5)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.rating").value(4.2));

        // Verify Appraisal calculation: Final rating = avg(4.8, 4.6, 4.4, 4.5, 4.2) = 4.5 -> OUTSTANDING
        evalMockMvc.perform(get("/api/v1/appraisals/" + flowAAppraisalId + "/result")
                        .header("Authorization", empTokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.finalRating").value(4.5))
                .andExpect(jsonPath("$.data.performanceCategory").value("OUTSTANDING"))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));

        // Step 7: Publish Appraisal Result
        evalMockMvc.perform(post("/api/v1/appraisals/" + flowAAppraisalId + "/publish")
                        .header("Authorization", adminTokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PUBLISHED"));

        // =========================================================================
        // 7. HISTORY & AUDIT TRAIL LOGGING (Section 16)
        // =========================================================================
        evalMockMvc.perform(get("/api/v1/appraisals/" + flowAAppraisalId + "/history")
                        .header("Authorization", empTokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(3))))
                .andExpect(jsonPath("$.data[?(@.changeType == 'SELF_ASSESSMENT_SUBMITTED')]").exists())
                .andExpect(jsonPath("$.data[?(@.changeType == 'STAGE_REVIEW_SUBMITTED')]").exists())
                .andExpect(jsonPath("$.data[?(@.changeType == 'APPRAISAL_PUBLISHED')]").exists());

        // =========================================================================
        // 8. FLOW B — REGULAR HR APPRAISAL CYCLE (Section 11)
        // =========================================================================
        CreateAppraisalCycleDto cycleDto = new CreateAppraisalCycleDto();
        cycleDto.setName("Org A Annual Performance Cycle 2026");
        cycleDto.setType("ANNUAL");
        cycleDto.setStartDate(LocalDate.of(2026, 1, 1));
        cycleDto.setEndDate(LocalDate.of(2026, 12, 31));
        CycleEligibilityCriteriaDto criteria = new CycleEligibilityCriteriaDto();
        criteria.setMinimumServiceMonths(6);
        criteria.setDepartment("Engineering");
        cycleDto.setEligibleEmployeeCriteria(criteria);

        String createCycleResp = cycleMockMvc.perform(post("/api/v1/appraisals/cycles")
                        .header("Authorization", hrTokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cycleDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("Org A Annual Performance Cycle 2026"))
                .andReturn().getResponse().getContentAsString();

        Long cycleId = objectMapper.readTree(createCycleResp).get("data").get("id").asLong();

        // Activate & Generate Appraisals directly
        cycleMockMvc.perform(post("/api/v1/appraisals/cycles/" + cycleId + "/activate")
                        .header("Authorization", hrTokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("OPEN"));

        cycleMockMvc.perform(post("/api/v1/appraisals/cycles/" + cycleId + "/generate")
                        .header("Authorization", hrTokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.generatedCount").value(greaterThanOrEqualTo(1)));

        // Verify that Flow B created Appraisals directly WITHOUT creating AppraisalRequest
        List<Appraisal> cycleAppraisals = appraisalRepository.findByCycleId(cycleId);
        assertFalse(cycleAppraisals.isEmpty());
        for (Appraisal ca : cycleAppraisals) {
            assertNull(ca.getRequest()); // Direct generation!
            assertEquals(cycleId, ca.getCycle().getId());
        }

        // =========================================================================
        // 9. REJECTION & RESUBMIT LIFECYCLE (Section 9 & 10)
        // =========================================================================
        // 9a. Rejection Test
        AppraisalRequest rejectReq = new AppraisalRequest();
        rejectReq.setOrganization(orgA);
        rejectReq.setEmployee(empA);
        rejectReq.setReason(activeReason);
        rejectReq.setJustification("Request to be rejected");
        rejectReq.setStatus(AppraisalRequestStatus.DRAFT);
        rejectReq = requestRepository.save(rejectReq);

        requestMockMvc.perform(post("/api/v1/appraisal/requests/" + rejectReq.getId() + "/submit")
                        .header("Authorization", empTokenA))
                .andExpect(status().isOk());

        AppraisalRequest submittedRejectReq = requestRepository.findById(rejectReq.getId()).orElseThrow();
        workflowEngineService.rejectInstanceTask(leadUserA, submittedRejectReq.getApprovalInstanceId(), "Budget cap reached");

        AppraisalRequest finalRejected = requestRepository.findById(rejectReq.getId()).orElseThrow();
        assertEquals(AppraisalRequestStatus.REJECTED, finalRejected.getStatus());
        assertNull(finalRejected.getAppraisalId());

        // 9b. Request Information / Resubmit Test
        AppraisalRequest infoReq = new AppraisalRequest();
        infoReq.setOrganization(orgA);
        infoReq.setEmployee(empA);
        infoReq.setReason(activeReason);
        infoReq.setJustification("Initial submission needing more details");
        infoReq.setStatus(AppraisalRequestStatus.DRAFT);
        infoReq = requestRepository.save(infoReq);

        requestMockMvc.perform(post("/api/v1/appraisal/requests/" + infoReq.getId() + "/submit")
                        .header("Authorization", empTokenA))
                .andExpect(status().isOk());

        AppraisalRequest submittedInfoReq = requestRepository.findById(infoReq.getId()).orElseThrow();
        workflowEngineService.requestChangesInstanceTask(leadUserA, submittedInfoReq.getApprovalInstanceId(), "Please attach metrics");

        infoReq.setStatus(AppraisalRequestStatus.MORE_INFORMATION_REQUIRED);
        requestRepository.save(infoReq);

        // Employee resubmits with additional information
        requestMockMvc.perform(post("/api/v1/appraisal/requests/" + infoReq.getId() + "/resubmit")
                        .header("Authorization", empTokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("additionalInformation", "Metrics: 40% latency reduction."))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("UNDER_REVIEW"));

        // =========================================================================
        // 10. MULTI-TENANT SECURITY ISOLATION (Section 17)
        // =========================================================================
        TenantContext.setCurrentTenant(orgB.getId());

        // User from Org B attempting to access Org A configuration or appraisal
        assertThrows(Exception.class, () -> {
            evalMockMvc.perform(get("/api/v1/appraisals/" + flowAAppraisalId)
                    .header("Authorization", tokenOrgB));
        });

        assertThrows(Exception.class, () -> {
            requestMockMvc.perform(get("/api/v1/appraisal/requests/" + requestId)
                    .header("Authorization", tokenOrgB));
        });
    }
}
