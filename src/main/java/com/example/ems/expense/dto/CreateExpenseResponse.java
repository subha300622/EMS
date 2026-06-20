package com.example.ems.expense.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public class CreateExpenseResponse {
    @Schema(example = "1")
    private Long expenseId;
    @Schema(example = "string")
    private String expenseNumber;
    @Schema(example = "ACTIVE")
    private String status;
    @Schema(example = "2026-06-19T10:00:00")
    private LocalDateTime submittedAt;
    @Schema(example = "string")
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
