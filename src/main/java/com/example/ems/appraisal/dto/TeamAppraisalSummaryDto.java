package com.example.ems.appraisal.dto;

import java.math.BigDecimal;

public class TeamAppraisalSummaryDto {
    private long eligibleCount;
    private long totalCount;
    private long pendingFinanceCount;
    private long approvedFinanceCount;
    private double avgAttendance;
    private double avgPerfScore;
    private BigDecimal payrollImpact;

    public TeamAppraisalSummaryDto() {}

    public TeamAppraisalSummaryDto(long eligibleCount, long totalCount, long pendingFinanceCount, long approvedFinanceCount, double avgAttendance, double avgPerfScore, BigDecimal payrollImpact) {
        this.eligibleCount = eligibleCount;
        this.totalCount = totalCount;
        this.pendingFinanceCount = pendingFinanceCount;
        this.approvedFinanceCount = approvedFinanceCount;
        this.avgAttendance = avgAttendance;
        this.avgPerfScore = avgPerfScore;
        this.payrollImpact = payrollImpact;
    }

    public long getEligibleCount() { return eligibleCount; }
    public void setEligibleCount(long eligibleCount) { this.eligibleCount = eligibleCount; }

    public long getTotalCount() { return totalCount; }
    public void setTotalCount(long totalCount) { this.totalCount = totalCount; }

    public long getPendingFinanceCount() { return pendingFinanceCount; }
    public void setPendingFinanceCount(long pendingFinanceCount) { this.pendingFinanceCount = pendingFinanceCount; }

    public long getApprovedFinanceCount() { return approvedFinanceCount; }
    public void setApprovedFinanceCount(long approvedFinanceCount) { this.approvedFinanceCount = approvedFinanceCount; }

    public double getAvgAttendance() { return avgAttendance; }
    public void setAvgAttendance(double avgAttendance) { this.avgAttendance = avgAttendance; }

    public double getAvgPerfScore() { return avgPerfScore; }
    public void setAvgPerfScore(double avgPerfScore) { this.avgPerfScore = avgPerfScore; }

    public BigDecimal getPayrollImpact() { return payrollImpact; }
    public void setPayrollImpact(BigDecimal payrollImpact) { this.payrollImpact = payrollImpact; }
}
