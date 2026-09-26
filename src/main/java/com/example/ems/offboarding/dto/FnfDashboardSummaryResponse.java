package com.example.ems.offboarding.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.Map;

@Schema(description = "F&F Operational Dashboard Summary Statistics")
public class FnfDashboardSummaryResponse {

    @Schema(description = "Total F&F settlement records", example = "42")
    private Long totalSettlements;

    @Schema(description = "Count of settlements in draft or calculated state", example = "8")
    private Long draftSettlements;

    @Schema(description = "Count of settlements awaiting departmental approvals", example = "12")
    private Long pendingApprovals;

    @Schema(description = "Count of approved settlements awaiting payment disbursement", example = "5")
    private Long paymentPending;

    @Schema(description = "Count of successfully paid settlements", example = "15")
    private Long paidSettlements;

    @Schema(description = "Count of finalized and closed settlements", example = "14")
    private Long finalizedSettlements;

    @Schema(description = "Total disbursed settlement funds in currency", example = "1250000.00")
    private BigDecimal totalDisbursedAmount;

    @Schema(description = "Total pending settlement payable liabilities", example = "420500.00")
    private BigDecimal totalPendingAmount;

    @Schema(description = "Breakdown counts by lifecycle status", example = "{\"CALCULATED\": 5, \"SUBMITTED\": 7, \"FINANCE_APPROVAL_PENDING\": 5, \"SETTLEMENT_APPROVED\": 5, \"PAYMENT_RELEASED\": 15, \"FINALIZED\": 14}")
    private Map<String, Long> statusCounts;

    public FnfDashboardSummaryResponse() {}

    public Long getTotalSettlements() { return totalSettlements; }
    public void setTotalSettlements(Long totalSettlements) { this.totalSettlements = totalSettlements; }

    public Long getDraftSettlements() { return draftSettlements; }
    public void setDraftSettlements(Long draftSettlements) { this.draftSettlements = draftSettlements; }

    public Long getPendingApprovals() { return pendingApprovals; }
    public void setPendingApprovals(Long pendingApprovals) { this.pendingApprovals = pendingApprovals; }

    public Long getPaymentPending() { return paymentPending; }
    public void setPaymentPending(Long paymentPending) { this.paymentPending = paymentPending; }

    public Long getPaidSettlements() { return paidSettlements; }
    public void setPaidSettlements(Long paidSettlements) { this.paidSettlements = paidSettlements; }

    public Long getFinalizedSettlements() { return finalizedSettlements; }
    public void setFinalizedSettlements(Long finalizedSettlements) { this.finalizedSettlements = finalizedSettlements; }

    public BigDecimal getTotalDisbursedAmount() { return totalDisbursedAmount; }
    public void setTotalDisbursedAmount(BigDecimal totalDisbursedAmount) { this.totalDisbursedAmount = totalDisbursedAmount; }

    public BigDecimal getTotalPendingAmount() { return totalPendingAmount; }
    public void setTotalPendingAmount(BigDecimal totalPendingAmount) { this.totalPendingAmount = totalPendingAmount; }

    public Map<String, Long> getStatusCounts() { return statusCounts; }
    public void setStatusCounts(Map<String, Long> statusCounts) { this.statusCounts = statusCounts; }
}
