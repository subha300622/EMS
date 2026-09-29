package com.example.ems.increment.eligibility;

import com.example.ems.appraisal.entity.Appraisal;
import com.example.ems.appraisal.entity.AppraisalStatus;
import com.example.ems.employee.entity.Employee;
import com.example.ems.increment.dto.EligibilityEvaluationResponse;
import com.example.ems.increment.entity.IncrementCycle;
import com.example.ems.increment.entity.IncrementPolicy;
import com.example.ems.increment.repository.IncrementEligibilityEvaluationRepository;
import com.example.ems.increment.repository.IncrementRecommendationRepository;
import com.example.ems.organization.entity.Organization;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Comprehensive Increment Boundary & Negative Validation Tests")
public class IncrementBoundaryValidationTest {

    @Mock
    private IncrementRecommendationRepository recommendationRepository;

    @Mock
    private IncrementEligibilityEvaluationRepository evaluationRepository;

    @Spy
    private List<EligibilityRule> rules = new ArrayList<>();

    @InjectMocks
    private IncrementEligibilityService eligibilityService;

    private Organization organization;
    private IncrementPolicy policy;
    private IncrementCycle cycle;
    private Employee employee;
    private Appraisal appraisal;

    @BeforeEach
    void setUp() {
        rules.clear();
        rules.add(new RatingEligibilityRule());
        rules.add(new ServiceDurationEligibilityRule());
        rules.add(new GoalEligibilityRule());
        rules.add(new AttendanceEligibilityRule());
        rules.add(new DisciplinaryEligibilityRule());
        rules.add(new BudgetEligibilityRule());

        organization = new Organization();
        organization.setId(1L);

        policy = new IncrementPolicy();
        policy.setId(10L);
        policy.setOrganization(organization);
        policy.setAppraisalRequired(true);
        policy.setMinimumRating(4.00);
        policy.setMinimumGoalAchievementPercentage(80.00);
        policy.setMinimumAttendancePercentage(90.00);
        policy.setMinimumServiceMonths(12);
        policy.setMaximumIncrementPercentage(20.0);
        policy.setMinimumIncrementPercentage(3.0);
        policy.setBudgetLimit(new BigDecimal("100000.00"));

        cycle = new IncrementCycle();
        cycle.setId(100L);
        cycle.setOrganization(organization);
        cycle.setPolicy(policy);
        cycle.setBudgetLimit(new BigDecimal("100000.00"));
        cycle.setAllocatedBudget(BigDecimal.ZERO);
        cycle.setEffectiveDate(LocalDate.of(2026, 10, 1));

        employee = new Employee();
        employee.setId(101L);
        employee.setFullName("Boundary Tester");
        employee.setEmployeeId("EMP101");
        employee.setStatus("ACTIVE");
        employee.setAnnualSalary(new BigDecimal("50000.00"));
        employee.setJoiningDate(LocalDate.of(2025, 10, 1)); // exactly 12 months before effectiveDate

        appraisal = new Appraisal();
        appraisal.setId(501L);
        appraisal.setEmployee(employee);
        appraisal.setStatus(AppraisalStatus.COMPLETED);
        appraisal.setFinalRating(4.00);
        appraisal.setDeliveryManagementRating(4.00);
    }

    // ── 1. RATING BOUNDARY TESTS (min = 4.00) ────────────────────────────────────

    @ParameterizedTest(name = "Rating: {0}, Expected Eligible: {1}")
    @CsvSource({
            "3.99, false",
            "4.00, true",
            "4.01, true",
            "5.00, true",
            "0.00, false",
            "-1.00, false"
    })
    void testRatingBoundaries(double rating, boolean expectedEligible) {
        appraisal.setFinalRating(rating);
        when(recommendationRepository.sumAllocatedAmountByCycleId(100L)).thenReturn(BigDecimal.ZERO);

        EligibilityEvaluationResponse resp = eligibilityService.evaluate(cycle, employee, appraisal, 10.0, true);
        assertEquals(expectedEligible, resp.isEligible(), "Failed rating boundary check for rating: " + rating);
    }

    // ── 2. SERVICE DURATION BOUNDARY TESTS (min = 12 months) ───────────────────

    @ParameterizedTest(name = "Service Months: {0}, Expected Eligible: {1}")
    @CsvSource({
            "11, false",
            "12, true",
            "13, true",
            "24, true",
            "0, false"
    })
    void testServiceDurationBoundaries(int months, boolean expectedEligible) {
        employee.setJoiningDate(cycle.getEffectiveDate().minusMonths(months));
        when(recommendationRepository.sumAllocatedAmountByCycleId(100L)).thenReturn(BigDecimal.ZERO);

        EligibilityEvaluationResponse resp = eligibilityService.evaluate(cycle, employee, appraisal, 10.0, true);
        assertEquals(expectedEligible, resp.isEligible(), "Failed service months boundary for months: " + months);
    }

    // ── 3. DISCIPLINARY RECORD VALIDATION ──────────────────────────────────────

    @Test
    @DisplayName("Disciplinary Clear: true -> Eligible, false -> Ineligible, null -> Ineligible")
    void testDisciplinaryValidation() {
        when(recommendationRepository.sumAllocatedAmountByCycleId(100L)).thenReturn(BigDecimal.ZERO);

        // Pass
        EligibilityEvaluationResponse respPass = eligibilityService.evaluate(cycle, employee, appraisal, 10.0, true);
        assertTrue(respPass.isEligible());
        assertTrue(respPass.isDisciplinaryClear());

        // Fail (false)
        EligibilityEvaluationResponse respFail = eligibilityService.evaluate(cycle, employee, appraisal, 10.0, false);
        assertFalse(respFail.isEligible());
        assertFalse(respFail.isDisciplinaryClear());

        // Fail (null)
        EligibilityEvaluationResponse respNull = eligibilityService.evaluate(cycle, employee, appraisal, 10.0, (Boolean) null);
        assertFalse(respNull.isEligible());
        assertFalse(respNull.isDisciplinaryClear());
    }

    // ── 4. BUDGET BOUNDARY VALIDATION ──────────────────────────────────────────

    @ParameterizedTest(name = "Allocated: {0}, Proposed Inc: {1}, Expected Available: {2}")
    @CsvSource({
            "80000.00, 20000.00, true",   // exactly 100,000 (valid boundary)
            "80000.00, 19999.99, true",   // within limit
            "80000.00, 20000.01, false",  // 100,000.01 exceeds limit by 1 cent
            "99999.99, 0.01, true",       // exactly at limit
            "100000.00, 0.01, false"      // exceeds limit
    })
    void testBudgetBoundaries(double allocated, double proposedIncrement, boolean expectedAvailable) {
        when(recommendationRepository.sumAllocatedAmountByCycleId(100L)).thenReturn(BigDecimal.valueOf(allocated));

        // Employee salary 50,000. Proposed percentage = (proposedIncrement / 50000) * 100
        double percentage = (proposedIncrement / 50000.0) * 100.0;

        EligibilityEvaluationResponse resp = eligibilityService.evaluate(cycle, employee, appraisal, percentage, true);
        assertEquals(expectedAvailable, resp.isBudgetAvailable(), "Failed budget boundary check");
    }

    // ── 5. APPRAISAL STATUS MATRIX VALIDATION ──────────────────────────────────

    @ParameterizedTest(name = "Appraisal Status: {0}")
    @EnumSource(value = AppraisalStatus.class, names = {"DRAFT", "STAGE_REVIEW", "FINAL_REVIEW", "CANCELLED"})
    void testNonCompletedAppraisalStatusRejection(AppraisalStatus status) {
        appraisal.setStatus(status);
        assertNotEquals(AppraisalStatus.COMPLETED, appraisal.getStatus());
    }
}
