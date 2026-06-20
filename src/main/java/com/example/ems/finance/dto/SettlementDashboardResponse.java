package com.example.ems.finance.dto;

import java.math.BigDecimal;

public class SettlementDashboardResponse {
    private long pendingReview;
    private long approved;
    private long processed;
    private long rejected;
    private BigDecimal totalSettlementAmount;
    private BigDecimal avgSettlementAmount;
    private BigDecimal pendingDisbursementAmount;

    public SettlementDashboardResponse() {}

    public SettlementDashboardResponse(long pendingReview, long approved, long processed, long rejected, BigDecimal totalSettlementAmount, BigDecimal avgSettlementAmount, BigDecimal pendingDisbursementAmount) {
        this.pendingReview = pendingReview;
        this.approved = approved;
        this.processed = processed;
        this.rejected = rejected;
        this.totalSettlementAmount = totalSettlementAmount;
        this.avgSettlementAmount = avgSettlementAmount;
        this.pendingDisbursementAmount = pendingDisbursementAmount;
    }

    public long getPendingReview() { return pendingReview; }
    public void setPendingReview(long pendingReview) { this.pendingReview = pendingReview; }

    public long getApproved() { return approved; }
    public void setApproved(long approved) { this.approved = approved; }

    public long getProcessed() { return processed; }
    public void setProcessed(long processed) { this.processed = processed; }

    public long getRejected() { return rejected; }
    public void setRejected(long rejected) { this.rejected = rejected; }

    public BigDecimal getTotalSettlementAmount() { return totalSettlementAmount; }
    public void setTotalSettlementAmount(BigDecimal totalSettlementAmount) { this.totalSettlementAmount = totalSettlementAmount; }

    public BigDecimal getAvgSettlementAmount() { return avgSettlementAmount; }
    public void setAvgSettlementAmount(BigDecimal avgSettlementAmount) { this.avgSettlementAmount = avgSettlementAmount; }

    public BigDecimal getPendingDisbursementAmount() { return pendingDisbursementAmount; }
    public void setPendingDisbursementAmount(BigDecimal pendingDisbursementAmount) { this.pendingDisbursementAmount = pendingDisbursementAmount; }
}
