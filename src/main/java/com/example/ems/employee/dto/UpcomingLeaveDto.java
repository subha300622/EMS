package com.example.ems.employee.dto;

public record UpcomingLeaveDto(
    String startDate,
    String endDate,
    String status
) {}
