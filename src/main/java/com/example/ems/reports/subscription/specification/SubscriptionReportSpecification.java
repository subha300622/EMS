package com.example.ems.reports.subscription.specification;

import com.example.ems.organization.entity.Subscription;
import com.example.ems.organization.entity.SubscriptionStatus;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class SubscriptionReportSpecification {

    public static Specification<Subscription> withFilters(
            String search, String status, String plan, String billingCycle, LocalDate fromDate, LocalDate toDate) {
        
        return (root, query, cb) -> {
            var predicate = cb.conjunction();

            if (search != null && !search.trim().isEmpty()) {
                String likePattern = "%" + search.trim().toLowerCase() + "%";
                var orgJoin = root.join("organization", JoinType.LEFT);
                var nameLike = cb.like(cb.lower(orgJoin.get("name")), likePattern);
                var planCodeLike = cb.like(cb.lower(root.get("planCode")), likePattern);
                var planNameLike = cb.like(cb.lower(root.get("planName")), likePattern);
                predicate = cb.and(predicate, cb.or(nameLike, planCodeLike, planNameLike));
            }

            if (status != null && !status.trim().isEmpty()) {
                try {
                    SubscriptionStatus statusEnum = SubscriptionStatus.valueOf(status.toUpperCase());
                    predicate = cb.and(predicate, cb.equal(root.get("status"), statusEnum));
                } catch (IllegalArgumentException e) {
                    // Ignore
                }
            }

            if (plan != null && !plan.trim().isEmpty()) {
                String likePattern = "%" + plan.trim().toLowerCase() + "%";
                var planCodeLike = cb.like(cb.lower(root.get("planCode")), likePattern);
                var planNameLike = cb.like(cb.lower(root.get("planName")), likePattern);
                predicate = cb.and(predicate, cb.or(planCodeLike, planNameLike));
            }

            if (billingCycle != null && !billingCycle.trim().isEmpty()) {
                // Extracts the "cycle" field from billingInfo JSON
                var jsonPathText = cb.function("jsonb_extract_path_text", String.class, 
                        root.get("billingInfo"), cb.literal("cycle"));
                predicate = cb.and(predicate, cb.equal(cb.lower(jsonPathText), billingCycle.trim().toLowerCase()));
            }

            if (fromDate != null) {
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("startDate"), fromDate));
            }

            if (toDate != null) {
                predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("expiryDate"), toDate));
            }

            return predicate;
        };
    }
}
