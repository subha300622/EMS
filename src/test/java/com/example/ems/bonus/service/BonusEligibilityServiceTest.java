package com.example.ems.bonus.service;

import com.example.ems.bonus.entity.BonusPolicy;
import com.example.ems.bonus.entity.BonusPolicyStatus;
import com.example.ems.employee.entity.Employee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class BonusEligibilityServiceTest {

    private BonusEligibilityService eligibilityService;
    private Employee employee;
    private BonusPolicy policy;

    @BeforeEach
    void setUp() {
        eligibilityService = new BonusEligibilityService();

        employee = new Employee();
        employee.setId(101L);
        employee.setDepartment("Engineering");
        employee.setDesignation("Software Engineer");
        employee.setLocation("HQ");
        employee.setEmploymentType("FULL_TIME");
        employee.setStatus("ACTIVE");

        policy = new BonusPolicy();
        policy.setId(5L);
        policy.setStatus(BonusPolicyStatus.ACTIVE);
        policy.setEffectiveFrom(LocalDate.of(2026, 1, 1));
        policy.setEffectiveTo(LocalDate.of(2026, 12, 31));
    }

    @Test
    void testCheckEligibility_Eligible() {
        BonusEligibilityService.EligibilityResult result = eligibilityService.checkEligibility(
                employee, policy, LocalDate.of(2026, 4, 1), LocalDate.of(2026, 9, 30), BigDecimal.valueOf(4.5)
        );

        assertTrue(result.isEligible());
        assertNull(result.reason());
    }

    @Test
    void testCheckEligibility_PolicyNotActive() {
        policy.setStatus(BonusPolicyStatus.DRAFT);

        BonusEligibilityService.EligibilityResult result = eligibilityService.checkEligibility(
                employee, policy, LocalDate.of(2026, 4, 1), LocalDate.of(2026, 9, 30), BigDecimal.valueOf(4.5)
        );

        assertFalse(result.isEligible());
        assertTrue(result.reason().contains("not in ACTIVE status"));
    }

    @Test
    void testCheckEligibility_RatingBelowMinimum() {
        policy.setMinimumRating(BigDecimal.valueOf(4.0));

        BonusEligibilityService.EligibilityResult result = eligibilityService.checkEligibility(
                employee, policy, LocalDate.of(2026, 4, 1), LocalDate.of(2026, 9, 30), BigDecimal.valueOf(3.5)
        );

        assertFalse(result.isEligible());
        assertTrue(result.reason().contains("does not meet the minimum required rating"));
    }

    @Test
    void testCheckEligibility_DepartmentMismatch() {
        policy.setDepartmentId("Sales");

        BonusEligibilityService.EligibilityResult result = eligibilityService.checkEligibility(
                employee, policy, LocalDate.of(2026, 4, 1), LocalDate.of(2026, 9, 30), BigDecimal.valueOf(4.5)
        );

        assertFalse(result.isEligible());
        assertTrue(result.reason().contains("department does not match"));
    }
}
