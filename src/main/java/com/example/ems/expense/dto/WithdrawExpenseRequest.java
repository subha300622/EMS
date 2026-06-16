package com.example.ems.expense.dto;

import jakarta.validation.constraints.NotBlank;

public class WithdrawExpenseRequest {

    @NotBlank(message = "Reason is required to withdraw a claim")
    private String reason;

    public WithdrawExpenseRequest() {}

    public WithdrawExpenseRequest(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
