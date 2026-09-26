package com.example.ems.reports.revenue.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "Revenue invoice detail record response")
public class RevenueInvoiceResponse {
    @Schema(description = "Invoice Number", example = "INV-001")
    private String invoiceNumber;

    @Schema(description = "Organization ID", example = "10")
    private Long organizationId;

    @Schema(description = "Organization Name", example = "Acme Corp")
    private String organizationName;

    @Schema(description = "Subscription Plan", example = "ENTERPRISE")
    private String plan;

    @Schema(description = "Issue Date", example = "2026-07-03")
    private String issueDate;

    @Schema(description = "Due Date", example = "2026-08-03")
    private String dueDate;

    @Schema(description = "Invoice Status", example = "PAID")
    private String status;

    @Schema(description = "Subtotal Amount", example = "100.00")
    private BigDecimal subtotal;

    @Schema(description = "Tax Amount", example = "0.00")
    private BigDecimal tax;

    @Schema(description = "Discount Amount", example = "0.00")
    private BigDecimal discount;

    @Schema(description = "Grand Total Amount", example = "100.00")
    private BigDecimal grandTotal;

    public RevenueInvoiceResponse() {}

    public RevenueInvoiceResponse(String invoiceNumber, Long organizationId, String organizationName, String plan, String issueDate, String dueDate, String status, BigDecimal subtotal, BigDecimal tax, BigDecimal discount, BigDecimal grandTotal) {
        this.invoiceNumber = invoiceNumber;
        this.organizationId = organizationId;
        this.organizationName = organizationName;
        this.plan = plan;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.status = status;
        this.subtotal = subtotal;
        this.tax = tax;
        this.discount = discount;
        this.grandTotal = grandTotal;
    }

    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }

    public Long getOrganizationId() { return organizationId; }
    public void setOrganizationId(Long organizationId) { this.organizationId = organizationId; }

    public String getOrganizationName() { return organizationName; }
    public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }

    public String getPlan() { return plan; }
    public void setPlan(String plan) { this.plan = plan; }

    public String getIssueDate() { return issueDate; }
    public void setIssueDate(String issueDate) { this.issueDate = issueDate; }

    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public BigDecimal getTax() { return tax; }
    public void setTax(BigDecimal tax) { this.tax = tax; }

    public BigDecimal getDiscount() { return discount; }
    public void setDiscount(BigDecimal discount) { this.discount = discount; }

    public BigDecimal getGrandTotal() { return grandTotal; }
    public void setGrandTotal(BigDecimal grandTotal) { this.grandTotal = grandTotal; }
}
