package com.example.ems.expense.dto;

import java.time.LocalDateTime;

public record ExpenseSubmitResponse(
    Long expenseId,
    String status,
    Long submittedBy,
    LocalDateTime submittedAt,
    NextApproverInfo nextApprover
) {
    public record NextApproverInfo(
        Long id,
        String name
    ) {}
}
