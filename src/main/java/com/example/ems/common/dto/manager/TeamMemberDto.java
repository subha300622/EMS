package com.example.ems.common.dto.manager;

public record TeamMemberDto(
    Long employeeId,
    String name,
    String designation,
    Integer attendance,
    ShiftType shift,
    AttendanceStatus status
) {}
