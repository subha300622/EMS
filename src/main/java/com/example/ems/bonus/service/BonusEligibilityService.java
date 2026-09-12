package com.example.ems.bonus.service;

import com.example.ems.bonus.entity.BonusPolicy;
import com.example.ems.bonus.entity.BonusPolicyStatus;
import com.example.ems.employee.entity.Employee;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class BonusEligibilityService {

    public record EligibilityResult(boolean isEligible, String reason) {
        public static EligibilityResult eligible() {
            return new EligibilityResult(true, null);
        }

        public static EligibilityResult notEligible(String reason) {
            return new EligibilityResult(false, reason);
        }
    }

    public EligibilityResult checkEligibility(Employee employee, BonusPolicy policy,
                                             LocalDate periodStart, LocalDate periodEnd,
                                             BigDecimal performanceRating) {
        if (employee == null) {
            return EligibilityResult.notEligible("Employee profile is null.");
        }

        if (employee.getStatus() != null && !employee.getStatus().equalsIgnoreCase("ACTIVE")) {
            return EligibilityResult.notEligible("Employee is not active (status: " + employee.getStatus() + ").");
        }

        if (policy == null) {
            return EligibilityResult.notEligible("No bonus policy provided.");
        }

        if (policy.getStatus() != BonusPolicyStatus.ACTIVE) {
            return EligibilityResult.notEligible("Bonus policy is not in ACTIVE status (current: " + policy.getStatus() + ").");
        }

        if (policy.getEffectiveFrom().isAfter(periodEnd)) {
            return EligibilityResult.notEligible("Evaluation period ends before policy effective date: " + policy.getEffectiveFrom());
        }
        if (policy.getEffectiveTo() != null && policy.getEffectiveTo().isBefore(periodStart)) {
            return EligibilityResult.notEligible("Evaluation period starts after policy expired: " + policy.getEffectiveTo());
        }

        // Scope checks
        String empDept = employee.getDepartment();
        String empDesig = employee.getDesignation();
        String empLoc = employee.getLocation();
        String empType = employee.getEmploymentType();

        // Department check
        if (policy.getDepartmentId() != null && !policy.getDepartmentId().isBlank()) {
            if (empDept == null || !policy.getDepartmentId().equalsIgnoreCase(empDept)) {
                return EligibilityResult.notEligible("Employee department does not match policy requirement.");
            }
        }

        // Designation check
        if (policy.getDesignationId() != null && !policy.getDesignationId().isBlank()) {
            if (empDesig == null || !policy.getDesignationId().equalsIgnoreCase(empDesig)) {
                return EligibilityResult.notEligible("Employee designation does not match policy requirement.");
            }
        }

        // Branch / Location check
        if (policy.getBranchId() != null && !policy.getBranchId().isBlank()) {
            if (empLoc == null || !policy.getBranchId().equalsIgnoreCase(empLoc)) {
                return EligibilityResult.notEligible("Employee branch/location does not match policy requirement.");
            }
        }

        // Employee / Employment type check
        if (policy.getEmployeeType() != null && !policy.getEmployeeType().isBlank()) {
            if (empType == null || !policy.getEmployeeType().equalsIgnoreCase(empType)) {
                return EligibilityResult.notEligible("Employee type (" + empType + ") is not eligible for this policy.");
            }
        }

        // Minimum rating requirement
        if (policy.getMinimumRating() != null && policy.getMinimumRating().compareTo(BigDecimal.ZERO) > 0) {
            if (performanceRating == null || performanceRating.compareTo(policy.getMinimumRating()) < 0) {
                return EligibilityResult.notEligible(String.format(
                        "Performance rating (%.2f) does not meet the minimum required rating of %.2f.",
                        performanceRating != null ? performanceRating.doubleValue() : 0.0,
                        policy.getMinimumRating().doubleValue()));
            }
        }

        return EligibilityResult.eligible();
    }
}
