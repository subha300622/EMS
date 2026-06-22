package com.example.ems.employee.dto;

import java.math.BigDecimal;

public record TeamMemberListItemDto(
    Long employeeId,
    String name,
    String designation,
    int attendance,
    long leaveBalance,
    double performanceRating,
    BigDecimal ctc,
    String status
) {}
