package com.example.ems.expense.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PendingApprovalsResponse(
    Long expenseId,
    EmployeeInfo employee,
    String title,
    BigDecimal amount,
    String category,
    LocalDateTime submittedAt,
    List<String> receiptUrls,
    String status
) {
    public record EmployeeInfo(
        Long id,
        String name
    ) {}
}
