package com.example.ems.expense.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ExpenseSubmitRequest(
    String title,
    String category,
    BigDecimal amount,
    String currency,
    LocalDate expenseDate,
    String description,
    Long projectId,
    List<String> receiptUrls
) {}
