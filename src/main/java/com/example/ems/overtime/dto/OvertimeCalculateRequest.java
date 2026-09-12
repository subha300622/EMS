package com.example.ems.overtime.dto;

import jakarta.validation.constraints.NotNull;

public class OvertimeCalculateRequest {

    @NotNull(message = "attendanceId is mandatory")
    private Long attendanceId;

    public OvertimeCalculateRequest() {}

    public OvertimeCalculateRequest(Long attendanceId) {
        this.attendanceId = attendanceId;
    }

    public Long getAttendanceId() {
        return attendanceId;
    }

    public void setAttendanceId(Long attendanceId) {
        this.attendanceId = attendanceId;
    }
}
