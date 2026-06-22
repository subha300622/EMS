package com.example.ems.employee.dto;

public record EmployeePerformanceSummaryDto(
    double rating,
    long goalsCompleted,
    long goalsPending,
    String appraisalStatus
) {}
