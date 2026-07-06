package com.example.ems.reports.subscription.dto;

import com.example.ems.organization.dto.SubscriptionDtos.*;
import java.math.BigDecimal;

public class SubscriptionDetailResponse {
    private Long subscriptionId;
    private Long organizationId;
    private String organizationName;
    private String status;
    private PlanDto plan;
    private BillingDto billing;
    private DurationDto duration;
    private LimitsDto limits;
    private PaymentDto payment;
    private String createdAt;
    private String createdBy;

    public SubscriptionDetailResponse() {}

    public SubscriptionDetailResponse(Long subscriptionId, Long organizationId, String organizationName, String status, 
                                      PlanDto plan, BillingDto billing, DurationDto duration, LimitsDto limits, 
                                      PaymentDto payment, String createdAt, String createdBy) {
        this.subscriptionId = subscriptionId;
        this.organizationId = organizationId;
        this.organizationName = organizationName;
        this.status = status;
        this.plan = plan;
        this.billing = billing;
        this.duration = duration;
        this.limits = limits;
        this.payment = payment;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
    }

    public Long getSubscriptionId() { return subscriptionId; }
    public void setSubscriptionId(Long subscriptionId) { this.subscriptionId = subscriptionId; }

    public Long getOrganizationId() { return organizationId; }
    public void setOrganizationId(Long organizationId) { this.organizationId = organizationId; }

    public String getOrganizationName() { return organizationName; }
    public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public PlanDto getPlan() { return plan; }
    public void setPlan(PlanDto plan) { this.plan = plan; }

    public BillingDto getBilling() { return billing; }
    public void setBilling(BillingDto billing) { this.billing = billing; }

    public DurationDto getDuration() { return duration; }
    public void setDuration(DurationDto duration) { this.duration = duration; }

    public LimitsDto getLimits() { return limits; }
    public void setLimits(LimitsDto limits) { this.limits = limits; }

    public PaymentDto getPayment() { return payment; }
    public void setPayment(PaymentDto payment) { this.payment = payment; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
}
