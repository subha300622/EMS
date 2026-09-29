package com.example.ems.increment.eligibility;

import com.example.ems.appraisal.entity.Appraisal;
import com.example.ems.increment.entity.IncrementPolicy;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class RatingEligibilityRule implements EligibilityRule {

    @Override
    public String getRuleName() {
        return "MINIMUM_RATING";
    }

    @Override
    public boolean isApplicable(EligibilityContext context) {
        IncrementPolicy policy = context.getPolicy();
        return policy != null && Boolean.TRUE.equals(policy.getAppraisalRequired());
    }

    @Override
    public EligibilityRuleResult evaluate(EligibilityContext context) {
        IncrementPolicy policy = context.getPolicy();
        if (policy == null || policy.getMinimumRating() == null) {
            return EligibilityRuleResult.pass(getRuleName(), "No minimum rating constraint defined.");
        }

        Appraisal appraisal = context.getAppraisal();
        if (appraisal == null) {
            return EligibilityRuleResult.fail(
                    getRuleName(),
                    policy.getMinimumRating(),
                    null,
                    "Required appraisal is missing."
            );
        }

        Double rating = appraisal.getFinalRating();
        if (rating == null) {
            return EligibilityRuleResult.fail(
                    getRuleName(),
                    policy.getMinimumRating(),
                    null,
                    "Final performance rating is missing."
            );
        }

        if (rating < policy.getMinimumRating()) {
            return EligibilityRuleResult.fail(
                    getRuleName(),
                    policy.getMinimumRating(),
                    rating,
                    "Final performance rating is below the minimum required rating of " + policy.getMinimumRating() + "."
            );
        }

        return EligibilityRuleResult.pass(getRuleName(), "Performance rating criterion met.");
    }
}
