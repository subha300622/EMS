package com.example.ems.expense.dto;

import java.time.LocalDateTime;

public class WithdrawExpenseResponse {
    private Long expenseId;
    private String status;
    private LocalDateTime withdrawnAt;
    private String message;

    public WithdrawExpenseResponse() {}

    public WithdrawExpenseResponse(Long expenseId, String status, LocalDateTime withdrawnAt, String message) {
        this.expenseId = expenseId;
        this.status = status;
        this.withdrawnAt = withdrawnAt;
        this.message = message;
    }

    public Long getExpenseId() { return expenseId; }
    public void setExpenseId(Long expenseId) { this.expenseId = expenseId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getWithdrawnAt() { return withdrawnAt; }
    public void setWithdrawnAt(LocalDateTime withdrawnAt) { this.withdrawnAt = withdrawnAt; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
