package com.example.ems.reports.revenue.specification;

import com.example.ems.organization.entity.Payment;
import com.example.ems.reports.revenue.dto.RevenueFilterRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class PaymentSpecification implements BaseRevenueSpecification<Payment> {

    @Override
    public Specification<Payment> withFilters(RevenueFilterRequest filters) {
        if (filters == null) {
            return (root, query, cb) -> cb.conjunction();
        }

        LocalDate fromDate = filters.getFrom() != null ? LocalDate.parse(filters.getFrom()) : null;
        LocalDate toDate = filters.getTo() != null ? LocalDate.parse(filters.getTo()) : null;

        Specification<Payment> spec = Specification.where(RevenueCommonSpecification.paymentHasOrgId(filters.getOrganizationId()))
            .and(RevenueCommonSpecification.paymentHasOrgName(filters.getSubscriptionPlan())) // fallback check org by name search if plan is used as search in dashboard or custom query
            .and(RevenueCommonSpecification.paymentHasPlanCode(filters.getSubscriptionPlan()))
            .and(RevenueCommonSpecification.paymentHasCurrency(filters.getCurrency()))
            .and(RevenueCommonSpecification.paymentHasBillingCycle(filters.getBillingCycle()))
            .and(RevenueCommonSpecification.paymentHasDateRange(fromDate, toDate))
            .and(RevenueCommonSpecification.paymentHasAutoRenewal(filters.getAutoRenewal()))
            .and(RevenueCommonSpecification.paymentHasAmountRange(filters.getMinAmount(), filters.getMaxAmount()));

        if (filters.getGateway() != null && !filters.getGateway().trim().isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.equal(cb.lower(root.get("gateway")), filters.getGateway().trim().toLowerCase()));
        }

        if (filters.getPaymentMethod() != null && !filters.getPaymentMethod().trim().isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.equal(cb.lower(root.get("paymentMethod")), filters.getPaymentMethod().trim().toLowerCase()));
        }

        if (filters.getPaymentStatus() != null && !filters.getPaymentStatus().trim().isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.equal(cb.lower(root.get("status")), filters.getPaymentStatus().trim().toLowerCase()));
        }

        if (filters.getCountry() != null && !filters.getCountry().trim().isEmpty()) {
            spec = spec.and((root, query, cb) -> {
                var invoiceJoin = root.join("invoice", jakarta.persistence.criteria.JoinType.INNER);
                var subJoin = invoiceJoin.join("subscription", jakarta.persistence.criteria.JoinType.INNER);
                var orgJoin = subJoin.join("organization", jakarta.persistence.criteria.JoinType.INNER);
                var addrJoin = orgJoin.join("address", jakarta.persistence.criteria.JoinType.INNER);
                return cb.equal(cb.lower(addrJoin.get("country")), filters.getCountry().trim().toLowerCase());
            });
        }

        return spec;
    }
}
