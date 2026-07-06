package com.example.ems.reports.revenue.service;

import com.example.ems.organization.entity.Payment;
import com.example.ems.organization.entity.SubscriptionInvoice;
import com.example.ems.reports.revenue.dto.*;
import com.example.ems.reports.revenue.projection.PlanRevenueDistributionProjection;
import com.example.ems.reports.revenue.repository.RevenueDashboardRepository;
import com.example.ems.reports.revenue.repository.RevenueInvoiceRepository;
import com.example.ems.reports.revenue.repository.RevenuePaymentRepository;
import com.example.ems.reports.revenue.specification.InvoiceSpecification;
import com.example.ems.reports.revenue.specification.PaymentSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class RevenueAnalyticsService {

    @Autowired
    private RevenuePaymentRepository paymentRepository;

    @Autowired
    private RevenueInvoiceRepository invoiceRepository;

    @Autowired
    private RevenueDashboardRepository dashboardRepository;

    @Autowired
    private PaymentSpecification paymentSpecification;

    @Autowired
    private InvoiceSpecification invoiceSpecification;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            .withZone(ZoneId.systemDefault());

    public Page<RevenuePaymentResponse> getPaymentsReport(RevenueFilterRequest filters) {
        Pageable pageable = getPageable(filters);
        Specification<Payment> spec = paymentSpecification.withFilters(filters);
        Page<Payment> page = paymentRepository.findAll(spec, pageable);

        return page.map(p -> {
            BigDecimal tax = p.getInvoice() != null && p.getInvoice().getTax() != null ? p.getInvoice().getTax() : BigDecimal.ZERO;
            BigDecimal discount = p.getInvoice() != null && p.getInvoice().getDiscount() != null ? p.getInvoice().getDiscount() : BigDecimal.ZERO;
            BigDecimal net = p.getAmount().subtract(tax);

            return new RevenuePaymentResponse(
                p.getId(),
                p.getInvoice() != null && p.getInvoice().getSubscription() != null && p.getInvoice().getSubscription().getOrganization() != null 
                    ? p.getInvoice().getSubscription().getOrganization().getId() : null,
                p.getInvoice() != null && p.getInvoice().getSubscription() != null && p.getInvoice().getSubscription().getOrganization() != null 
                    ? p.getInvoice().getSubscription().getOrganization().getName() : "N/A",
                p.getInvoice() != null && p.getInvoice().getSubscription() != null ? p.getInvoice().getSubscription().getPlanCode() : "N/A",
                p.getInvoice() != null ? p.getInvoice().getInvoiceNumber() : "N/A",
                p.getGateway(),
                p.getStatus(),
                p.getCurrency(),
                p.getAmount(),
                tax,
                discount,
                net,
                p.getPaidAt() != null ? DATE_FORMATTER.format(p.getPaidAt()) : "N/A"
            );
        });
    }

    public Page<RevenueInvoiceResponse> getInvoicesReport(RevenueFilterRequest filters) {
        Pageable pageable = getPageable(filters);
        Specification<SubscriptionInvoice> spec = invoiceSpecification.withFilters(filters);
        Page<SubscriptionInvoice> page = invoiceRepository.findAll(spec, pageable);

        return page.map(inv -> {
            BigDecimal tax = inv.getTax() != null ? inv.getTax() : BigDecimal.ZERO;
            BigDecimal discount = inv.getDiscount() != null ? inv.getDiscount() : BigDecimal.ZERO;
            BigDecimal subtotal = inv.getAmount().subtract(tax);

            return new RevenueInvoiceResponse(
                inv.getInvoiceNumber(),
                inv.getSubscription() != null && inv.getSubscription().getOrganization() != null 
                    ? inv.getSubscription().getOrganization().getId() : null,
                inv.getSubscription() != null && inv.getSubscription().getOrganization() != null 
                    ? inv.getSubscription().getOrganization().getName() : "N/A",
                inv.getSubscription() != null ? inv.getSubscription().getPlanCode() : "N/A",
                inv.getIssuedAt() != null ? DATE_FORMATTER.format(inv.getIssuedAt()) : "N/A",
                inv.getDueAt() != null ? inv.getDueAt().toString() : "N/A",
                inv.getStatus() != null ? inv.getStatus().name() : "N/A",
                subtotal,
                tax,
                discount,
                inv.getAmount()
            );
        });
    }

    public Page<RevenueRefundResponse> getRefundsReport(RevenueFilterRequest filters) {
        // Enforce refund status filter explicitly to Payment Specification
        filters.setPaymentStatus("REFUNDED");
        Pageable pageable = getPageable(filters);
        Specification<Payment> spec = paymentSpecification.withFilters(filters);
        Page<Payment> page = paymentRepository.findAll(spec, pageable);

        return page.map(p -> new RevenueRefundResponse(
            p.getGatewayRefundId() != null ? p.getGatewayRefundId() : "N/A",
            p.getInvoice() != null && p.getInvoice().getSubscription() != null && p.getInvoice().getSubscription().getOrganization() != null 
                ? p.getInvoice().getSubscription().getOrganization().getId() : null,
            p.getInvoice() != null && p.getInvoice().getSubscription() != null && p.getInvoice().getSubscription().getOrganization() != null 
                ? p.getInvoice().getSubscription().getOrganization().getName() : "N/A",
            p.getId(),
            p.getRefundAmount() != null ? p.getRefundAmount() : BigDecimal.ZERO,
            p.getRefundReason() != null ? p.getRefundReason() : "N/A",
            p.getRefundedAt() != null ? DATE_FORMATTER.format(p.getRefundedAt()) : "N/A",
            p.getGateway()
        ));
    }

    public List<RevenuePlanDistributionResponse> getPlansReport() {
        List<PlanRevenueDistributionProjection> list = dashboardRepository.getPlanRevenueDistribution();
        return list.stream().map(p -> new RevenuePlanDistributionResponse(
            p.getPlanCode(),
            p.getOrganizationCount(),
            p.getSubscribers(),
            p.getMonthlyRevenue(),
            p.getAnnualRevenue(),
            p.getLifetimeRevenue(),
            p.getAverageRevenue(),
            p.getGrowth()
        )).collect(Collectors.toList());
    }

    private Pageable getPageable(RevenueFilterRequest request) {
        Sort sort = Sort.unsorted();
        if (request.getSortBy() != null && !request.getSortBy().isEmpty()) {
            Sort.Direction dir = request.getDirection() != null && request.getDirection().equalsIgnoreCase("asc")
                ? Sort.Direction.ASC : Sort.Direction.DESC;
            sort = Sort.by(dir, request.getSortBy());
        }
        return PageRequest.of(request.getPage(), request.getSize(), sort);
    }
}
