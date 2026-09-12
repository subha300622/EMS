package com.example.ems.organization.specification;

import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.entity.Subscription;
import com.example.ems.organization.entity.SubscriptionPlan;
import com.example.ems.organization.entity.SubscriptionStatus;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class OrganizationSpecification {

    public static Specification<Organization> filter(String search, String status, String plan) {
        return (Root<Organization> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // By default exclude soft-deleted records
            predicates.add(cb.equal(root.get("isDeleted"), false));

            if (search != null && !search.trim().isEmpty()) {
                String searchPattern = "%" + search.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), searchPattern),
                        cb.like(cb.lower(root.get("organizationCode")), searchPattern),
                        cb.like(cb.lower(root.get("email")), searchPattern)
                ));
            }

            if ((status != null && !status.trim().isEmpty()) || (plan != null && !plan.trim().isEmpty())) {
                Join<Organization, Subscription> subJoin = root.join("subscriptions", JoinType.INNER);

                // Filter active subscriptions
                predicates.add(cb.equal(subJoin.get("status"), SubscriptionStatus.ACTIVE));

                if (status != null && !status.trim().isEmpty()) {
                    try {
                        SubscriptionStatus statusEnum = SubscriptionStatus.valueOf(status.trim().toUpperCase());
                        predicates.add(cb.equal(subJoin.get("status"), statusEnum));
                    } catch (IllegalArgumentException e) {
                        // ignore invalid values
                    }
                }

                if (plan != null && !plan.trim().isEmpty()) {
                    try {
                        SubscriptionPlan planEnum = SubscriptionPlan.valueOf(plan.trim().toUpperCase());
                        predicates.add(cb.equal(subJoin.get("plan"), planEnum));
                    } catch (IllegalArgumentException e) {
                        // ignore invalid values
                    }
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
