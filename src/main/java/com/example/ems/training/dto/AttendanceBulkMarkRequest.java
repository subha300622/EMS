package com.example.ems.training.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class AttendanceBulkMarkRequest {

    private Long sessionId;

    @NotEmpty(message = "Attendance items list cannot be empty")
    private List<AttendanceItemRequest> items;

    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }

    public List<AttendanceItemRequest> getItems() { return items; }
    public void setItems(List<AttendanceItemRequest> items) { this.items = items; }
}
