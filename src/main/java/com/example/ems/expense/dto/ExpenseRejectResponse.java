package com.example.ems.expense.dto;

public record ExpenseRejectResponse(
    Long expenseId,
    String status
) {}
