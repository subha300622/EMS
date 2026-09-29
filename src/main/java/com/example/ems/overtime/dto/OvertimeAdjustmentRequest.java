package com.example.ems.overtime.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class OvertimeAdjustmentRequest {

    @NotNull(message = "adjustedOtMinutes is mandatory")
    @Min(value = 0, message = "adjustedOtMinutes cannot be negative")
    private Integer adjustedOtMinutes;

    @NotBlank(message = "reason is mandatory")
    @Size(min = 3, max = 1000, message = "reason must be between 3 and 1000 characters")
    private String reason;

    public OvertimeAdjustmentRequest() {}

    public OvertimeAdjustmentRequest(Integer adjustedOtMinutes, String reason) {
        this.adjustedOtMinutes = adjustedOtMinutes;
        this.reason = reason;
    }

    public Integer getAdjustedOtMinutes() {
        return adjustedOtMinutes;
    }

    public void setAdjustedOtMinutes(Integer adjustedOtMinutes) {
        this.adjustedOtMinutes = adjustedOtMinutes;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
