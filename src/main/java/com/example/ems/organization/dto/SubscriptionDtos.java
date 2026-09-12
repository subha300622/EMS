package com.example.ems.organization.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class SubscriptionDtos {

    public record PlanDto(
            @NotBlank(message = "Plan code is required") String code,
            String name
    ) {}

    public record BillingDto(
            String cycle,
            BigDecimal amount,
            String currency,
            BigDecimal taxAmount,
            BigDecimal discountAmount,
            BigDecimal finalAmount
    ) {}

    public record DurationDto(
            LocalDate startDate,
            LocalDate endDate,
            Boolean autoRenew,
            Long remainingDays
    ) {}

    public record LimitsDto(
            Integer maxEmployees,
            Integer maxAdmins,
            Integer maxDepartments,
            Integer maxStorageGB,
            Integer maxApiRequestsPerMonth
    ) {}

    public record FeaturesDto(
            Boolean employeeManagement,
            Boolean attendance,
            Boolean leaveManagement,
            Boolean payroll,
            Boolean recruitment,
            Boolean performanceManagement,
            Boolean assetManagement,
            Boolean training,
            Boolean helpDesk,
            Boolean documentManagement,
            Boolean reports,
            Boolean apiAccess,
            Boolean singleSignOn
    ) {}

    public record PaymentDto(
            String method,
            String referenceNumber,
            String paymentStatus
    ) {}

    public record CreateSubscriptionRequest(
            Long organizationId,
            @NotNull @Valid PlanDto plan,
            @Valid BillingDto billing,
            @Valid DurationDto duration,
            @Valid LimitsDto limits,
            @Valid FeaturesDto features,
            @Valid PaymentDto payment,
            String notes
    ) {}

    public record UpgradeSubscriptionRequest(
            @NotBlank String newPlanCode,
            LocalDate effectiveDate,
            String remarks,
            BillingDto billing,
            LimitsDto limits,
            FeaturesDto features
    ) {}

    public record DowngradeSubscriptionRequest(
            @NotBlank String newPlanCode,
            LocalDate effectiveDate,
            String remarks,
            BillingDto billing,
            LimitsDto limits,
            FeaturesDto features
    ) {}

    public record RenewSubscriptionRequest(
            @Valid BillingDto billing,
            @Valid DurationDto duration,
            @Valid PaymentDto payment,
            String remarks
    ) {}

    public record AutoRenewRequest(
            boolean autoRenew,
            String remarks
    ) {}

    public record ModulesRequest(
            List<String> enabledModules,
            List<String> disabledModules
    ) {}

    public record LimitsRequest(
            Integer employeeLimit,
            Integer adminLimit,
            Integer storageLimit,
            Integer apiRateLimit
    ) {}

    public record SubscriptionResponse(
            Long subscriptionId,
            Long organizationId,
            String status,
            PlanDto plan,
            BillingDto billing,
            DurationDto duration,
            LimitsDto limits,
            PaymentDto payment,
            String createdAt,
            String createdBy
    ) {}

    public record UsageDetail(
            long used,
            long limit
    ) {}

    public record UsageDetailGB(
            double usedGB,
            double limitGB
    ) {}

    public record SubscriptionUsageResponse(
            Long organizationId,
            Long subscriptionId,
            Map<String, Object> limits,
            Map<String, Object> usage,
            Map<String, Object> remaining,
            Map<String, Double> utilizationPercentage,
            String lastCalculatedAt
    ) {}

    public record InvoiceResponse(
            Long id,
            Long subscriptionId,
            String invoiceNumber,
            BigDecimal amount,
            String currency,
            BigDecimal tax,
            BigDecimal discount,
            String status,
            String issuedAt,
            String dueAt,
            String paidAt
    ) {}

    public record SubscriptionHistoryResponse(
            Long id,
            Long subscriptionId,
            String action,
            String oldPlan,
            String newPlan,
            String oldStatus,
            String newStatus,
            String performedBy,
            String performedAt,
            String remarks
    ) {}
}
