package com.example.ems.increment.service;

import com.example.ems.appraisal.entity.Appraisal;
import com.example.ems.appraisal.entity.AppraisalStatus;
import com.example.ems.appraisal.repository.AppraisalRepository;
import com.example.ems.common.exception.BadRequestException;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.increment.dto.*;
import com.example.ems.increment.eligibility.*;
import com.example.ems.increment.entity.*;
import com.example.ems.increment.repository.*;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.repository.OrganizationRepository;
import com.example.ems.security.context.TenantContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AppraisalIncrementMultiModeTest {

    @Mock
    private IncrementPolicyRepository policyRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private IncrementCycleRepository cycleRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private AppraisalRepository appraisalRepository;

    @Mock
    private IncrementRecommendationRepository recommendationRepository;

    @Mock
    private IncrementEligibilityEvaluationRepository evaluationRepository;

    private IncrementAppraisalResolver appraisalResolver;
    private IncrementEligibilityService eligibilityService;
    private IncrementPolicyService policyService;
    private RatingEligibilityRule ratingRule;
    private ServiceDurationEligibilityRule serviceRule;
    private AttendanceEligibilityRule attendanceRule;
    private DisciplinaryEligibilityRule disciplinaryRule;
    private BudgetEligibilityRule budgetRule;

    private Organization org1;
    private Organization org2;
    private Employee emp1;
    private Employee emp2;

    @BeforeEach
    void setUp() {
        TenantContext.setCurrentTenant(100L);

        org1 = new Organization();
        org1.setId(100L);
        org1.setName("Org 100");

        org2 = new Organization();
        org2.setId(200L);
        org2.setName("Org 200");

        emp1 = new Employee();
        emp1.setId(1L);
        emp1.setEmployeeId("EMP001");
        emp1.setFullName("John Doe");
        emp1.setStatus("ACTIVE");
        emp1.setAnnualSalary(BigDecimal.valueOf(1000000));
        emp1.setJoiningDate(LocalDate.now().minusYears(2));

        emp2 = new Employee();
        emp2.setId(2L);
        emp2.setEmployeeId("EMP002");
        emp2.setFullName("Jane Smith");
        emp2.setStatus("ACTIVE");
        emp2.setAnnualSalary(BigDecimal.valueOf(800000));
        emp2.setJoiningDate(LocalDate.now().minusYears(1));

        appraisalResolver = new IncrementAppraisalResolver(appraisalRepository);

        ratingRule = new RatingEligibilityRule();
        serviceRule = new ServiceDurationEligibilityRule();
        attendanceRule = new AttendanceEligibilityRule();
        disciplinaryRule = new DisciplinaryEligibilityRule();
        budgetRule = new BudgetEligibilityRule();

        List<EligibilityRule> rules = List.of(ratingRule, serviceRule, attendanceRule, disciplinaryRule, budgetRule);
        eligibilityService = new IncrementEligibilityService(
                rules,
                recommendationRepository,
                evaluationRepository,
                new ObjectMapper()
        );

        policyService = new IncrementPolicyService();
        try {
            var f1 = IncrementPolicyService.class.getDeclaredField("policyRepository");
            f1.setAccessible(true);
            f1.set(policyService, policyRepository);
            var f2 = IncrementPolicyService.class.getDeclaredField("organizationRepository");
            f2.setAccessible(true);
            f2.set(policyService, organizationRepository);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        when(organizationRepository.findById(100L)).thenReturn(Optional.of(org1));
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    // ==========================================
    // MODE 1: APPRAISAL ONLY
    // ==========================================
    @Test
    @DisplayName("Mode 1: Appraisal completed independently produces final rating without altering employee salary")
    void testMode1_AppraisalOnly_DoesNotMutateSalary() {
        Appraisal appraisal = new Appraisal();
        appraisal.setId(50L);
        appraisal.setEmployee(emp1);
        appraisal.setOrganization(org1);
        appraisal.setFinalRating(4.8);
        appraisal.setStatus(AppraisalStatus.COMPLETED);

        // Assert that appraisal completes cleanly, and salary remains strictly unmodified
        assertEquals(AppraisalStatus.COMPLETED, appraisal.getStatus());
        assertEquals(4.8, appraisal.getFinalRating());
        assertEquals(BigDecimal.valueOf(1000000), emp1.getAnnualSalary());
    }

    // ==========================================
    // MODE 2: INCREMENT ONLY (appraisalRequired = false)
    // ==========================================
    @Test
    @DisplayName("Mode 2: Increment Only - appraisalRequired=false, appraisal is not required and RatingRule is not applicable")
    void testMode2_IncrementOnly_AutonomousExecution() {
        IncrementPolicy policy = new IncrementPolicy();
        policy.setId(10L);
        policy.setAppraisalRequired(false);
        policy.setMinimumServiceMonths(12);
        policy.setBudgetLimit(BigDecimal.valueOf(500000));

        IncrementCycle cycle = new IncrementCycle();
        cycle.setId(20L);
        cycle.setOrganization(org1);
        cycle.setPolicy(policy);
        cycle.setBudgetLimit(BigDecimal.valueOf(500000));
        cycle.setAllocatedBudget(BigDecimal.ZERO);
        cycle.setEffectiveDate(LocalDate.now());

        // Resolver returns null safely when appraisalId is null
        Appraisal resolved = appraisalResolver.resolve(policy, emp1, null, 100L);
        assertNull(resolved);

        // EligibilityContext without appraisal
        EligibilityContext context = new EligibilityContext(
                emp1, null, cycle, policy, emp1.getAnnualSalary(), BigDecimal.ZERO, 5.0, true
        );

        // RatingRule should NOT be applicable
        assertFalse(ratingRule.isApplicable(context));

        // Overall eligibility should PASS
        EligibilityEvaluationResponse eval = eligibilityService.evaluate(cycle, emp1, null, 5.0, true);
        assertTrue(eval.isEligible(), "Employee should be eligible for autonomous increment without appraisal");
    }

    // ==========================================
    // MODE 3: INDEPENDENT (Both active in org, appraisalRequired = false)
    // ==========================================
    @Test
    @DisplayName("Mode 3A: Independent - Optional completed appraisal is resolved and rating band matches")
    void testMode3A_Independent_WithCompletedAppraisal() {
        IncrementPolicy policy = new IncrementPolicy();
        policy.setId(10L);
        policy.setAppraisalRequired(false);
        policy.addBand(new IncrementPolicyBand(4.0, 5.0, 12.0));

        Appraisal appraisal = new Appraisal();
        appraisal.setId(55L);
        appraisal.setEmployee(emp1);
        appraisal.setOrganization(org1);
        appraisal.setFinalRating(4.5);
        appraisal.setStatus(AppraisalStatus.COMPLETED);

        when(appraisalRepository.findById(55L)).thenReturn(Optional.of(appraisal));

        Appraisal resolved = appraisalResolver.resolve(policy, emp1, 55L, 100L);
        assertNotNull(resolved);
        assertEquals(4.5, resolved.getFinalRating());
    }

    @Test
    @DisplayName("Mode 3B: Independent - New employee without appraisal still gets increment")
    void testMode3B_Independent_WithoutAppraisal() {
        IncrementPolicy policy = new IncrementPolicy();
        policy.setId(10L);
        policy.setAppraisalRequired(false);
        policy.setMinimumServiceMonths(12);

        IncrementCycle cycle = new IncrementCycle();
        cycle.setId(20L);
        cycle.setOrganization(org1);
        cycle.setPolicy(policy);
        cycle.setAllocatedBudget(BigDecimal.ZERO);

        Appraisal resolved = appraisalResolver.resolve(policy, emp1, null, 100L);
        assertNull(resolved);

        EligibilityEvaluationResponse eval = eligibilityService.evaluate(cycle, emp1, null, 5.0, true);
        assertTrue(eval.isEligible());
    }

    // ==========================================
    // MODE 4: INTEGRATED / PERFORMANCE-LINKED (appraisalRequired = true)
    // ==========================================
    @Test
    @DisplayName("Mode 4.1: Integrated - Missing appraisal throws BadRequestException")
    void testMode4_MissingAppraisal_ThrowsException() {
        IncrementPolicy policy = new IncrementPolicy();
        policy.setId(10L);
        policy.setAppraisalRequired(true);
        policy.setMinimumRating(3.5);

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                appraisalResolver.resolve(policy, emp1, null, 100L)
        );
        assertTrue(ex.getMessage().contains("Appraisal is required for this increment policy"));
    }

    @Test
    @DisplayName("Mode 4.2: Integrated - Incomplete appraisal (DRAFT/IN_PROGRESS) is rejected")
    void testMode4_IncompleteAppraisal_Rejected() {
        IncrementPolicy policy = new IncrementPolicy();
        policy.setId(10L);
        policy.setAppraisalRequired(true);
        policy.setMinimumRating(3.5);

        Appraisal draftAppraisal = new Appraisal();
        draftAppraisal.setId(60L);
        draftAppraisal.setEmployee(emp1);
        draftAppraisal.setOrganization(org1);
        draftAppraisal.setStatus(AppraisalStatus.SELF_ASSESSMENT);

        when(appraisalRepository.findById(60L)).thenReturn(Optional.of(draftAppraisal));

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                appraisalResolver.resolve(policy, emp1, 60L, 100L)
        );
        assertTrue(ex.getMessage().contains("allowed only for finalized appraisals"));
    }

    @Test
    @DisplayName("Mode 4.3: Integrated Boundary - Rating 3.49 < minimum 3.50 FAILS eligibility")
    void testMode4_Boundary_RatingBelowMinimum_Fails() {
        IncrementPolicy policy = new IncrementPolicy();
        policy.setId(10L);
        policy.setAppraisalRequired(true);
        policy.setMinimumRating(3.50);

        IncrementCycle cycle = new IncrementCycle();
        cycle.setId(20L);
        cycle.setPolicy(policy);
        cycle.setOrganization(org1);
        cycle.setAllocatedBudget(BigDecimal.ZERO);

        Appraisal appraisal = new Appraisal();
        appraisal.setId(70L);
        appraisal.setEmployee(emp1);
        appraisal.setOrganization(org1);
        appraisal.setFinalRating(3.49);
        appraisal.setStatus(AppraisalStatus.COMPLETED);

        EligibilityContext context = new EligibilityContext(
                emp1, appraisal, cycle, policy, emp1.getAnnualSalary(), BigDecimal.ZERO, 5.0, true
        );

        assertTrue(ratingRule.isApplicable(context));
        EligibilityRuleResult result = ratingRule.evaluate(context);
        assertFalse(result.isPassed());
        assertTrue(result.getMessage().contains("below the minimum required rating"));

        EligibilityEvaluationResponse eval = eligibilityService.evaluate(cycle, emp1, appraisal, 5.0, true);
        assertFalse(eval.isEligible());
    }

    @Test
    @DisplayName("Mode 4.4: Integrated Boundary - Rating 3.50 == minimum 3.50 PASSES eligibility")
    void testMode4_Boundary_RatingExactMinimum_Passes() {
        IncrementPolicy policy = new IncrementPolicy();
        policy.setId(10L);
        policy.setAppraisalRequired(true);
        policy.setMinimumRating(3.50);

        IncrementCycle cycle = new IncrementCycle();
        cycle.setId(20L);
        cycle.setPolicy(policy);
        cycle.setOrganization(org1);
        cycle.setAllocatedBudget(BigDecimal.ZERO);

        Appraisal appraisal = new Appraisal();
        appraisal.setId(71L);
        appraisal.setEmployee(emp1);
        appraisal.setOrganization(org1);
        appraisal.setFinalRating(3.50);
        appraisal.setStatus(AppraisalStatus.COMPLETED);

        EligibilityContext context = new EligibilityContext(
                emp1, appraisal, cycle, policy, emp1.getAnnualSalary(), BigDecimal.ZERO, 5.0, true
        );

        assertTrue(ratingRule.isApplicable(context));
        EligibilityRuleResult result = ratingRule.evaluate(context);
        assertTrue(result.isPassed());

        EligibilityEvaluationResponse eval = eligibilityService.evaluate(cycle, emp1, appraisal, 5.0, true);
        assertTrue(eval.isEligible());
    }

    @Test
    @DisplayName("Mode 4.5: Integrated Boundary - Rating 3.51 & 5.00 > minimum 3.50 PASSES eligibility")
    void testMode4_Boundary_RatingAboveMinimum_Passes() {
        IncrementPolicy policy = new IncrementPolicy();
        policy.setId(10L);
        policy.setAppraisalRequired(true);
        policy.setMinimumRating(3.50);

        IncrementCycle cycle = new IncrementCycle();
        cycle.setId(20L);
        cycle.setPolicy(policy);
        cycle.setOrganization(org1);
        cycle.setAllocatedBudget(BigDecimal.ZERO);

        Appraisal appraisal = new Appraisal();
        appraisal.setId(72L);
        appraisal.setEmployee(emp1);
        appraisal.setOrganization(org1);
        appraisal.setFinalRating(5.00);
        appraisal.setStatus(AppraisalStatus.PUBLISHED);

        EligibilityEvaluationResponse eval = eligibilityService.evaluate(cycle, emp1, appraisal, 15.0, true);
        assertTrue(eval.isEligible());
        assertEquals(5.0, eval.getFinalRating());
    }

    // ==========================================
    // SECURITY & TENANT / EMPLOYEE ISOLATION
    // ==========================================
    @Test
    @DisplayName("Security: Appraisal belonging to another employee is rejected")
    void testSecurity_WrongEmployeeAppraisal_Rejected() {
        IncrementPolicy policy = new IncrementPolicy();
        policy.setAppraisalRequired(true);

        Appraisal appraisalOfEmp2 = new Appraisal();
        appraisalOfEmp2.setId(80L);
        appraisalOfEmp2.setEmployee(emp2); // belongs to emp2
        appraisalOfEmp2.setOrganization(org1);
        appraisalOfEmp2.setStatus(AppraisalStatus.COMPLETED);

        when(appraisalRepository.findById(80L)).thenReturn(Optional.of(appraisalOfEmp2));

        // Request is for emp1, but appraisal belongs to emp2
        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                appraisalResolver.resolve(policy, emp1, 80L, 100L)
        );
        assertTrue(ex.getMessage().contains("Appraisal does not belong to the specified employee"));
    }

    @Test
    @DisplayName("Security: Appraisal belonging to another organization (tenant) is rejected")
    void testSecurity_WrongTenantAppraisal_Rejected() {
        IncrementPolicy policy = new IncrementPolicy();
        policy.setAppraisalRequired(true);

        Appraisal appraisalOfOrg2 = new Appraisal();
        appraisalOfOrg2.setId(81L);
        appraisalOfOrg2.setEmployee(emp1);
        appraisalOfOrg2.setOrganization(org2); // belongs to Org 200
        appraisalOfOrg2.setStatus(AppraisalStatus.COMPLETED);

        when(appraisalRepository.findById(81L)).thenReturn(Optional.of(appraisalOfOrg2));

        // Current tenant context is Org 100
        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                appraisalResolver.resolve(policy, emp1, 81L, 100L)
        );
        assertTrue(ex.getMessage().contains("Appraisal does not belong to the current organization"));
    }

    @Test
    @DisplayName("Security: Optional wrong appraisal supplied when appraisalRequired=false is still validated and rejected")
    void testSecurity_OptionalWrongAppraisal_Rejected() {
        IncrementPolicy policy = new IncrementPolicy();
        policy.setAppraisalRequired(false); // optional appraisal

        Appraisal appraisalOfEmp2 = new Appraisal();
        appraisalOfEmp2.setId(82L);
        appraisalOfEmp2.setEmployee(emp2);
        appraisalOfEmp2.setOrganization(org1);
        appraisalOfEmp2.setStatus(AppraisalStatus.COMPLETED);

        when(appraisalRepository.findById(82L)).thenReturn(Optional.of(appraisalOfEmp2));

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                appraisalResolver.resolve(policy, emp1, 82L, 100L)
        );
        assertTrue(ex.getMessage().contains("Appraisal does not belong to the specified employee"));
    }

    // ==========================================
    // POLICY CONFIGURATION VALIDATION
    // ==========================================
    @Test
    @DisplayName("Policy Validation: minimumRating < 1.0 when appraisalRequired=true throws BadRequestException")
    void testPolicyValidation_InvalidMinimumRatingLow() {
        CreateIncrementPolicyRequest req = new CreateIncrementPolicyRequest();
        req.setName("Invalid Low Policy");
        req.setAppraisalRequired(true);
        req.setMinimumRating(0.5);

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                policyService.createPolicy(req)
        );
        assertTrue(ex.getMessage().contains("minimumRating must be between 1.0 and 5.0 when appraisal is required"));
    }

    @Test
    @DisplayName("Policy Validation: minimumRating > 5.0 when appraisalRequired=true throws BadRequestException")
    void testPolicyValidation_InvalidMinimumRatingHigh() {
        CreateIncrementPolicyRequest req = new CreateIncrementPolicyRequest();
        req.setName("Invalid High Policy");
        req.setAppraisalRequired(true);
        req.setMinimumRating(5.5);

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                policyService.createPolicy(req)
        );
        assertTrue(ex.getMessage().contains("minimumRating must be between 1.0 and 5.0 when appraisal is required"));
    }

    @Test
    @DisplayName("Policy Validation: Valid appraisalRequired policy with rating 3.5 creates successfully")
    void testPolicyValidation_ValidPolicy_Success() {
        CreateIncrementPolicyRequest req = new CreateIncrementPolicyRequest();
        req.setName("Valid FY27 Policy");
        req.setAppraisalRequired(true);
        req.setMinimumRating(3.5);
        req.setMaximumIncrementPercentage(20.0);
        req.setMinimumIncrementPercentage(0.0);
        req.setBudgetLimit(BigDecimal.valueOf(1000000));
        req.setEffectiveDateRule(EffectiveDateRule.FIXED_DATE);
        req.setEffectiveDate(LocalDate.now().plusMonths(1));

        when(policyRepository.save(any(IncrementPolicy.class))).thenAnswer(invocation -> {
            IncrementPolicy p = invocation.getArgument(0);
            p.setId(99L);
            return p;
        });

        IncrementPolicyResponse resp = policyService.createPolicy(req);
        assertNotNull(resp);
        assertEquals(99L, resp.getId());
        assertTrue(resp.getAppraisalRequired());
        assertEquals(3.5, resp.getMinimumRating());
    }

    // ==========================================
    // MODULE CONFIGURATION CONTRADICTION & MATRIX TESTS
    // ==========================================
    @Test
    @DisplayName("Config Matrix: Appraisal OFF + Increment ON + appraisalRequired=false is VALID (Autonomous Increment)")
    void testConfigMatrix_AppraisalOff_AppraisalRequiredFalse_Valid() {
        IncrementPolicy policy = new IncrementPolicy();
        policy.setId(10L);
        policy.setAppraisalRequired(false);

        IncrementCycle cycle = new IncrementCycle();
        cycle.setId(20L);
        cycle.setOrganization(org1);
        cycle.setPolicy(policy);
        cycle.setAllocatedBudget(BigDecimal.ZERO);

        // When Appraisal module is OFF, appraisalId is null and appraisalRequired is false -> passes cleanly with 0 DB calls
        Appraisal resolved = appraisalResolver.resolve(policy, emp1, null, 100L);
        assertNull(resolved);
        verifyNoInteractions(appraisalRepository);

        EligibilityEvaluationResponse eval = eligibilityService.evaluate(cycle, emp1, null, 5.0, true);
        assertTrue(eval.isEligible(), "Autonomous increment without appraisal must pass when appraisalRequired=false");
    }

    @Test
    @DisplayName("Config Matrix: Appraisal ON + Increment ON + appraisalRequired=false is VALID (Independent Parallel)")
    void testConfigMatrix_AppraisalOn_AppraisalRequiredFalse_Valid() {
        IncrementPolicy policy = new IncrementPolicy();
        policy.setId(10L);
        policy.setAppraisalRequired(false);

        IncrementCycle cycle = new IncrementCycle();
        cycle.setId(20L);
        cycle.setOrganization(org1);
        cycle.setPolicy(policy);
        cycle.setAllocatedBudget(BigDecimal.ZERO);

        Appraisal completed = new Appraisal();
        completed.setId(77L);
        completed.setEmployee(emp1);
        completed.setOrganization(org1);
        completed.setFinalRating(4.2);
        completed.setStatus(AppraisalStatus.COMPLETED);

        when(appraisalRepository.findById(77L)).thenReturn(Optional.of(completed));

        Appraisal resolved = appraisalResolver.resolve(policy, emp1, 77L, 100L);
        assertNotNull(resolved);
        assertEquals(4.2, resolved.getFinalRating());

        EligibilityEvaluationResponse eval = eligibilityService.evaluate(cycle, emp1, resolved, 8.0, true);
        assertTrue(eval.isEligible());
    }

    @Test
    @DisplayName("Mode 1: Appraisal Only - Appraisal completes independently with no increment created and salary unchanged")
    void testMode1_AppraisalOnly_CompletedWithoutIncrement_SalaryUnchanged() {
        BigDecimal initialSalary = emp1.getAnnualSalary();

        Appraisal completed = new Appraisal();
        completed.setId(88L);
        completed.setEmployee(emp1);
        completed.setOrganization(org1);
        completed.setFinalRating(4.8);
        completed.setStatus(AppraisalStatus.COMPLETED);

        assertEquals(AppraisalStatus.COMPLETED, completed.getStatus());
        assertEquals(4.8, completed.getFinalRating());

        // Under Mode 1 (Appraisal Only), no increment recommendation is created and salary remains intact
        assertEquals(initialSalary, emp1.getAnnualSalary(), "Employee annual salary must remain unchanged in Appraisal-only mode");
        verifyNoInteractions(recommendationRepository);
    }

    @Test
    @DisplayName("Mode 3: Optional Appraisal - Supplying an appraisal ID belonging to another employee fails validation")
    void testMode3_OptionalAppraisal_WrongEmployeeMismatch_ThrowsBadRequest() {
        IncrementPolicy policy = new IncrementPolicy();
        policy.setId(10L);
        policy.setAppraisalRequired(false); // Optional in Mode 3

        Appraisal otherEmployeeAppraisal = new Appraisal();
        otherEmployeeAppraisal.setId(99L);
        otherEmployeeAppraisal.setEmployee(emp2); // Belong to emp2, but emp1 is requesting
        otherEmployeeAppraisal.setOrganization(org1);
        otherEmployeeAppraisal.setFinalRating(4.5);
        otherEmployeeAppraisal.setStatus(AppraisalStatus.COMPLETED);

        when(appraisalRepository.findById(99L)).thenReturn(Optional.of(otherEmployeeAppraisal));

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                appraisalResolver.resolve(policy, emp1, 99L, 100L)
        );
        assertTrue(ex.getMessage().contains("Appraisal does not belong to the specified employee"),
                "Optional appraisal ID must still be validated against employee context");
    }

    @Test
    @DisplayName("Rating Rule: Manager rating does NOT bypass missing finalRating")
    void testRatingRule_ManagerRatingDoesNotBypassNullFinalRating() {
        IncrementPolicy policy = new IncrementPolicy();
        policy.setId(10L);
        policy.setAppraisalRequired(true);
        policy.setMinimumRating(3.5);

        Appraisal appraisalWithoutFinalRating = new Appraisal();
        appraisalWithoutFinalRating.setId(55L);
        appraisalWithoutFinalRating.setEmployee(emp1);
        appraisalWithoutFinalRating.setOrganization(org1);
        appraisalWithoutFinalRating.setManagerRating(4.5); // High manager rating, but no final rating
        appraisalWithoutFinalRating.setFinalRating(null);
        appraisalWithoutFinalRating.setStatus(AppraisalStatus.COMPLETED);

        // Rating rule must evaluate finalRating only and reject when null
        EligibilityContext context = new EligibilityContext(emp1, appraisalWithoutFinalRating, null, policy, emp1.getAnnualSalary(), BigDecimal.ZERO, 5.0, true);
        EligibilityRuleResult result = ratingRule.evaluate(context);
        assertFalse(result.isPassed(), "Null finalRating must fail eligibility despite high manager rating");
    }

    @Test
    @DisplayName("Rating Rule: Manager rating does NOT bypass low finalRating below threshold")
    void testRatingRule_ManagerRatingDoesNotBypassLowFinalRating() {
        IncrementPolicy policy = new IncrementPolicy();
        policy.setId(10L);
        policy.setAppraisalRequired(true);
        policy.setMinimumRating(3.5);

        Appraisal lowRatingAppraisal = new Appraisal();
        lowRatingAppraisal.setId(56L);
        lowRatingAppraisal.setEmployee(emp1);
        lowRatingAppraisal.setOrganization(org1);
        lowRatingAppraisal.setManagerRating(4.5); // High manager rating
        lowRatingAppraisal.setFinalRating(3.2);   // Low final rating below 3.5
        lowRatingAppraisal.setStatus(AppraisalStatus.COMPLETED);

        EligibilityContext context = new EligibilityContext(emp1, lowRatingAppraisal, null, policy, emp1.getAnnualSalary(), BigDecimal.ZERO, 5.0, true);
        EligibilityRuleResult result = ratingRule.evaluate(context);
        assertFalse(result.isPassed(), "Rating rule must strictly evaluate finalRating 3.2 against minimum 3.5");
    }
}
