package com.example.ems.attendance.dto.permission;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AttendancePermissionRejectRequest {

    @NotBlank(message = "reason is mandatory")
    @Size(min = 3, max = 1000, message = "reason must be between 3 and 1000 characters")
    private String reason;

    public AttendancePermissionRejectRequest() {}

    public AttendancePermissionRejectRequest(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
