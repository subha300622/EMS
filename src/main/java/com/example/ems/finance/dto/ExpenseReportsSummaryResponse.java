package com.example.ems.finance.dto;

import java.math.BigDecimal;

public class ExpenseReportsSummaryResponse {
    private long totalExpenses;
    private long approvedExpenses;
    private long pendingExpenses;
    private long rejectedExpenses;
    private BigDecimal totalAmount;
    private BigDecimal approvedAmount;
    private double averageApprovalDays;

    public ExpenseReportsSummaryResponse() {}

    public ExpenseReportsSummaryResponse(long totalExpenses, long approvedExpenses, long pendingExpenses, long rejectedExpenses, BigDecimal totalAmount, BigDecimal approvedAmount, double averageApprovalDays) {
        this.totalExpenses = totalExpenses;
        this.approvedExpenses = approvedExpenses;
        this.pendingExpenses = pendingExpenses;
        this.rejectedExpenses = rejectedExpenses;
        this.totalAmount = totalAmount;
        this.approvedAmount = approvedAmount;
        this.averageApprovalDays = averageApprovalDays;
    }

    public long getTotalExpenses() {
        return totalExpenses;
    }

    public void setTotalExpenses(long totalExpenses) {
        this.totalExpenses = totalExpenses;
    }

    public long getApprovedExpenses() {
        return approvedExpenses;
    }

    public void setApprovedExpenses(long approvedExpenses) {
        this.approvedExpenses = approvedExpenses;
    }

    public long getPendingExpenses() {
        return pendingExpenses;
    }

    public void setPendingExpenses(long pendingExpenses) {
        this.pendingExpenses = pendingExpenses;
    }

    public long getRejectedExpenses() {
        return rejectedExpenses;
    }

    public void setRejectedExpenses(long rejectedExpenses) {
        this.rejectedExpenses = rejectedExpenses;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getApprovedAmount() {
        return approvedAmount;
    }

    public void setApprovedAmount(BigDecimal approvedAmount) {
        this.approvedAmount = approvedAmount;
    }

    public BigDecimal getTotalApprovedAmount() {
        return approvedAmount;
    }

    public double getAverageApprovalDays() {
        return averageApprovalDays;
    }

    public void setAverageApprovalDays(double averageApprovalDays) {
        this.averageApprovalDays = averageApprovalDays;
    }
}
