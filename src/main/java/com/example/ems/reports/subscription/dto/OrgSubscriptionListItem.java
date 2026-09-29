package com.example.ems.reports.subscription.dto;

import java.math.BigDecimal;

public class OrgSubscriptionListItem {
    private Long organizationId;
    private String organizationName;
    private String plan;
    private String status;
    private String billingCycle;
    private String subscriptionStart;
    private String subscriptionEnd;
    private BigDecimal amount;
    private boolean autoRenew;

    public OrgSubscriptionListItem() {}

    public OrgSubscriptionListItem(Long organizationId, String organizationName, String plan, String status, 
                                   String billingCycle, String subscriptionStart, String subscriptionEnd, 
                                   BigDecimal amount, boolean autoRenew) {
        this.organizationId = organizationId;
        this.organizationName = organizationName;
        this.plan = plan;
        this.status = status;
        this.billingCycle = billingCycle;
        this.subscriptionStart = subscriptionStart;
        this.subscriptionEnd = subscriptionEnd;
        this.amount = amount;
        this.autoRenew = autoRenew;
    }

    public Long getOrganizationId() { return organizationId; }
    public void setOrganizationId(Long organizationId) { this.organizationId = organizationId; }

    public String getOrganizationName() { return organizationName; }
    public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }

    public String getPlan() { return plan; }
    public void setPlan(String plan) { this.plan = plan; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getBillingCycle() { return billingCycle; }
    public void setBillingCycle(String billingCycle) { this.billingCycle = billingCycle; }

    public String getSubscriptionStart() { return subscriptionStart; }
    public void setSubscriptionStart(String subscriptionStart) { this.subscriptionStart = subscriptionStart; }

    public String getSubscriptionEnd() { return subscriptionEnd; }
    public void setSubscriptionEnd(String subscriptionEnd) { this.subscriptionEnd = subscriptionEnd; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public boolean isAutoRenew() { return autoRenew; }
    public void setAutoRenew(boolean autoRenew) { this.autoRenew = autoRenew; }
}
