package com.example.ems.reports.revenue.dto;

import java.math.BigDecimal;

public class RevenueInvoiceResponse {
    private String invoiceNumber;
    private Long organizationId;
    private String organizationName;
    private String plan;
    private String issueDate;
    private String dueDate;
    private String status;
    private BigDecimal subtotal;
    private BigDecimal tax;
    private BigDecimal discount;
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
