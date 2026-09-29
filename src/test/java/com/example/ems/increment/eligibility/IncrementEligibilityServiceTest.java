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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
public class IncrementEligibilityServiceTest {

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
        policy.setMinimumRating(3.5);
        policy.setMinimumGoalAchievementPercentage(70.0);
        policy.setMinimumAttendancePercentage(90.0);
        policy.setMinimumServiceMonths(12);
        policy.setMaximumIncrementPercentage(20.0);
        policy.setMinimumIncrementPercentage(3.0);
        policy.setBudgetLimit(new BigDecimal("1000000"));

        cycle = new IncrementCycle();
        cycle.setId(100L);
        cycle.setOrganization(organization);
        cycle.setPolicy(policy);
        cycle.setBudgetLimit(new BigDecimal("1000000"));
        cycle.setAllocatedBudget(BigDecimal.ZERO);
        cycle.setEffectiveDate(LocalDate.of(2026, 10, 1));

        employee = new Employee();
        employee.setId(125L);
        employee.setFullName("John Doe");
        employee.setEmployeeId("EMP125");
        employee.setStatus("ACTIVE");
        employee.setAnnualSalary(new BigDecimal("600000"));
        employee.setJoiningDate(LocalDate.of(2024, 1, 1)); // > 2 years service

        appraisal = new Appraisal();
        appraisal.setId(501L);
        appraisal.setEmployee(employee);
        appraisal.setStatus(AppraisalStatus.COMPLETED);
        appraisal.setFinalRating(4.2);
        appraisal.setDeliveryManagementRating(4.5);
    }

    @Test
    void testEvaluate_AllCriteriaMet_Eligible() {
        when(recommendationRepository.sumAllocatedAmountByCycleId(100L)).thenReturn(new BigDecimal("100000"));

        EligibilityEvaluationResponse resp = eligibilityService.evaluate(cycle, employee, appraisal, 10.0, true);

        assertNotNull(resp);
        assertTrue(resp.isEligible());
        assertEquals(125L, resp.getEmployeeId());
        assertEquals(501L, resp.getAppraisalId());
        assertEquals(4.2, resp.getFinalRating());
        assertTrue(resp.isDisciplinaryClear());
        assertTrue(resp.isBudgetAvailable());
        assertEquals("Employee satisfies all increment eligibility criteria.", resp.getReason());
    }

    @Test
    void testEvaluate_LowRating_Ineligible() {
        appraisal.setFinalRating(3.1); // Below 3.5 required
        when(recommendationRepository.sumAllocatedAmountByCycleId(100L)).thenReturn(BigDecimal.ZERO);

        EligibilityEvaluationResponse resp = eligibilityService.evaluate(cycle, employee, appraisal, 10.0, true);

        assertNotNull(resp);
        assertFalse(resp.isEligible());
        assertNotNull(resp.getReasons());
        assertEquals(1, resp.getReasons().size());
        assertEquals("MINIMUM_RATING", resp.getReasons().get(0).getRule());
        assertEquals(3.5, resp.getReasons().get(0).getRequired());
        assertEquals(3.1, resp.getReasons().get(0).getActual());
    }

    @Test
    void testEvaluate_ShortServiceDuration_Ineligible() {
        employee.setJoiningDate(LocalDate.of(2026, 5, 1)); // Only 5 months before effectiveDate
        when(recommendationRepository.sumAllocatedAmountByCycleId(100L)).thenReturn(BigDecimal.ZERO);

        EligibilityEvaluationResponse resp = eligibilityService.evaluate(cycle, employee, appraisal, 10.0, true);

        assertNotNull(resp);
        assertFalse(resp.isEligible());
        assertTrue(resp.getReasons().stream().anyMatch(r -> "MINIMUM_SERVICE".equals(r.getRule())));
    }

    @Test
    void testEvaluate_DisciplinaryHold_Ineligible() {
        when(recommendationRepository.sumAllocatedAmountByCycleId(100L)).thenReturn(BigDecimal.ZERO);

        EligibilityEvaluationResponse resp = eligibilityService.evaluate(cycle, employee, appraisal, 10.0, false);

        assertNotNull(resp);
        assertFalse(resp.isEligible());
        assertFalse(resp.isDisciplinaryClear());
        assertTrue(resp.getReasons().stream().anyMatch(r -> "DISCIPLINARY_RECORD".equals(r.getRule())));
    }

    @Test
    void testEvaluate_ExceedsBudget_Ineligible() {
        // Cycle budget 1,000,000, already allocated 950,000, proposed 10% on 600,000 = 60,000 => 1,010,000 > 1,000,000
        when(recommendationRepository.sumAllocatedAmountByCycleId(100L)).thenReturn(new BigDecimal("950000"));

        EligibilityEvaluationResponse resp = eligibilityService.evaluate(cycle, employee, appraisal, 10.0, true);

        assertNotNull(resp);
        assertFalse(resp.isEligible());
        assertFalse(resp.isBudgetAvailable());
        assertTrue(resp.getReasons().stream().anyMatch(r -> "BUDGET_AVAILABILITY".equals(r.getRule())));
    }

    @Test
    void testEvaluate_LowGoalAchievement_Ineligible() {
        appraisal.setDeliveryManagementRating(2.5); // (2.5 / 5.0) * 100 = 50% < 70% required
        appraisal.setFinalRating(4.0);
        when(recommendationRepository.sumAllocatedAmountByCycleId(100L)).thenReturn(BigDecimal.ZERO);

        EligibilityEvaluationResponse resp = eligibilityService.evaluate(cycle, employee, appraisal, 10.0, true);

        assertNotNull(resp);
        assertFalse(resp.isEligible());
        assertTrue(resp.getReasons().stream().anyMatch(r -> "MINIMUM_GOAL_ACHIEVEMENT".equals(r.getRule())));
    }
}
