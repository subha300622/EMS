package com.example.ems.expense.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseResponse(
    Long expenseId,
    String title,
    BigDecimal amount,
    String status,
    String category,
    LocalDate expenseDate
) {}
