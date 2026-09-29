package com.example.ems.increment.eligibility;

public class EligibilityRuleResult {

    private final boolean passed;
    private final String ruleName;
    private final Object required;
    private final Object actual;
    private final String message;

    private EligibilityRuleResult(boolean passed, String ruleName, Object required, Object actual, String message) {
        this.passed = passed;
        this.ruleName = ruleName;
        this.required = required;
        this.actual = actual;
        this.message = message;
    }

    public static EligibilityRuleResult pass(String ruleName, String message) {
        return new EligibilityRuleResult(true, ruleName, null, null, message);
    }

    public static EligibilityRuleResult fail(String ruleName, Object required, Object actual, String message) {
        return new EligibilityRuleResult(false, ruleName, required, actual, message);
    }

    public boolean isPassed() { return passed; }
    public String getRuleName() { return ruleName; }
    public Object getRequired() { return required; }
    public Object getActual() { return actual; }
    public String getMessage() { return message; }
}
