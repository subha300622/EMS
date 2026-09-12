package com.example.ems.reports.revenue.specification;

import com.example.ems.organization.entity.Payment;
import com.example.ems.organization.entity.SubscriptionInvoice;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

public final class RevenueCommonSpecification {

    private RevenueCommonSpecification() {}

    // Payment-based common specifications
    public static Specification<Payment> paymentHasOrgId(Long orgId) {
        return (root, query, cb) -> {
            if (orgId == null) return cb.conjunction();
            Join<?, ?> invoiceJoin = root.join("invoice", JoinType.INNER);
            Join<?, ?> subJoin = invoiceJoin.join("subscription", JoinType.INNER);
            Join<?, ?> orgJoin = subJoin.join("organization", JoinType.INNER);
            return cb.equal(orgJoin.get("id"), orgId);
        };
    }

    public static Specification<Payment> paymentHasOrgName(String orgName) {
        return (root, query, cb) -> {
            if (orgName == null || orgName.trim().isEmpty()) return cb.conjunction();
            Join<?, ?> invoiceJoin = root.join("invoice", JoinType.INNER);
            Join<?, ?> subJoin = invoiceJoin.join("subscription", JoinType.INNER);
            Join<?, ?> orgJoin = subJoin.join("organization", JoinType.INNER);
            return cb.like(cb.lower(orgJoin.get("name")), "%" + orgName.trim().toLowerCase() + "%");
        };
    }

    public static Specification<Payment> paymentHasPlanCode(String plan) {
        return (root, query, cb) -> {
            if (plan == null || plan.trim().isEmpty()) return cb.conjunction();
            Join<?, ?> invoiceJoin = root.join("invoice", JoinType.INNER);
            Join<?, ?> subJoin = invoiceJoin.join("subscription", JoinType.INNER);
            return cb.equal(cb.lower(subJoin.get("planCode")), plan.trim().toLowerCase());
        };
    }

    public static Specification<Payment> paymentHasCurrency(String currency) {
        return (root, query, cb) -> {
            if (currency == null || currency.trim().isEmpty()) return cb.conjunction();
            return cb.equal(cb.lower(root.get("currency")), currency.trim().toLowerCase());
        };
    }

    public static Specification<Payment> paymentHasBillingCycle(String cycle) {
        return (root, query, cb) -> {
            if (cycle == null || cycle.trim().isEmpty()) return cb.conjunction();
            Join<?, ?> invoiceJoin = root.join("invoice", JoinType.INNER);
            Join<?, ?> subJoin = invoiceJoin.join("subscription", JoinType.INNER);
            Expression<String> jsonPathText = cb.function("jsonb_extract_path_text", String.class, 
                    subJoin.get("billingInfo"), cb.literal("cycle"));
            return cb.equal(cb.lower(jsonPathText), cycle.trim().toLowerCase());
        };
    }

    public static Specification<Payment> paymentHasDateRange(LocalDate from, LocalDate to) {
        return (root, query, cb) -> {
            var predicate = cb.conjunction();
            if (from != null) {
                Instant startInstant = from.atStartOfDay(ZoneId.systemDefault()).toInstant();
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("paidAt"), startInstant));
            }
            if (to != null) {
                Instant endInstant = to.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
                predicate = cb.and(predicate, cb.lessThan(root.get("paidAt"), endInstant));
            }
            return predicate;
        };
    }

    public static Specification<Payment> paymentHasAutoRenewal(Boolean autoRenew) {
        return (root, query, cb) -> {
            if (autoRenew == null) return cb.conjunction();
            Join<?, ?> invoiceJoin = root.join("invoice", JoinType.INNER);
            Join<?, ?> subJoin = invoiceJoin.join("subscription", JoinType.INNER);
            return cb.equal(subJoin.get("autoRenew"), autoRenew);
        };
    }

    public static Specification<Payment> paymentHasAmountRange(BigDecimal min, BigDecimal max) {
        return (root, query, cb) -> {
            var predicate = cb.conjunction();
            if (min != null) {
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("amount"), min));
            }
            if (max != null) {
                predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("amount"), max));
            }
            return predicate;
        };
    }

    // Invoice-based common specifications
    public static Specification<SubscriptionInvoice> invoiceHasOrgId(Long orgId) {
        return (root, query, cb) -> {
            if (orgId == null) return cb.conjunction();
            Join<?, ?> subJoin = root.join("subscription", JoinType.INNER);
            Join<?, ?> orgJoin = subJoin.join("organization", JoinType.INNER);
            return cb.equal(orgJoin.get("id"), orgId);
        };
    }

    public static Specification<SubscriptionInvoice> invoiceHasOrgName(String orgName) {
        return (root, query, cb) -> {
            if (orgName == null || orgName.trim().isEmpty()) return cb.conjunction();
            Join<?, ?> subJoin = root.join("subscription", JoinType.INNER);
            Join<?, ?> orgJoin = subJoin.join("organization", JoinType.INNER);
            return cb.like(cb.lower(orgJoin.get("name")), "%" + orgName.trim().toLowerCase() + "%");
        };
    }

    public static Specification<SubscriptionInvoice> invoiceHasPlanCode(String plan) {
        return (root, query, cb) -> {
            if (plan == null || plan.trim().isEmpty()) return cb.conjunction();
            Join<?, ?> subJoin = root.join("subscription", JoinType.INNER);
            return cb.equal(cb.lower(subJoin.get("planCode")), plan.trim().toLowerCase());
        };
    }

    public static Specification<SubscriptionInvoice> invoiceHasCurrency(String currency) {
        return (root, query, cb) -> {
            if (currency == null || currency.trim().isEmpty()) return cb.conjunction();
            return cb.equal(cb.lower(root.get("currency")), currency.trim().toLowerCase());
        };
    }

    public static Specification<SubscriptionInvoice> invoiceHasBillingCycle(String cycle) {
        return (root, query, cb) -> {
            if (cycle == null || cycle.trim().isEmpty()) return cb.conjunction();
            Join<?, ?> subJoin = root.join("subscription", JoinType.INNER);
            Expression<String> jsonPathText = cb.function("jsonb_extract_path_text", String.class, 
                    subJoin.get("billingInfo"), cb.literal("cycle"));
            return cb.equal(cb.lower(jsonPathText), cycle.trim().toLowerCase());
        };
    }

    public static Specification<SubscriptionInvoice> invoiceHasDateRange(LocalDate from, LocalDate to) {
        return (root, query, cb) -> {
            var predicate = cb.conjunction();
            if (from != null) {
                Instant startInstant = from.atStartOfDay(ZoneId.systemDefault()).toInstant();
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("issuedAt"), startInstant));
            }
            if (to != null) {
                Instant endInstant = to.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
                predicate = cb.and(predicate, cb.lessThan(root.get("issuedAt"), endInstant));
            }
            return predicate;
        };
    }

    public static Specification<SubscriptionInvoice> invoiceHasAutoRenewal(Boolean autoRenew) {
        return (root, query, cb) -> {
            if (autoRenew == null) return cb.conjunction();
            Join<?, ?> subJoin = root.join("subscription", JoinType.INNER);
            return cb.equal(subJoin.get("autoRenew"), autoRenew);
        };
    }

    public static Specification<SubscriptionInvoice> invoiceHasAmountRange(BigDecimal min, BigDecimal max) {
        return (root, query, cb) -> {
            var predicate = cb.conjunction();
            if (min != null) {
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("amount"), min));
            }
            if (max != null) {
                predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("amount"), max));
            }
            return predicate;
        };
    }
}
