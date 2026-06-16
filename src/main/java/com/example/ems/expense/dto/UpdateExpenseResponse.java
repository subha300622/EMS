package com.example.ems.expense.dto;

import java.time.LocalDateTime;

public class UpdateExpenseResponse {
    private Long expenseId;
    private String status;
    private LocalDateTime updatedAt;
    private String message;

    public UpdateExpenseResponse() {}

    public UpdateExpenseResponse(Long expenseId, String status, LocalDateTime updatedAt, String message) {
        this.expenseId = expenseId;
        this.status = status;
        this.updatedAt = updatedAt;
        this.message = message;
    }

    public Long getExpenseId() { return expenseId; }
    public void setExpenseId(Long expenseId) { this.expenseId = expenseId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
