package com.example.ems.common.dto.manager;

public record TeamTodayDto(
    Long employeeId,
    String name,
    String avatar,
    AttendanceStatus status
) {}
