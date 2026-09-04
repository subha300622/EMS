package com.example.ems.expense.dto;

import java.time.LocalDateTime;

public record ExpenseApprovalResponse(
    Long expenseId,
    String status,
    Long approvedBy,
    LocalDateTime approvedAt,
    String nextStage
) {}
