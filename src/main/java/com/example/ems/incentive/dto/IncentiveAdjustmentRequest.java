package com.example.ems.incentive.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class IncentiveAdjustmentRequest {

    @NotNull(message = "Adjusted amount is mandatory.")
    @DecimalMin(value = "0.0", message = "Adjusted amount must be greater than or equal to 0.")
    private BigDecimal adjustedAmount;

    @NotBlank(message = "Adjustment reason is mandatory.")
    @Size(max = 1000, message = "Adjustment reason cannot exceed 1000 characters.")
    private String reason;

    public IncentiveAdjustmentRequest() {}

    public IncentiveAdjustmentRequest(BigDecimal adjustedAmount, String reason) {
        this.adjustedAmount = adjustedAmount;
        this.reason = reason;
    }

    public BigDecimal getAdjustedAmount() { return adjustedAmount; }
    public void setAdjustedAmount(BigDecimal adjustedAmount) { this.adjustedAmount = adjustedAmount; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getAdjustmentReason() { return reason; }
    public void setAdjustmentReason(String adjustmentReason) { this.reason = adjustmentReason; }
}
