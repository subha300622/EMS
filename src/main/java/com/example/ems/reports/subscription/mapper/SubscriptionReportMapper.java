package com.example.ems.reports.subscription.mapper;

import com.example.ems.organization.dto.SubscriptionDtos.*;
import com.example.ems.organization.entity.Subscription;
import com.example.ems.reports.subscription.dto.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Component
public class SubscriptionReportMapper {

    public OrgSubscriptionListItem toListItem(Subscription sub) {
        BigDecimal amount = BigDecimal.ZERO;
        String cycle = "MONTHLY";
        if (sub.getBillingInfo() != null) {
            if (sub.getBillingInfo().get("amount") != null) {
                amount = new BigDecimal(sub.getBillingInfo().get("amount").toString());
            }
            if (sub.getBillingInfo().get("cycle") != null) {
                cycle = sub.getBillingInfo().get("cycle").toString();
            }
        }

        String orgName = sub.getOrganization() != null ? sub.getOrganization().getName() : "N/A";
        Long orgId = sub.getOrganization() != null ? sub.getOrganization().getId() : null;

        return new OrgSubscriptionListItem(
                orgId,
                orgName,
                sub.getPlanCode(),
                sub.getStatus().name(),
                cycle,
                sub.getStartDate() != null ? sub.getStartDate().toString() : "N/A",
                sub.getExpiryDate() != null ? sub.getExpiryDate().toString() : "N/A",
                amount,
                sub.isAutoRenew()
        );
    }

    public SubscriptionDetailResponse toDetailResponse(Subscription sub) {
        return toDetail(sub);
    }

    public SubscriptionDetailResponse toDetail(Subscription sub) {
        PlanDto plan = new PlanDto(sub.getPlanCode(), sub.getPlanName());

        BigDecimal amount = BigDecimal.ZERO;
        String currency = "USD";
        String cycle = "MONTHLY";
        BigDecimal tax = BigDecimal.ZERO;
        BigDecimal discount = BigDecimal.ZERO;
        BigDecimal finalAmt = BigDecimal.ZERO;

        if (sub.getBillingInfo() != null) {
            if (sub.getBillingInfo().get("amount") != null) {
                amount = new BigDecimal(sub.getBillingInfo().get("amount").toString());
            }
            if (sub.getBillingInfo().get("currency") != null) {
                currency = sub.getBillingInfo().get("currency").toString();
            }
            if (sub.getBillingInfo().get("cycle") != null) {
                cycle = sub.getBillingInfo().get("cycle").toString();
            }
            if (sub.getBillingInfo().get("taxAmount") != null) {
                tax = new BigDecimal(sub.getBillingInfo().get("taxAmount").toString());
            }
            if (sub.getBillingInfo().get("discountAmount") != null) {
                discount = new BigDecimal(sub.getBillingInfo().get("discountAmount").toString());
            }
            if (sub.getBillingInfo().get("finalAmount") != null) {
                finalAmt = new BigDecimal(sub.getBillingInfo().get("finalAmount").toString());
            }
        }

        BillingDto billing = new BillingDto(cycle, amount, currency, tax, discount, finalAmt);

        long remainingDays = 0;
        if (sub.getExpiryDate() != null) {
            remainingDays = Math.max(0, ChronoUnit.DAYS.between(LocalDate.now(), sub.getExpiryDate()));
        }

        DurationDto duration = new DurationDto(
                sub.getStartDate(),
                sub.getExpiryDate(),
                sub.isAutoRenew(),
                remainingDays
        );

        Integer maxEmployees = 1000;
        Integer maxAdmins = 25;
        Integer maxDepartments = 100;
        Integer maxStorageGB = 500;
        Integer maxApiRequestsPerMonth = 10000;

        if (sub.getLimitsInfo() != null) {
            if (sub.getLimitsInfo().get("maxEmployees") != null) {
                maxEmployees = Integer.parseInt(sub.getLimitsInfo().get("maxEmployees").toString());
            }
            if (sub.getLimitsInfo().get("maxAdmins") != null) {
                maxAdmins = Integer.parseInt(sub.getLimitsInfo().get("maxAdmins").toString());
            }
            if (sub.getLimitsInfo().get("maxDepartments") != null) {
                maxDepartments = Integer.parseInt(sub.getLimitsInfo().get("maxDepartments").toString());
            }
            if (sub.getLimitsInfo().get("maxStorageGB") != null) {
                maxStorageGB = Integer.parseInt(sub.getLimitsInfo().get("maxStorageGB").toString());
            }
            if (sub.getLimitsInfo().get("maxApiRequestsPerMonth") != null) {
                maxApiRequestsPerMonth = Integer.parseInt(sub.getLimitsInfo().get("maxApiRequestsPerMonth").toString());
            }
        }

        LimitsDto limits = new LimitsDto(maxEmployees, maxAdmins, maxDepartments, maxStorageGB, maxApiRequestsPerMonth);

        String method = "N/A";
        String referenceNumber = "N/A";
        String paymentStatus = "N/A";

        if (sub.getPaymentInfo() != null) {
            if (sub.getPaymentInfo().get("method") != null) {
                method = sub.getPaymentInfo().get("method").toString();
            }
            if (sub.getPaymentInfo().get("referenceNumber") != null) {
                referenceNumber = sub.getPaymentInfo().get("referenceNumber").toString();
            }
            if (sub.getPaymentInfo().get("paymentStatus") != null) {
                paymentStatus = sub.getPaymentInfo().get("paymentStatus").toString();
            }
        }

        PaymentDto payment = new PaymentDto(method, referenceNumber, paymentStatus);

        String orgName = sub.getOrganization() != null ? sub.getOrganization().getName() : "N/A";
        Long orgId = sub.getOrganization() != null ? sub.getOrganization().getId() : null;

        return new SubscriptionDetailResponse(
                sub.getId(),
                orgId,
                orgName,
                sub.getStatus().name(),
                plan,
                billing,
                duration,
                limits,
                payment,
                sub.getCreatedAt() != null ? sub.getCreatedAt().toString() : "N/A",
                "system"
        );
    }

    public ExpiringSubscriptionEntry toExpiringEntry(Subscription sub) {
        long remainingDays = 0;
        if (sub.getExpiryDate() != null) {
            remainingDays = Math.max(0, ChronoUnit.DAYS.between(LocalDate.now(), sub.getExpiryDate()));
        }
        String orgName = sub.getOrganization() != null ? sub.getOrganization().getName() : "N/A";
        Long orgId = sub.getOrganization() != null ? sub.getOrganization().getId() : null;
        return new ExpiringSubscriptionEntry(
                orgId,
                orgName,
                sub.getPlanCode(),
                sub.getExpiryDate() != null ? sub.getExpiryDate().toString() : "N/A",
                remainingDays
        );
    }

    public TrialOrganizationEntry toTrialEntry(Subscription sub) {
        long remainingDays = 0;
        if (sub.getExpiryDate() != null) {
            remainingDays = Math.max(0, ChronoUnit.DAYS.between(LocalDate.now(), sub.getExpiryDate()));
        }
        String orgName = sub.getOrganization() != null ? sub.getOrganization().getName() : "N/A";
        Long orgId = sub.getOrganization() != null ? sub.getOrganization().getId() : null;
        return new TrialOrganizationEntry(
                orgId,
                orgName,
                sub.getStartDate() != null ? sub.getStartDate().toString() : "N/A",
                sub.getExpiryDate() != null ? sub.getExpiryDate().toString() : "N/A",
                remainingDays
        );
    }
}
