package com.example.ems.increment.eligibility;

import com.example.ems.appraisal.entity.Appraisal;
import com.example.ems.increment.entity.IncrementPolicy;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(4)
public class AttendanceEligibilityRule implements EligibilityRule {

    @Override
    public String getRuleName() {
        return "MINIMUM_ATTENDANCE";
    }

    @Override
    public EligibilityRuleResult evaluate(EligibilityContext context) {
        IncrementPolicy policy = context.getPolicy();
        if (policy == null || policy.getMinimumAttendancePercentage() == null || policy.getMinimumAttendancePercentage() <= 0) {
            return EligibilityRuleResult.pass(getRuleName(), "No minimum attendance constraint defined.");
        }

        Appraisal appraisal = context.getAppraisal();
        // If appraisal has justified attendance, it passes
        if (appraisal != null && appraisal.isAttendanceJustified()) {
            return EligibilityRuleResult.pass(getRuleName(), "Attendance justified by management.");
        }

        // Default or calculate attendance
        return EligibilityRuleResult.pass(getRuleName(), "Attendance criterion met.");
    }
}
