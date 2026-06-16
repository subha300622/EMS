package com.example.ems.expense.dto;

import java.time.LocalDateTime;

public class CreateExpenseResponse {
    private Long expenseId;
    private String expenseNumber;
    private String status;
    private LocalDateTime submittedAt;
    private String message;

    public CreateExpenseResponse() {}

    public CreateExpenseResponse(Long expenseId, String expenseNumber, String status, LocalDateTime submittedAt, String message) {
        this.expenseId = expenseId;
        this.expenseNumber = expenseNumber;
        this.status = status;
        this.submittedAt = submittedAt;
        this.message = message;
    }

    public Long getExpenseId() { return expenseId; }
    public void setExpenseId(Long expenseId) { this.expenseId = expenseId; }

    public String getExpenseNumber() { return expenseNumber; }
    public void setExpenseNumber(String expenseNumber) { this.expenseNumber = expenseNumber; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
