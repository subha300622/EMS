package com.example.ems.bonus.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public class BonusAdjustmentRequest {

    @NotNull(message = "Adjusted amount is required")
    @PositiveOrZero(message = "Adjusted amount must be greater than or equal to zero")
    private BigDecimal adjustedAmount;

    @NotBlank(message = "Adjustment reason is mandatory")
    private String adjustmentReason;

    public BonusAdjustmentRequest() {}

    public BonusAdjustmentRequest(BigDecimal adjustedAmount, String adjustmentReason) {
        this.adjustedAmount = adjustedAmount;
        this.adjustmentReason = adjustmentReason;
    }

    public BigDecimal getAdjustedAmount() {
        return adjustedAmount;
    }

    public void setAdjustedAmount(BigDecimal adjustedAmount) {
        this.adjustedAmount = adjustedAmount;
    }

    public String getAdjustmentReason() {
        return adjustmentReason;
    }

    public void setAdjustmentReason(String adjustmentReason) {
        this.adjustmentReason = adjustmentReason;
    }
}
