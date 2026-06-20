package com.example.ems.finance.dto;

import java.math.BigDecimal;

public class ExpenseDashboardResponse {
    private long totalPending;
    private BigDecimal pendingAmount;
    private long approvedThisMonth;
    private BigDecimal approvedAmountThisMonth;
    private long rejected;
    private BigDecimal rejectedAmount;
    private double averageApprovalDays;

    public ExpenseDashboardResponse() {}

    public ExpenseDashboardResponse(long totalPending, BigDecimal pendingAmount, long approvedThisMonth, BigDecimal approvedAmountThisMonth, long rejected, BigDecimal rejectedAmount, double averageApprovalDays) {
        this.totalPending = totalPending;
        this.pendingAmount = pendingAmount;
        this.approvedThisMonth = approvedThisMonth;
        this.approvedAmountThisMonth = approvedAmountThisMonth;
        this.rejected = rejected;
        this.rejectedAmount = rejectedAmount;
        this.averageApprovalDays = averageApprovalDays;
    }

    public long getTotalPending() {
        return totalPending;
    }

    public void setTotalPending(long totalPending) {
        this.totalPending = totalPending;
    }

    public BigDecimal getPendingAmount() {
        return pendingAmount;
    }

    public void setPendingAmount(BigDecimal pendingAmount) {
        this.pendingAmount = pendingAmount;
    }

    public long getApprovedThisMonth() {
        return approvedThisMonth;
    }

    public void setApprovedThisMonth(long approvedThisMonth) {
        this.approvedThisMonth = approvedThisMonth;
    }

    public BigDecimal getApprovedAmountThisMonth() {
        return approvedAmountThisMonth;
    }

    public void setApprovedAmountThisMonth(BigDecimal approvedAmountThisMonth) {
        this.approvedAmountThisMonth = approvedAmountThisMonth;
    }

    public long getRejected() {
        return rejected;
    }

    public void setRejected(long rejected) {
        this.rejected = rejected;
    }

    public BigDecimal getRejectedAmount() {
        return rejectedAmount;
    }

    public void setRejectedAmount(BigDecimal rejectedAmount) {
        this.rejectedAmount = rejectedAmount;
    }

    public double getAverageApprovalDays() {
        return averageApprovalDays;
    }

    public void setAverageApprovalDays(double averageApprovalDays) {
        this.averageApprovalDays = averageApprovalDays;
    }
}
