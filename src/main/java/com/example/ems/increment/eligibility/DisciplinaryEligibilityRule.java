package com.example.ems.increment.eligibility;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(5)
public class DisciplinaryEligibilityRule implements EligibilityRule {

    @Override
    public String getRuleName() {
        return "DISCIPLINARY_RECORD";
    }

    @Override
    public EligibilityRuleResult evaluate(EligibilityContext context) {
        if (!context.isDisciplinaryClear()) {
            return EligibilityRuleResult.fail(
                    getRuleName(),
                    "Clear",
                    "Disciplinary Hold Active",
                    "Employee has an active disciplinary hold or violation."
            );
        }

        return EligibilityRuleResult.pass(getRuleName(), "Disciplinary clearance confirmed.");
    }
}
