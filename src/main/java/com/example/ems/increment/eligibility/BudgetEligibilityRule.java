package com.example.ems.increment.eligibility;

import com.example.ems.increment.entity.IncrementCycle;
import com.example.ems.increment.entity.IncrementPolicy;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
@Order(6)
public class BudgetEligibilityRule implements EligibilityRule {

    @Override
    public String getRuleName() {
        return "BUDGET_AVAILABILITY";
    }

    @Override
    public EligibilityRuleResult evaluate(EligibilityContext context) {
        IncrementCycle cycle = context.getCycle();
        IncrementPolicy policy = context.getPolicy();

        BigDecimal budgetLimit = (cycle != null && cycle.getBudgetLimit() != null && cycle.getBudgetLimit().compareTo(BigDecimal.ZERO) > 0)
                ? cycle.getBudgetLimit()
                : (policy != null ? policy.getBudgetLimit() : BigDecimal.ZERO);

        if (budgetLimit == null || budgetLimit.compareTo(BigDecimal.ZERO) <= 0) {
            return EligibilityRuleResult.pass(getRuleName(), "No cycle budget limit defined.");
        }

        BigDecimal currentAllocated = context.getAllocatedBudget();
        Double proposedPercentage = context.getProposedIncrementPercentage();
        BigDecimal proposedAmount = BigDecimal.ZERO;
        if (proposedPercentage != null && proposedPercentage > 0 && context.getCurrentSalary() != null) {
            proposedAmount = context.getCurrentSalary().multiply(BigDecimal.valueOf(proposedPercentage)).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }

        BigDecimal totalProjected = currentAllocated.add(proposedAmount);
        if (totalProjected.compareTo(budgetLimit) > 0) {
            return EligibilityRuleResult.fail(
                    getRuleName(),
                    "Budget Limit: " + budgetLimit,
                    "Total Projected: " + totalProjected,
                    "Increment exceeds total cycle budget limit of " + budgetLimit + "."
            );
        }

        return EligibilityRuleResult.pass(getRuleName(), "Budget available for increment.");
    }
}
