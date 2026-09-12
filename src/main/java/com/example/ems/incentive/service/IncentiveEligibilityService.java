package com.example.ems.incentive.service;

import com.example.ems.employee.entity.Employee;
import com.example.ems.incentive.entity.IncentivePolicy;
import com.example.ems.incentive.entity.IncentivePolicyStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class IncentiveEligibilityService {

    public record EligibilityResult(boolean isEligible, String reason) {}

    public EligibilityResult checkEligibility(Employee employee, IncentivePolicy policy,
                                             LocalDate periodStart, LocalDate periodEnd,
                                             BigDecimal achievementPercentage, BigDecimal performanceRating) {
        if (employee == null) {
            return new EligibilityResult(false, "Employee profile is null.");
        }

        if (employee.getStatus() != null && !employee.getStatus().equalsIgnoreCase("ACTIVE")) {
            return new EligibilityResult(false, "Employee is not active (status: " + employee.getStatus() + ").");
        }

        if (policy == null) {
            return new EligibilityResult(false, "No incentive policy provided.");
        }

        if (policy.getStatus() != IncentivePolicyStatus.ACTIVE) {
            return new EligibilityResult(false, "Incentive policy is not ACTIVE (status: " + policy.getStatus() + ").");
        }

        if (policy.getEffectiveFrom().isAfter(periodEnd)) {
            return new EligibilityResult(false, "Policy is not effective yet (effective from: " + policy.getEffectiveFrom() + ").");
        }

        if (policy.getEffectiveTo() != null && policy.getEffectiveTo().isBefore(periodStart)) {
            return new EligibilityResult(false, "Policy has expired (effective to: " + policy.getEffectiveTo() + ").");
        }

        // Scope checks
        String empDept = employee.getDepartment();
        String empDesig = employee.getDesignation();
        String empLoc = employee.getLocation();
        String empType = employee.getEmploymentType();

        if (policy.getDepartmentId() != null && !policy.getDepartmentId().isBlank()) {
            if (empDept == null || !policy.getDepartmentId().equalsIgnoreCase(empDept)) {
                return new EligibilityResult(false, "Employee department does not match policy scope.");
            }
        }

        if (policy.getDesignationId() != null && !policy.getDesignationId().isBlank()) {
            if (empDesig == null || !policy.getDesignationId().equalsIgnoreCase(empDesig)) {
                return new EligibilityResult(false, "Employee designation does not match policy scope.");
            }
        }

        if (policy.getBranchId() != null && !policy.getBranchId().isBlank()) {
            if (empLoc == null || !policy.getBranchId().equalsIgnoreCase(empLoc)) {
                return new EligibilityResult(false, "Employee branch does not match policy scope.");
            }
        }

        if (policy.getEmployeeType() != null && !policy.getEmployeeType().isBlank()) {
            if (empType == null || !policy.getEmployeeType().equalsIgnoreCase(empType)) {
                return new EligibilityResult(false, "Employee employment type does not match policy scope.");
            }
        }

        // Minimum achievement threshold
        if (policy.getMinimumAchievementPercentage() != null && achievementPercentage != null) {
            if (achievementPercentage.compareTo(policy.getMinimumAchievementPercentage()) < 0) {
                return new EligibilityResult(false, String.format("Achievement percentage (%.2f%%) is below the minimum threshold (%.2f%%).",
                        achievementPercentage, policy.getMinimumAchievementPercentage()));
            }
        }

        // Minimum rating threshold
        if (policy.getMinimumRating() != null && performanceRating != null) {
            if (performanceRating.compareTo(policy.getMinimumRating()) < 0) {
                return new EligibilityResult(false, String.format("Performance rating (%.2f) is below the minimum required rating (%.2f).",
                        performanceRating, policy.getMinimumRating()));
            }
        }

        return new EligibilityResult(true, "Eligible");
    }
}
