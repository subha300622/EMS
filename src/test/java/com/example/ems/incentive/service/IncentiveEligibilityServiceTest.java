package com.example.ems.incentive.service;

import com.example.ems.employee.entity.Employee;
import com.example.ems.incentive.entity.IncentivePolicy;
import com.example.ems.incentive.entity.IncentivePolicyStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class IncentiveEligibilityServiceTest {

    private IncentiveEligibilityService eligibilityService;
    private Employee employee;
    private IncentivePolicy policy;

    @BeforeEach
    void setUp() {
        eligibilityService = new IncentiveEligibilityService();

        employee = new Employee();
        employee.setId(1L);
        employee.setStatus("ACTIVE");
        employee.setDepartment("Engineering");
        employee.setDesignation("Software Engineer");
        employee.setLocation("HQ");
        employee.setEmploymentType("FULL_TIME");

        policy = new IncentivePolicy();
        policy.setId(100L);
        policy.setStatus(IncentivePolicyStatus.ACTIVE);
        policy.setEffectiveFrom(LocalDate.of(2026, 1, 1));
        policy.setEffectiveTo(LocalDate.of(2026, 12, 31));
    }

    @Test
    void testEligibleWhenAllCriteriaMatch() {
        policy.setDepartmentId("Engineering");
        policy.setDesignationId("Software Engineer");
        policy.setBranchId("HQ");
        policy.setEmployeeType("FULL_TIME");
        policy.setMinimumAchievementPercentage(BigDecimal.valueOf(80.0));
        policy.setMinimumRating(BigDecimal.valueOf(3.5));

        IncentiveEligibilityService.EligibilityResult result = eligibilityService.checkEligibility(
                employee, policy, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30),
                BigDecimal.valueOf(100.0), BigDecimal.valueOf(4.0));

        assertTrue(result.isEligible());
    }

    @Test
    void testIneligibleWhenEmployeeNotActive() {
        employee.setStatus("TERMINATED");

        IncentiveEligibilityService.EligibilityResult result = eligibilityService.checkEligibility(
                employee, policy, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30),
                BigDecimal.valueOf(100.0), BigDecimal.valueOf(4.0));

        assertFalse(result.isEligible());
        assertTrue(result.reason().contains("not active"));
    }

    @Test
    void testIneligibleWhenPolicyNotActive() {
        policy.setStatus(IncentivePolicyStatus.DRAFT);

        IncentiveEligibilityService.EligibilityResult result = eligibilityService.checkEligibility(
                employee, policy, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30),
                BigDecimal.valueOf(100.0), BigDecimal.valueOf(4.0));

        assertFalse(result.isEligible());
        assertTrue(result.reason().contains("not ACTIVE"));
    }

    @Test
    void testIneligibleWhenAchievementBelowThreshold() {
        policy.setMinimumAchievementPercentage(BigDecimal.valueOf(90.0));

        IncentiveEligibilityService.EligibilityResult result = eligibilityService.checkEligibility(
                employee, policy, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30),
                BigDecimal.valueOf(85.0), BigDecimal.valueOf(4.0));

        assertFalse(result.isEligible());
        assertTrue(result.reason().contains("below the minimum threshold"));
    }

    @Test
    void testIneligibleWhenRatingBelowThreshold() {
        policy.setMinimumRating(BigDecimal.valueOf(4.0));

        IncentiveEligibilityService.EligibilityResult result = eligibilityService.checkEligibility(
                employee, policy, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30),
                BigDecimal.valueOf(100.0), BigDecimal.valueOf(3.5));

        assertFalse(result.isEligible());
        assertTrue(result.reason().contains("below the minimum required rating"));
    }

    @Test
    void testIneligibleWhenDepartmentMismatch() {
        policy.setDepartmentId("Sales");

        IncentiveEligibilityService.EligibilityResult result = eligibilityService.checkEligibility(
                employee, policy, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30),
                BigDecimal.valueOf(100.0), BigDecimal.valueOf(4.0));

        assertFalse(result.isEligible());
        assertTrue(result.reason().contains("department does not match"));
    }
}
