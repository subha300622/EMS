package com.example.ems.appraisal.controller;

import com.example.ems.appraisal.dto.*;
import com.example.ems.appraisal.entity.*;
import com.example.ems.appraisal.repository.*;
import com.example.ems.appraisal.service.AppraisalConfigurationService;
import com.example.ems.appraisal.service.AppraisalEvaluationService;
import com.example.ems.appraisal.service.AppraisalRequestService;
import com.example.ems.auth.entity.Role;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.RoleRepository;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.repository.OrganizationRepository;
import com.example.ems.security.context.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class AppraisalLifecycleAuditAndMutationIntegrationTest {

    @Autowired
    private AppraisalConfigurationService configurationService;

    @Autowired
    private AppraisalEvaluationService evaluationService;

    @Autowired
    private AppraisalRequestService requestService;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private AppraisalRepository appraisalRepository;

    @Autowired
    private AppraisalRequestReasonRepository reasonRepository;

    @Autowired
    private AppraisalHistoryRepository historyRepository;

    private Organization org;
    private Employee emp;
    private Employee reviewer;
    private User reviewerUser;
    private AppraisalRequestReason reason;

    @BeforeEach
    public void setup() {
        long ts = System.currentTimeMillis();
        org = new Organization();
        org.setName("Audit & Mutation Org " + ts);
        org.setOrganizationCode("AUDIT-" + ts);
        org = organizationRepository.save(org);

        TenantContext.setCurrentTenant(org.getId());

        Role adminRole = roleRepository.findByName("PLATFORM_ADMIN")
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setName("PLATFORM_ADMIN");
                    r.setOrganization(org);
                    return roleRepository.save(r);
                });

        Role empRole = roleRepository.findByName("EMPLOYEE")
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setName("EMPLOYEE");
                    r.setOrganization(org);
                    return roleRepository.save(r);
                });

        emp = new Employee();
        emp.setOrganization(org);
        emp.setFirstName("Alice");
        emp.setLastName("Appraisee");
        emp.setEmail("alice.audit." + ts + "@test.com");
        emp.setDepartment("Engineering");
        emp.setDesignation("Engineer");
        emp.setJoiningDate(LocalDate.now().minusMonths(24));
        emp = employeeRepository.save(emp);

        reviewer = new Employee();
        reviewer.setOrganization(org);
        reviewer.setFirstName("Bob");
        reviewer.setLastName("Lead");
        reviewer.setEmail("bob.audit." + ts + "@test.com");
        reviewer.setDepartment("Engineering");
        reviewer.setDesignation("Tech Lead");
        reviewer.setJoiningDate(LocalDate.now().minusMonths(36));
        reviewer = employeeRepository.save(reviewer);

        reviewerUser = new User();
        reviewerUser.setWorkEmail(reviewer.getEmail());
        reviewerUser.setRole(adminRole);
        reviewerUser.setOrganization(org);
        reviewerUser = userRepository.save(reviewerUser);

        User empUser = new User();
        empUser.setWorkEmail(emp.getEmail());
        empUser.setRole(empRole);
        empUser.setOrganization(org);
        userRepository.save(empUser);

        reason = new AppraisalRequestReason();
        reason.setOrganization(org);
        reason.setCode("PROMOTION");
        reason.setName("Promotion Review");
        reason.setActive(true);
        reason.setCreatedAt(LocalDateTime.now());
        reason = reasonRepository.save(reason);
    }

    @AfterEach
    public void cleanup() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("MUT-01: Configuration Mutation - Existing appraisals dynamically adhere to updated review stages")
    public void testConfigurationMutationBehavior() {
        TenantContext.setCurrentTenant(org.getId());

        // 1. Initial 5-stage configuration
        SaveAppraisalConfigurationRequest configReq = new SaveAppraisalConfigurationRequest();
        configReq.setInitiationMode(AppraisalInitiationMode.HR_AND_EMPLOYEE);
        configReq.setReviewStages(List.of(
                new ReviewStageConfigurationDto(1, "Lead Review", "APPRAISAL_REVIEW", true),
                new ReviewStageConfigurationDto(2, "Manager Review", "APPRAISAL_REVIEW", true),
                new ReviewStageConfigurationDto(3, "HR Review", "APPRAISAL_REVIEW", true),
                new ReviewStageConfigurationDto(4, "Director Approval", "APPRAISAL_APPROVE", true),
                new ReviewStageConfigurationDto(5, "Finance Review", "APPRAISAL_REVIEW", true)
        ));
        configurationService.saveOrUpdateConfiguration(configReq, reviewer);

        // 2. Create appraisal and submit self-assessment
        Appraisal appraisal = new Appraisal();
        appraisal.setOrganization(org);
        appraisal.setEmployee(emp);
        appraisal.setStatus(AppraisalStatus.CREATED);
        appraisal = appraisalRepository.save(appraisal);

        SelfAssessmentDto saDto = new SelfAssessmentDto(4.0, "Strengths", "Achievements", "Areas", null);
        evaluationService.submitSelfAssessment(appraisal.getId(), saDto, emp);

        Appraisal inStage1 = appraisalRepository.findById(appraisal.getId()).orElseThrow();
        assertEquals(AppraisalStatus.STAGE_REVIEW, inStage1.getStatus());
        assertEquals(1, inStage1.getCurrentStageOrder());

        // 3. Complete Stage 1
        ReviewStageDto review1 = new ReviewStageDto();
        review1.setStageOrder(1);
        review1.setRating(4.5);
        review1.setComments("Stage 1 done");
        evaluationService.submitReview(appraisal.getId(), review1, reviewer, reviewerUser);

        Appraisal inStage2 = appraisalRepository.findById(appraisal.getId()).orElseThrow();
        assertEquals(2, inStage2.getCurrentStageOrder());

        // 4. NOW: Organization modifies policy down to a 2-stage workflow (Stage 1 & Stage 2)
        SaveAppraisalConfigurationRequest updatedConfigReq = new SaveAppraisalConfigurationRequest();
        updatedConfigReq.setInitiationMode(AppraisalInitiationMode.HR_AND_EMPLOYEE);
        updatedConfigReq.setReviewStages(List.of(
                new ReviewStageConfigurationDto(1, "Lead Review", "APPRAISAL_REVIEW", true),
                new ReviewStageConfigurationDto(2, "Final Director Review", "APPRAISAL_APPROVE", true)
        ));
        configurationService.saveOrUpdateConfiguration(updatedConfigReq, reviewer);

        // 5. Submit Stage 2 review under the new 2-stage policy -> Since stage 2 is now the final stage, status should transition to COMPLETED!
        ReviewStageDto review2 = new ReviewStageDto();
        review2.setStageOrder(2);
        review2.setRating(4.5);
        review2.setComments("Final Stage review");
        evaluationService.submitReview(appraisal.getId(), review2, reviewer, reviewerUser);

        Appraisal completedAppraisal = appraisalRepository.findById(appraisal.getId()).orElseThrow();
        assertEquals(AppraisalStatus.COMPLETED, completedAppraisal.getStatus(), "Appraisal should reach COMPLETED after finishing the newly configured final stage");
        assertNotNull(completedAppraisal.getFinalRating());
    }

    @Test
    @DisplayName("AUD-01: Full Lifecycle Audit Trail - Verifies physical appraisal_history records for every event")
    public void testFullLifecycleAuditTrailRecords() {
        TenantContext.setCurrentTenant(org.getId());

        // 1. Configure 2-stage policy
        SaveAppraisalConfigurationRequest configReq = new SaveAppraisalConfigurationRequest();
        configReq.setInitiationMode(AppraisalInitiationMode.HR_AND_EMPLOYEE);
        configReq.setReviewStages(List.of(
                new ReviewStageConfigurationDto(1, "Peer Review", "APPRAISAL_REVIEW", true),
                new ReviewStageConfigurationDto(2, "HR Review", "APPRAISAL_REVIEW", true)
        ));
        configurationService.saveOrUpdateConfiguration(configReq, reviewer);

        // 2. Draft request -> REQUEST_CREATED
        CreateEmployeeAppraisalRequestDto reqDto = new CreateEmployeeAppraisalRequestDto();
        reqDto.setReasonId(reason.getId());
        reqDto.setJustification("Promotion request with audit check");
        AppraisalRequestResponseDto draftReq = requestService.createDraftRequest(reqDto, emp);
        assertNotNull(draftReq.getId());

        // 3. Submit request -> REQUEST_SUBMITTED
        AppraisalRequestResponseDto submittedReq = requestService.submitRequest(draftReq.getId(), emp);
        assertEquals(AppraisalRequestStatus.UNDER_REVIEW, submittedReq.getStatus());

        // 4. Create Appraisal directly from approval / generation -> APPRAISAL_CREATED
        Appraisal appraisal = new Appraisal();
        appraisal.setOrganization(org);
        appraisal.setEmployee(emp);
        appraisal.setStatus(AppraisalStatus.CREATED);
        appraisal = appraisalRepository.save(appraisal);

        // 5. Submit self assessment -> SELF_ASSESSMENT_SUBMITTED
        SelfAssessmentDto saDto = new SelfAssessmentDto(4.2, "Code quality", "Delivered module", "Cloud", null);
        evaluationService.submitSelfAssessment(appraisal.getId(), saDto, emp);

        // 6. Submit Stage 1 Review -> STAGE_REVIEW_SUBMITTED (Stage 1)
        ReviewStageDto r1 = new ReviewStageDto();
        r1.setStageOrder(1);
        r1.setRating(4.5);
        r1.setComments("Great performance in stage 1");
        evaluationService.submitReview(appraisal.getId(), r1, reviewer, reviewerUser);

        // 7. Submit Stage 2 Review -> STAGE_REVIEW_SUBMITTED (Stage 2) -> COMPLETED
        ReviewStageDto r2 = new ReviewStageDto();
        r2.setStageOrder(2);
        r2.setRating(4.5);
        r2.setComments("Final HR Approval in stage 2");
        evaluationService.submitReview(appraisal.getId(), r2, reviewer, reviewerUser);

        // 8. Publish appraisal -> APPRAISAL_PUBLISHED
        AppraisalResultResponseDto publishedResult = evaluationService.publishAppraisal(appraisal.getId(), reviewer);
        assertEquals(AppraisalStatus.PUBLISHED, publishedResult.getStatus());

        // 9. QUERY DATABASE FOR ACTUAL AUDIT RECORDS
        List<AppraisalHistory> auditLogs = historyRepository.findByAppraisalIdOrderByChangedAtAsc(appraisal.getId());
        assertFalse(auditLogs.isEmpty(), "Audit history records must exist in database");

        List<String> recordedEvents = auditLogs.stream()
                .map(AppraisalHistory::getChangeType)
                .toList();

        assertTrue(recordedEvents.contains("SELF_ASSESSMENT_SUBMITTED"), "Must record SELF_ASSESSMENT_SUBMITTED in audit history");
        assertTrue(recordedEvents.contains("STAGE_REVIEW_SUBMITTED"), "Must record STAGE_REVIEW_SUBMITTED in audit history");
        assertTrue(recordedEvents.contains("APPRAISAL_PUBLISHED"), "Must record APPRAISAL_PUBLISHED in audit history");

        // Verify audit log properties
        for (AppraisalHistory history : auditLogs) {
            assertEquals(emp.getId(), history.getEmployee().getId(), "Audit record must match employee");
            assertNotNull(history.getChangedAt(), "Audit timestamp must be present");
            assertNotNull(history.getChangedBy(), "Audit actor must be recorded");
        }
    }
}
