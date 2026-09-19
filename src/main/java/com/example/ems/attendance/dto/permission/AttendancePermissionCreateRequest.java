package com.example.ems.attendance.dto.permission;

import com.example.ems.attendance.entity.AttendancePermissionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;

public class AttendancePermissionCreateRequest {

    private Long employeeId; // Optional if submitting for oneself; mandatory if HR/Admin submits on behalf

    @NotNull(message = "attendanceDate is mandatory")
    private LocalDate attendanceDate;

    @NotNull(message = "permissionType is mandatory")
    private AttendancePermissionType permissionType;

    private Integer requestedMinutes;

    private LocalTime expectedTime;

    private LocalTime actualTime;

    @NotNull(message = "reason is mandatory")
    @Size(min = 3, max = 1000, message = "reason must be between 3 and 1000 characters")
    private String reason;

    public AttendancePermissionCreateRequest() {}

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public LocalDate getAttendanceDate() {
        return attendanceDate;
    }

    public void setAttendanceDate(LocalDate attendanceDate) {
        this.attendanceDate = attendanceDate;
    }

    public AttendancePermissionType getPermissionType() {
        return permissionType;
    }

    public void setPermissionType(AttendancePermissionType permissionType) {
        this.permissionType = permissionType;
    }

    public Integer getRequestedMinutes() {
        return requestedMinutes;
    }

    public void setRequestedMinutes(Integer requestedMinutes) {
        this.requestedMinutes = requestedMinutes;
    }

    public LocalTime getExpectedTime() {
        return expectedTime;
    }

    public void setExpectedTime(LocalTime expectedTime) {
        this.expectedTime = expectedTime;
    }

    public LocalTime getActualTime() {
        return actualTime;
    }

    public void setActualTime(LocalTime actualTime) {
        this.actualTime = actualTime;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
