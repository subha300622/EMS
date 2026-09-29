package com.example.ems.increment.eligibility;

public interface EligibilityRule {
    String getRuleName();
    
    default boolean isApplicable(EligibilityContext context) {
        return true;
    }

    EligibilityRuleResult evaluate(EligibilityContext context);
}
