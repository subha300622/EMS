package com.example.ems.increment.eligibility;

import com.example.ems.employee.entity.Employee;
import com.example.ems.increment.entity.IncrementPolicy;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Period;

@Component
@Order(2)
public class ServiceDurationEligibilityRule implements EligibilityRule {

    @Override
    public String getRuleName() {
        return "MINIMUM_SERVICE";
    }

    @Override
    public EligibilityRuleResult evaluate(EligibilityContext context) {
        IncrementPolicy policy = context.getPolicy();
        if (policy == null || policy.getMinimumServiceMonths() == null || policy.getMinimumServiceMonths() <= 0) {
            return EligibilityRuleResult.pass(getRuleName(), "No minimum service duration constraint defined.");
        }

        Employee employee = context.getEmployee();
        if (employee == null || employee.getJoiningDate() == null) {
            return EligibilityRuleResult.pass(getRuleName(), "Employee joining date not specified; skipped.");
        }

        LocalDate refDate = (context.getCycle() != null && context.getCycle().getEffectiveDate() != null)
                ? context.getCycle().getEffectiveDate()
                : LocalDate.now();

        Period period = Period.between(employee.getJoiningDate(), refDate);
        int totalMonths = period.getYears() * 12 + period.getMonths();

        if (totalMonths < policy.getMinimumServiceMonths()) {
            return EligibilityRuleResult.fail(
                    getRuleName(),
                    policy.getMinimumServiceMonths() + " months",
                    totalMonths + " months",
                    "Employee service duration (" + totalMonths + " months) is below required " + policy.getMinimumServiceMonths() + " months."
            );
        }

        return EligibilityRuleResult.pass(getRuleName(), "Service duration criterion met.");
    }
}
