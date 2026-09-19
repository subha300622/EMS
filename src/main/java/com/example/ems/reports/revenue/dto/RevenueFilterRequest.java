package com.example.ems.reports.revenue.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "Revenue reporting filter criteria")
public class RevenueFilterRequest {
    @Schema(description = "Start Date (YYYY-MM-DD)", example = "2026-01-01")
    private String from;

    @Schema(description = "End Date (YYYY-MM-DD)", example = "2026-12-31")
    private String to;

    @Schema(description = "Organization ID", example = "10")
    private Long organizationId;

    @Schema(description = "Subscription Plan Name", example = "ENTERPRISE")
    private String subscriptionPlan;

    @Schema(description = "Payment Status", example = "SUCCESS")
    private String paymentStatus;

    @Schema(description = "Invoice Status", example = "PAID")
    private String invoiceStatus;

    @Schema(description = "Currency Code", example = "USD")
    private String currency;

    @Schema(description = "Payment Gateway", example = "RAZORPAY")
    private String gateway;

    @Schema(description = "Payment Method", example = "CREDIT_CARD")
    private String paymentMethod;

    @Schema(description = "Billing Cycle", example = "ANNUAL")
    private String billingCycle;

    @Schema(description = "Country", example = "United States")
    private String country;

    @Schema(description = "Industry", example = "Technology")
    private String industry;

    @Schema(description = "Auto Renewal Flag", example = "true")
    private Boolean autoRenewal;

    @Schema(description = "Minimum Transaction Amount", example = "10.00")
    private BigDecimal minAmount;

    @Schema(description = "Maximum Transaction Amount", example = "10000.00")
    private BigDecimal maxAmount;

    @Schema(description = "Pagination page index (0-based)", example = "0")
    private int page = 0;

    @Schema(description = "Pagination page size", example = "10")
    private int size = 10;

    @Schema(description = "Sort By field", example = "id")
    private String sortBy;

    @Schema(description = "Sort direction (asc/desc)", example = "desc")
    private String direction;

    public RevenueFilterRequest() {}

    public String getFrom() { return from; }
    public void setFrom(String from) { this.from = from; }

    public String getTo() { return to; }
    public void setTo(String to) { this.to = to; }

    public Long getOrganizationId() { return organizationId; }
    public void setOrganizationId(Long organizationId) { this.organizationId = organizationId; }

    public String getSubscriptionPlan() { return subscriptionPlan; }
    public void setSubscriptionPlan(String subscriptionPlan) { this.subscriptionPlan = subscriptionPlan; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public String getInvoiceStatus() { return invoiceStatus; }
    public void setInvoiceStatus(String invoiceStatus) { this.invoiceStatus = invoiceStatus; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getGateway() { return gateway; }
    public void setGateway(String gateway) { this.gateway = gateway; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getBillingCycle() { return billingCycle; }
    public void setBillingCycle(String billingCycle) { this.billingCycle = billingCycle; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getIndustry() { return industry; }
    public void setIndustry(String industry) { this.industry = industry; }

    public Boolean getAutoRenewal() { return autoRenewal; }
    public void setAutoRenewal(Boolean autoRenewal) { this.autoRenewal = autoRenewal; }

    public BigDecimal getMinAmount() { return minAmount; }
    public void setMinAmount(BigDecimal minAmount) { this.minAmount = minAmount; }

    public BigDecimal getMaxAmount() { return maxAmount; }
    public void setMaxAmount(BigDecimal maxAmount) { this.maxAmount = maxAmount; }

    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }

    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }

    public String getSortBy() { return sortBy; }
    public void setSortBy(String sortBy) { this.sortBy = sortBy; }

    public String getDirection() { return direction; }
    public void setDirection(String direction) { this.direction = direction; }
}
