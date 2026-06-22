package com.example.ems.employee.dto;

public record EmployeeAttendanceSummaryDto(
    double attendancePercentage,
    long presentDays,
    long absentDays,
    long lateDays
) {}
