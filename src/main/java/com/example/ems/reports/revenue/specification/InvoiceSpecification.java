package com.example.ems.reports.revenue.specification;

import com.example.ems.organization.entity.InvoiceStatus;
import com.example.ems.organization.entity.SubscriptionInvoice;
import com.example.ems.reports.revenue.dto.RevenueFilterRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class InvoiceSpecification implements BaseRevenueSpecification<SubscriptionInvoice> {

    @Override
    public Specification<SubscriptionInvoice> withFilters(RevenueFilterRequest filters) {
        if (filters == null) {
            return (root, query, cb) -> cb.conjunction();
        }

        LocalDate fromDate = filters.getFrom() != null ? LocalDate.parse(filters.getFrom()) : null;
        LocalDate toDate = filters.getTo() != null ? LocalDate.parse(filters.getTo()) : null;

        Specification<SubscriptionInvoice> spec = Specification.where(RevenueCommonSpecification.invoiceHasOrgId(filters.getOrganizationId()))
            .and(RevenueCommonSpecification.invoiceHasPlanCode(filters.getSubscriptionPlan()))
            .and(RevenueCommonSpecification.invoiceHasCurrency(filters.getCurrency()))
            .and(RevenueCommonSpecification.invoiceHasBillingCycle(filters.getBillingCycle()))
            .and(RevenueCommonSpecification.invoiceHasDateRange(fromDate, toDate))
            .and(RevenueCommonSpecification.invoiceHasAutoRenewal(filters.getAutoRenewal()))
            .and(RevenueCommonSpecification.invoiceHasAmountRange(filters.getMinAmount(), filters.getMaxAmount()));

        if (filters.getInvoiceStatus() != null && !filters.getInvoiceStatus().trim().isEmpty()) {
            try {
                InvoiceStatus statusEnum = InvoiceStatus.valueOf(filters.getInvoiceStatus().toUpperCase());
                spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), statusEnum));
            } catch (IllegalArgumentException e) {
                // Ignore invalid enum values
            }
        }

        if (filters.getCountry() != null && !filters.getCountry().trim().isEmpty()) {
            spec = spec.and((root, query, cb) -> {
                var subJoin = root.join("subscription", jakarta.persistence.criteria.JoinType.INNER);
                var orgJoin = subJoin.join("organization", jakarta.persistence.criteria.JoinType.INNER);
                var addrJoin = orgJoin.join("address", jakarta.persistence.criteria.JoinType.INNER);
                return cb.equal(cb.lower(addrJoin.get("country")), filters.getCountry().trim().toLowerCase());
            });
        }

        return spec;
    }
}
