package com.example.ems.increment.eligibility;

import com.example.ems.appraisal.entity.Appraisal;
import com.example.ems.increment.entity.IncrementPolicy;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(3)
public class GoalEligibilityRule implements EligibilityRule {

    @Override
    public String getRuleName() {
        return "MINIMUM_GOAL_ACHIEVEMENT";
    }

    @Override
    public EligibilityRuleResult evaluate(EligibilityContext context) {
        IncrementPolicy policy = context.getPolicy();
        if (policy == null || policy.getMinimumGoalAchievementPercentage() == null || policy.getMinimumGoalAchievementPercentage() <= 0) {
            return EligibilityRuleResult.pass(getRuleName(), "No minimum goal achievement percentage constraint defined.");
        }

        Appraisal appraisal = context.getAppraisal();
        Double achievement = null;
        if (appraisal != null) {
            // Check delivery / execution or custom rating if mapped, or default 100 if completed
            if (appraisal.getDeliveryManagementRating() != null) {
                achievement = (appraisal.getDeliveryManagementRating() / 5.0) * 100.0;
            } else if (appraisal.getFinalRating() != null) {
                achievement = (appraisal.getFinalRating() / 5.0) * 100.0;
            }
        }

        if (achievement != null && achievement < policy.getMinimumGoalAchievementPercentage()) {
            return EligibilityRuleResult.fail(
                    getRuleName(),
                    policy.getMinimumGoalAchievementPercentage() + "%",
                    Math.round(achievement * 10.0) / 10.0 + "%",
                    "Goal achievement percentage is below required " + policy.getMinimumGoalAchievementPercentage() + "%."
            );
        }

        return EligibilityRuleResult.pass(getRuleName(), "Goal achievement criterion met.");
    }
}
