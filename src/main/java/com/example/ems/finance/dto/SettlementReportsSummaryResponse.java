package com.example.ems.finance.dto;

import java.math.BigDecimal;

public class SettlementReportsSummaryResponse {
    private long totalProcessed;
    private BigDecimal totalAmountPaid;
    private BigDecimal averageSettlement;
    private long pendingCases;

    public SettlementReportsSummaryResponse() {}

    public SettlementReportsSummaryResponse(long totalProcessed, BigDecimal totalAmountPaid, BigDecimal averageSettlement, long pendingCases) {
        this.totalProcessed = totalProcessed;
        this.totalAmountPaid = totalAmountPaid;
        this.averageSettlement = averageSettlement;
        this.pendingCases = pendingCases;
    }

    public long getTotalProcessed() { return totalProcessed; }
    public void setTotalProcessed(long totalProcessed) { this.totalProcessed = totalProcessed; }

    public BigDecimal getTotalAmountPaid() { return totalAmountPaid; }
    public void setTotalAmountPaid(BigDecimal totalAmountPaid) { this.totalAmountPaid = totalAmountPaid; }

    public BigDecimal getAverageSettlement() { return averageSettlement; }
    public void setAverageSettlement(BigDecimal averageSettlement) { this.averageSettlement = averageSettlement; }

    public long getPendingCases() { return pendingCases; }
    public void setPendingCases(long pendingCases) { this.pendingCases = pendingCases; }
}
