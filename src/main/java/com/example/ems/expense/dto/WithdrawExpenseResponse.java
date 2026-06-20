package com.example.ems.expense.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public class WithdrawExpenseResponse {
    @Schema(example = "1")
    private Long expenseId;
    @Schema(example = "ACTIVE")
    private String status;
    @Schema(example = "2026-06-19T10:00:00")
    private LocalDateTime withdrawnAt;
    @Schema(example = "string")
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
