package com.example.ems.increment.service;

import com.example.ems.appraisal.entity.Appraisal;
import com.example.ems.appraisal.entity.AppraisalStatus;
import com.example.ems.appraisal.repository.AppraisalRepository;
import com.example.ems.common.exception.BadRequestException;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.increment.dto.CreateRecommendationRequest;
import com.example.ems.increment.dto.EligibilityEvaluationResponse;
import com.example.ems.increment.dto.ImportAppraisalsRequest;
import com.example.ems.increment.dto.ImportAppraisalsResponse;
import com.example.ems.increment.eligibility.IncrementEligibilityService;
import com.example.ems.increment.entity.IncrementCycle;
import com.example.ems.increment.entity.IncrementCycleStatus;
import com.example.ems.increment.entity.IncrementPolicy;
import com.example.ems.increment.repository.IncrementCycleRepository;
import com.example.ems.increment.repository.IncrementRecommendationRepository;
import com.example.ems.organization.entity.Organization;
import com.example.ems.security.context.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import com.example.ems.increment.entity.IncrementRecommendation;

@ExtendWith(MockitoExtension.class)
@DisplayName("Appraisal -> Increment Boundary & Full Validation Chain Tests")
public class AppraisalIncrementBoundaryChainTest {

    @Mock
    private IncrementCycleRepository cycleRepository;

    @Mock
    private AppraisalRepository appraisalRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private IncrementRecommendationRepository recommendationRepository;

    @Mock
    private IncrementEligibilityService eligibilityService;

    @InjectMocks
    private IncrementCycleService cycleService;

    @InjectMocks
    private IncrementRecommendationService recommendationService;

    private Organization organization;
    private IncrementPolicy policy;
    private IncrementCycle cycle;
    private Employee employee;
    private Appraisal completedAppraisal;

    @BeforeEach
    void setUp() {
        TenantContext.setCurrentTenant(1L);

        organization = new Organization();
        organization.setId(1L);

        policy = new IncrementPolicy();
        policy.setId(10L);
        policy.setOrganization(organization);
        policy.setAppraisalRequired(true);
        policy.setName("FY27 Policy");
        policy.setActive(true);
        policy.setMinimumRating(4.00);
        policy.setMinimumGoalAchievementPercentage(80.00);
        policy.setMinimumAttendancePercentage(90.00);
        policy.setMinimumServiceMonths(12);
        policy.setMaximumIncrementPercentage(20.00);
        policy.setMinimumIncrementPercentage(3.00);
        policy.setBudgetLimit(new BigDecimal("1000000.00"));

        cycle = new IncrementCycle();
        cycle.setId(100L);
        cycle.setOrganization(organization);
        cycle.setPolicy(policy);
        cycle.setStatus(IncrementCycleStatus.OPEN);
        cycle.setBudgetLimit(new BigDecimal("1000000.00"));
        cycle.setAllocatedBudget(BigDecimal.ZERO);
        cycle.setEffectiveDate(LocalDate.of(2026, 10, 1));

        employee = new Employee();
        employee.setId(101L);
        employee.setOrganization(organization);
        employee.setFullName("Alice Walker");
        employee.setStatus("ACTIVE");
        employee.setAnnualSalary(new BigDecimal("60000.00"));
        employee.setJoiningDate(LocalDate.of(2024, 1, 1));

        completedAppraisal = new Appraisal();
        completedAppraisal.setId(501L);
        completedAppraisal.setEmployee(employee);
        completedAppraisal.setOrganization(organization);
        completedAppraisal.setStatus(AppraisalStatus.COMPLETED);
        completedAppraisal.setFinalRating(4.5);

        try {
            var f = IncrementRecommendationService.class.getDeclaredField("appraisalResolver");
            f.setAccessible(true);
            f.set(recommendationService, new IncrementAppraisalResolver(appraisalRepository));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    // ── 1. IMPORT BOUNDARY: Non-Completed Appraisals are Filtered Out ──────────

    @Test
    @DisplayName("Import Appraisals skips non-COMPLETED appraisals and evaluates only COMPLETED")
    void testImportAppraisals_FiltersNonCompleted() {
        Appraisal draft = new Appraisal();
        draft.setId(502L);
        draft.setEmployee(employee);
        draft.setStatus(AppraisalStatus.DRAFT);

        Appraisal inReview = new Appraisal();
        inReview.setId(503L);
        inReview.setEmployee(employee);
        inReview.setStatus(AppraisalStatus.STAGE_REVIEW);

        Appraisal cancelled = new Appraisal();
        cancelled.setId(504L);
        cancelled.setEmployee(employee);
        cancelled.setStatus(AppraisalStatus.CANCELLED);

        when(cycleRepository.findByIdAndOrgId(100L, 1L)).thenReturn(Optional.of(cycle));
        when(appraisalRepository.findByCycleId(10L)).thenReturn(Arrays.asList(draft, inReview, cancelled, completedAppraisal));

        EligibilityEvaluationResponse eligibleResp = new EligibilityEvaluationResponse();
        eligibleResp.setEmployeeId(101L);
        eligibleResp.setAppraisalId(501L);
        eligibleResp.setEligible(true);
        when(eligibilityService.evaluateAndRecord(eq(cycle), eq(employee), eq(completedAppraisal), any(), eq(true)))
                .thenReturn(eligibleResp);

        ImportAppraisalsRequest req = new ImportAppraisalsRequest();
        req.setAppraisalCycleId(10L);

        ImportAppraisalsResponse res = cycleService.importCompletedAppraisals(100L, req);

        assertNotNull(res);
        assertEquals(1, res.getTotalEvaluated(), "Only 1 completed appraisal must be evaluated");
        assertEquals(1, res.getEligibleCount());
        assertEquals(0, res.getIneligibleCount());
        verify(eligibilityService, times(1)).evaluateAndRecord(any(), any(), eq(completedAppraisal), any(), anyBoolean());
    }

    // ── 2. RECOMMENDATION CREATION: Direct Rejection of Non-Completed Appraisal ─

    @ParameterizedTest(name = "Reject recommendation for appraisal status: {0}")
    @EnumSource(value = AppraisalStatus.class, names = {"DRAFT", "STAGE_REVIEW", "FINAL_REVIEW", "CANCELLED", "SELF_ASSESSMENT", "SUBMITTED", "UNDER_REVIEW"})
    void testCreateRecommendation_RejectsNonCompletedAppraisal(AppraisalStatus nonCompletedStatus) {
        Appraisal nonCompletedAppraisal = new Appraisal();
        nonCompletedAppraisal.setId(601L);
        nonCompletedAppraisal.setEmployee(employee);
        nonCompletedAppraisal.setOrganization(organization);
        nonCompletedAppraisal.setStatus(nonCompletedStatus);

        when(cycleRepository.findByIdAndOrgId(100L, 1L)).thenReturn(Optional.of(cycle));
        when(employeeRepository.findById(101L)).thenReturn(Optional.of(employee));
        when(appraisalRepository.findById(601L)).thenReturn(Optional.of(nonCompletedAppraisal));

        CreateRecommendationRequest req = new CreateRecommendationRequest();
        req.setCycleId(100L);
        req.setEmployeeId(101L);
        req.setAppraisalId(601L);
        req.setIncrementPercentage(10.0);

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                recommendationService.createRecommendation(req));

        assertTrue(ex.getMessage().contains("finalized appraisals"), "Expected error regarding finalized appraisals");
        verify(recommendationRepository, never()).save(any());
    }

    // ── 3. FULL VALIDATION CHAIN: Ineligibility Blocks Recommendation Creation ─

    @Test
    @DisplayName("Chain Failure: Ineligible appraisal blocks increment recommendation creation")
    void testChainFailure_IneligibleBlocksRecommendation() {
        when(cycleRepository.findByIdAndOrgId(100L, 1L)).thenReturn(Optional.of(cycle));
        when(employeeRepository.findById(101L)).thenReturn(Optional.of(employee));
        when(appraisalRepository.findById(501L)).thenReturn(Optional.of(completedAppraisal));
        when(recommendationRepository.findActiveRecommendation(100L, 101L, 501L)).thenReturn(Optional.empty());

        // Eligibility fails on Rating rule
        EligibilityEvaluationResponse ineligibleResp = new EligibilityEvaluationResponse();
        ineligibleResp.setEmployeeId(101L);
        ineligibleResp.setAppraisalId(501L);
        ineligibleResp.setEligible(false);
        ineligibleResp.setReasons(Collections.singletonList(
                new EligibilityEvaluationResponse.IneligibleReasonDto("MINIMUM_RATING", 4.0, 3.2, "Rating below required minimum")
        ));
        when(eligibilityService.evaluateAndRecord(eq(cycle), eq(employee), eq(completedAppraisal), eq(10.0), eq(true)))
                .thenReturn(ineligibleResp);

        CreateRecommendationRequest req = new CreateRecommendationRequest();
        req.setCycleId(100L);
        req.setEmployeeId(101L);
        req.setAppraisalId(501L);
        req.setIncrementPercentage(10.0);

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                recommendationService.createRecommendation(req));

        assertTrue(ex.getMessage().contains("Cannot create increment recommendation"));
        assertTrue(ex.getMessage().contains("Rating below required minimum"));
        verify(recommendationRepository, never()).save(any());
    }

    // ── 4. FULL VALIDATION CHAIN: All Criteria Pass -> Recommendation Created ──

    @Test
    @DisplayName("Chain Success: All criteria pass -> Recommendation successfully saved")
    void testChainSuccess_AllPassCreatesRecommendation() {
        when(cycleRepository.findByIdAndOrgId(100L, 1L)).thenReturn(Optional.of(cycle));
        when(employeeRepository.findById(101L)).thenReturn(Optional.of(employee));
        when(appraisalRepository.findById(501L)).thenReturn(Optional.of(completedAppraisal));
        when(recommendationRepository.findActiveRecommendation(100L, 101L, 501L)).thenReturn(Optional.empty());

        EligibilityEvaluationResponse eligibleResp = new EligibilityEvaluationResponse();
        eligibleResp.setEmployeeId(101L);
        eligibleResp.setAppraisalId(501L);
        eligibleResp.setEligible(true);
        when(eligibilityService.evaluateAndRecord(eq(cycle), eq(employee), eq(completedAppraisal), eq(10.0), eq(true)))
                .thenReturn(eligibleResp);

        when(recommendationRepository.save(any())).thenAnswer(i -> {
            var rec = (IncrementRecommendation) i.getArgument(0);
            rec.setId(999L);
            return rec;
        });

        CreateRecommendationRequest req = new CreateRecommendationRequest();
        req.setCycleId(100L);
        req.setEmployeeId(101L);
        req.setAppraisalId(501L);
        req.setIncrementPercentage(10.0);

        var resp = recommendationService.createRecommendation(req);

        assertNotNull(resp);
        assertEquals(999L, resp.getId());
        assertEquals(new BigDecimal("6000.00"), resp.getIncrementAmount()); // 10% of 60,000 = 6,000
        assertEquals(new BigDecimal("66000.00"), resp.getRecommendedSalary()); // 60,000 + 6,000 = 66,000
        verify(recommendationRepository, times(1)).save(any());
    }
}
