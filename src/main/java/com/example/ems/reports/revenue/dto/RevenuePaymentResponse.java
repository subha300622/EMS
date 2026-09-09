package com.example.ems.reports.revenue.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "Revenue payments transaction record response")
public class RevenuePaymentResponse {
    @Schema(description = "Payment ID", example = "1")
    private Long paymentId;

    @Schema(description = "Organization ID", example = "10")
    private Long organizationId;

    @Schema(description = "Organization Name", example = "Acme Corp")
    private String organizationName;

    @Schema(description = "Subscription Plan Name", example = "ENTERPRISE")
    private String plan;

    @Schema(description = "Invoice Number", example = "INV-001")
    private String invoiceNumber;

    @Schema(description = "Payment Gateway Name", example = "RAZORPAY")
    private String gateway;

    @Schema(description = "Payment Status", example = "SUCCESS")
    private String status;

    @Schema(description = "Currency Code", example = "USD")
    private String currency;

    @Schema(description = "Payment Amount", example = "100.00")
    private BigDecimal amount;

    @Schema(description = "Tax Amount", example = "0.00")
    private BigDecimal tax;

    @Schema(description = "Discount Amount", example = "0.00")
    private BigDecimal discount;

    @Schema(description = "Net Amount", example = "100.00")
    private BigDecimal netAmount;

    @Schema(description = "Paid Date/Time", example = "2026-07-03")
    private String paidDate;

    public RevenuePaymentResponse() {}

    public RevenuePaymentResponse(Long paymentId, Long organizationId, String organizationName, String plan, String invoiceNumber, String gateway, String status, String currency, BigDecimal amount, BigDecimal tax, BigDecimal discount, BigDecimal netAmount, String paidDate) {
        this.paymentId = paymentId;
        this.organizationId = organizationId;
        this.organizationName = organizationName;
        this.plan = plan;
        this.invoiceNumber = invoiceNumber;
        this.gateway = gateway;
        this.status = status;
        this.currency = currency;
        this.amount = amount;
        this.tax = tax;
        this.discount = discount;
        this.netAmount = netAmount;
        this.paidDate = paidDate;
    }

    public Long getPaymentId() { return paymentId; }
    public void setPaymentId(Long paymentId) { this.paymentId = paymentId; }

    public Long getOrganizationId() { return organizationId; }
    public void setOrganizationId(Long organizationId) { this.organizationId = organizationId; }

    public String getOrganizationName() { return organizationName; }
    public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }

    public String getPlan() { return plan; }
    public void setPlan(String plan) { this.plan = plan; }

    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }

    public String getGateway() { return gateway; }
    public void setGateway(String gateway) { this.gateway = gateway; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public BigDecimal getTax() { return tax; }
    public void setTax(BigDecimal tax) { this.tax = tax; }

    public BigDecimal getDiscount() { return discount; }
    public void setDiscount(BigDecimal discount) { this.discount = discount; }

    public BigDecimal getNetAmount() { return netAmount; }
    public void setNetAmount(BigDecimal netAmount) { this.netAmount = netAmount; }

    public String getPaidDate() { return paidDate; }
    public void setPaidDate(String paidDate) { this.paidDate = paidDate; }
}
