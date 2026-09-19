package com.example.ems.appraisal.controller;

import com.example.ems.appraisal.dto.*;
import com.example.ems.appraisal.entity.AppraisalStatus;
import com.example.ems.appraisal.service.*;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class AppraisalFeaturesIntegrationTest {

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private AppraisalCycleService cycleService;

    @Autowired
    private AppraisalEvaluationService evaluationService;

    @Autowired
    private AppraisalIncrementService incrementService;

    @Autowired
    private AppraisalDashboardService dashboardService;

    @Autowired
    private AppraisalConfigurationExtendedService configExtendedService;

    private Organization testOrg;
    private Employee hrEmployee;
    private Employee regularEmployee;
    private User hrUser;
    private User regularUser;

    @BeforeEach
    void setUp() {
        long ts = System.currentTimeMillis();

        testOrg = new Organization();
        testOrg.setName("Test Enterprise " + ts);
        testOrg.setOrganizationCode("ORG-" + ts);
        testOrg = organizationRepository.save(testOrg);

        TenantContext.setCurrentTenant(testOrg.getId());

        Role hrRole = roleRepository.findByName("PLATFORM_ADMIN")
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setName("PLATFORM_ADMIN");
                    r.setDescription("Platform Admin");
                    r.setOrganization(testOrg);
                    return roleRepository.save(r);
                });


        Role empRole = roleRepository.findByName("EMPLOYEE")
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setName("EMPLOYEE");
                    r.setDescription("Employee");
                    r.setOrganization(testOrg);
                    return roleRepository.save(r);
                });

        hrEmployee = new Employee();
        hrEmployee.setOrganization(testOrg);
        hrEmployee.setFirstName("HR");
        hrEmployee.setLastName("Admin " + ts);
        hrEmployee.setEmail("hr." + ts + "@enterprise.com");
        hrEmployee.setEmployeeId("HR-" + ts);
        hrEmployee.setDepartment("Human Resources");
        hrEmployee.setDesignation("HR Manager");
        hrEmployee.setJoiningDate(LocalDate.now().minusYears(2));
        hrEmployee.setAnnualSalary(BigDecimal.valueOf(1200000.00));
        hrEmployee = employeeRepository.save(hrEmployee);

        hrUser = new User();
        hrUser.setWorkEmail(hrEmployee.getEmail());
        hrUser.setRole(hrRole);
        hrUser.setOrganization(testOrg);
        hrUser.setPassword("password");
        hrUser = userRepository.save(hrUser);

        regularEmployee = new Employee();
        regularEmployee.setOrganization(testOrg);
        regularEmployee.setFirstName("Jane");
        regularEmployee.setLastName("Engineer " + ts);
        regularEmployee.setEmail("jane." + ts + "@enterprise.com");
        regularEmployee.setEmployeeId("EMP-" + ts);
        regularEmployee.setDepartment("Engineering");
        regularEmployee.setDesignation("Software Engineer");
        regularEmployee.setJoiningDate(LocalDate.now().minusMonths(12));
        regularEmployee.setAnnualSalary(BigDecimal.valueOf(800000.00));
        regularEmployee = employeeRepository.save(regularEmployee);

        regularUser = new User();
        regularUser.setWorkEmail(regularEmployee.getEmail());
        regularUser.setRole(empRole);
        regularUser.setOrganization(testOrg);
        regularUser.setPassword("password");
        regularUser = userRepository.save(regularUser);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("Test Full Appraisal Lifecycle: Draft Cycles, Eligibility Preview, Generation, Reviews, Increment, and Dashboards")
    void testFullAppraisalEnhancedLifecycle() {
        TenantContext.setCurrentTenant(testOrg.getId());

        // 0. Configure 3 Review Stages for this organization
        configExtendedService.createReviewStage(new ReviewStageConfigurationDto(1, "Lead Evaluation", "APPRAISAL_REVIEW", true));
        configExtendedService.createReviewStage(new ReviewStageConfigurationDto(2, "Manager Approval", "APPRAISAL_REVIEW", true));
        configExtendedService.createReviewStage(new ReviewStageConfigurationDto(3, "HR Review", "APPRAISAL_REVIEW", true));

        // 1. Create a Draft Cycle
        CreateAppraisalCycleDto createDto = new CreateAppraisalCycleDto();

        createDto.setName("2026 Annual Cycle");
        createDto.setType("ANNUAL");
        createDto.setStartDate(LocalDate.now().minusDays(10));
        createDto.setEndDate(LocalDate.now().plusMonths(2));
        CycleEligibilityCriteriaDto crit1 = new CycleEligibilityCriteriaDto();
        crit1.setMinimumServiceMonths(6);
        crit1.setDepartment("Engineering");
        createDto.setEligibleEmployeeCriteria(crit1);

        AppraisalCycleResponseDto createdCycle = cycleService.createCycle(createDto, hrEmployee);
        assertNotNull(createdCycle);
        assertEquals("DRAFT", createdCycle.getStatus());
        Long cycleId = createdCycle.getId();

        // 2. Update Draft Cycle
        UpdateAppraisalCycleDto updateDto = new UpdateAppraisalCycleDto();
        updateDto.setName("2026 Comprehensive Annual Cycle");
        updateDto.setType("ANNUAL");
        updateDto.setStartDate(LocalDate.now().minusDays(10));
        updateDto.setEndDate(LocalDate.now().plusMonths(2));
        CycleEligibilityCriteriaDto crit2 = new CycleEligibilityCriteriaDto();
        crit2.setMinimumServiceMonths(6);
        updateDto.setEligibleEmployeeCriteria(crit2);

        AppraisalCycleResponseDto updatedCycle = cycleService.updateCycle(cycleId, updateDto);
        assertEquals("2026 Comprehensive Annual Cycle", updatedCycle.getName());

        // 3. Check Eligibility Preview
        CycleEligibilityPreviewResponseDto preview = cycleService.getEligibilityPreview(cycleId);
        assertNotNull(preview);
        assertTrue(preview.getTotalEmployees() >= 2);
        assertTrue(preview.getEligibleCount() >= 1);

        // 4. Activate Cycle (auto-binds snapshot version)
        AppraisalCycleResponseDto activated = cycleService.activateCycle(cycleId);
        assertEquals("OPEN", activated.getStatus());

        // 5. Get Cycle Configuration Snapshot
        AppraisalConfigurationSnapshotDto snapshot = cycleService.getCycleConfiguration(cycleId);
        assertNotNull(snapshot);
        assertNotNull(snapshot.getRatingScale());
        assertFalse(snapshot.getReviewStages().isEmpty());

        // 6. Generate Appraisals for Cycle
        Map<String, Object> genResult = cycleService.generateAppraisalsForCycle(cycleId);
        assertNotNull(genResult);
        assertTrue(((Number) genResult.get("generatedCount")).intValue() >= 1);

        // 7. Check Cycle Generation Status
        CycleGenerationStatusResponseDto genStatus = cycleService.getGenerationStatus(cycleId);
        assertNotNull(genStatus);
        assertTrue(genStatus.getTotalGeneratedAppraisals() >= 1);

        // 8. Employee views appraisals
        List<AppraisalResultResponseDto> myAppraisals = evaluationService.getEmployeeAppraisals(regularEmployee.getId());
        assertFalse(myAppraisals.isEmpty());
        Long appraisalId = myAppraisals.get(0).getAppraisalId();

        // 9. Employee submits Self-Assessment
        SelfAssessmentDto selfDto = new SelfAssessmentDto(4.5, "Strong Java & Architecture", "Delivered core features", "Cloud scaling", null);
        SelfAssessmentDto submittedAssessment = evaluationService.submitSelfAssessment(appraisalId, selfDto, regularEmployee);
        assertEquals(4.5, submittedAssessment.getOverallRating());

        // 10. Check Current Stage helper
        AppraisalCurrentStageResponseDto curStage = evaluationService.getCurrentStage(appraisalId, hrUser);
        assertNotNull(curStage);
        assertTrue(curStage.isCanReview());
        assertEquals(1, curStage.getCurrentStageOrder());

        // 11. Reviewer checks Pending Reviews work queue
        List<AppraisalResultResponseDto> pendingQueue = evaluationService.getPendingReviews(hrUser);
        assertFalse(pendingQueue.isEmpty());

        // 12. Submit Reviews across stages to complete appraisal
        ReviewStageDto review1 = new ReviewStageDto();
        review1.setStageOrder(1);
        review1.setRating(4.5);
        review1.setComments("Excellent delivery");
        review1.setRecommendation("Promote");
        evaluationService.submitReview(appraisalId, review1, hrEmployee, hrUser);

        ReviewStageDto review2 = new ReviewStageDto();
        review2.setStageOrder(2);
        review2.setRating(4.5);
        review2.setComments("Manager approved");
        review2.setRecommendation("Promote");
        evaluationService.submitReview(appraisalId, review2, hrEmployee, hrUser);

        ReviewStageDto review3 = new ReviewStageDto();
        review3.setStageOrder(3);
        review3.setRating(4.8);
        review3.setComments("HR approved");
        review3.setRecommendation("Top Performer");
        evaluationService.submitReview(appraisalId, review3, hrEmployee, hrUser);

        // 13. Publish Appraisal Result
        AppraisalResultResponseDto published = evaluationService.publishAppraisal(appraisalId, hrEmployee);
        assertEquals(AppraisalStatus.PUBLISHED, published.getStatus());

        // 14. Preview Increment Calculation
        IncrementCalculationPreviewDto incPreview = incrementService.calculateIncrementPreview(appraisalId);
        assertNotNull(incPreview);
        assertTrue(incPreview.isEligible());
        assertTrue(incPreview.getSuggestedIncrementPercentage() > 0);
        assertTrue(incPreview.getProposedSalary().compareTo(incPreview.getCurrentSalary()) > 0);

        // 15. Approve Increment Proposal
        ApproveIncrementRequestDto approveIncDto = new ApproveIncrementRequestDto(15.0, 5.0, LocalDate.now().plusMonths(1), "Top performance merit increment");
        AppraisalIncrementResponseDto approvedInc = incrementService.approveIncrement(appraisalId, approveIncDto, hrEmployee);
        assertEquals("APPROVED", approvedInc.getStatus());
        assertEquals(15.0, approvedInc.getIncrementPercentage());

        // 16. Apply Increment to Payroll (Idempotent)
        AppraisalIncrementResponseDto appliedInc1 = incrementService.applyIncrement(appraisalId, new ApplyIncrementRequestDto(), hrEmployee);
        assertEquals("APPLIED", appliedInc1.getStatus());

        // Idempotent re-apply check
        AppraisalIncrementResponseDto appliedInc2 = incrementService.applyIncrement(appraisalId, new ApplyIncrementRequestDto(), hrEmployee);
        assertEquals("APPLIED", appliedInc2.getStatus());

        // 17. Verify Increment History
        List<AppraisalIncrementResponseDto> incHistory = incrementService.getIncrementHistory(regularEmployee.getId());
        assertFalse(incHistory.isEmpty());
        assertEquals("APPLIED", incHistory.get(0).getStatus());

        // 18. Verify Dashboards
        // Organization Dashboard
        AppraisalOrganizationDashboardDto orgDashboard = dashboardService.getOrganizationDashboard();
        assertNotNull(orgDashboard);
        assertTrue(orgDashboard.getTotalCycles() >= 1);
        assertTrue(orgDashboard.getTotalAppraisals() >= 1);
        assertTrue(orgDashboard.getTotalIncrementsApplied() >= 1);

        // Cycle Dashboard
        AppraisalCycleDashboardDto cycleDashboard = dashboardService.getCycleDashboard(cycleId);
        assertNotNull(cycleDashboard);
        assertEquals(cycleId, cycleDashboard.getCycleId());
        assertTrue(cycleDashboard.getPublishedCount() >= 1);

        // Employee Dashboard
        AppraisalEmployeeDashboardDto empDashboard = dashboardService.getEmployeeDashboard(regularEmployee);
        assertNotNull(empDashboard);
        assertEquals(regularEmployee.getId(), empDashboard.getEmployeeId());
        assertEquals("PUBLISHED", empDashboard.getCurrentAppraisalStatus());
    }
}
