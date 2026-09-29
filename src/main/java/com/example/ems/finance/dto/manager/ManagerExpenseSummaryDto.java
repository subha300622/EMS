package com.example.ems.finance.dto.manager;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.math.BigDecimal;

@Schema(description = "Manager Expense Summary metrics")
public class ManagerExpenseSummaryDto implements Serializable {

    @Schema(description = "Pending expense claims count", example = "6")
    private Long pendingClaims;

    @Schema(description = "Total pending expense claims amount", example = "48500")
    private BigDecimal pendingAmount;

    @Schema(description = "Approved expense claims count this month", example = "18")
    private Long approvedThisMonth;

    @Schema(description = "Total approved expense claims amount this month", example = "126000")
    private BigDecimal approvedAmount;

    public ManagerExpenseSummaryDto() {}

    public ManagerExpenseSummaryDto(Long pendingClaims, BigDecimal pendingAmount, Long approvedThisMonth, BigDecimal approvedAmount) {
        this.pendingClaims = pendingClaims;
        this.pendingAmount = pendingAmount;
        this.approvedThisMonth = approvedThisMonth;
        this.approvedAmount = approvedAmount;
    }

    public Long getPendingClaims() {
        return pendingClaims;
    }

    public void setPendingClaims(Long pendingClaims) {
        this.pendingClaims = pendingClaims;
    }

    public BigDecimal getPendingAmount() {
        return pendingAmount;
    }

    public void setPendingAmount(BigDecimal pendingAmount) {
        this.pendingAmount = pendingAmount;
    }

    public Long getApprovedThisMonth() {
        return approvedThisMonth;
    }

    public void setApprovedThisMonth(Long approvedThisMonth) {
        this.approvedThisMonth = approvedThisMonth;
    }

    public BigDecimal getApprovedAmount() {
        return approvedAmount;
    }

    public void setApprovedAmount(BigDecimal approvedAmount) {
        this.approvedAmount = approvedAmount;
    }
}
