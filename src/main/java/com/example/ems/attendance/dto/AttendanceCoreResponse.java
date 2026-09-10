package com.example.ems.attendance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Schema(description = "Canonical Attendance Core State and Lifecycle Response")
public class AttendanceCoreResponse {

    @Schema(description = "Attendance record identifier (null if NOT_CHECKED_IN)", example = "1001")
    private Long attendanceId;

    @Schema(description = "Internal database employee identifier", example = "125")
    private Long employeeId;

    @Schema(description = "Employee full name", example = "John Doe")
    private String employeeName;

    @Schema(description = "Employee business code", example = "EMP-0125")
    private String employeeIdentifier;

    @Schema(description = "Attendance working date (YYYY-MM-DD)", example = "2026-09-10")
    private LocalDate attendanceDate;

    @Schema(description = "Lifecycle attendance state", allowableValues = {"NOT_CHECKED_IN", "WORKING", "ON_BREAK", "COMPLETED"}, example = "WORKING")
    private String status;

    @Schema(description = "Check-in timestamp in ISO-8601 UTC", example = "2026-09-10T09:02:15Z")
    private Instant checkInTime;

    @Schema(description = "Check-out timestamp in ISO-8601 UTC (null until checked out)", example = "2026-09-10T18:04:22Z")
    private Instant checkOutTime;

    @Schema(description = "Total accumulated break minutes", example = "45")
    private Integer totalBreakMinutes = 0;

    @Schema(description = "Total calculated working minutes (null until check-out is completed)", example = "495")
    private Integer totalWorkingMinutes;

    @Schema(description = "Indicates whether the employee currently has an open break", example = "false")
    private boolean activeBreak;

    @Schema(description = "List of all breaks taken today")
    private List<AttendanceBreakDto> breaks = new ArrayList<>();

    public AttendanceCoreResponse() {}

    public static AttendanceCoreResponse notCheckedIn(Long employeeId, String employeeName, String employeeIdentifier, LocalDate date) {
        AttendanceCoreResponse response = new AttendanceCoreResponse();
        response.setAttendanceId(null);
        response.setEmployeeId(employeeId);
        response.setEmployeeName(employeeName);
        response.setEmployeeIdentifier(employeeIdentifier);
        response.setAttendanceDate(date);
        response.setStatus("NOT_CHECKED_IN");
        response.setCheckInTime(null);
        response.setCheckOutTime(null);
        response.setTotalBreakMinutes(0);
        response.setTotalWorkingMinutes(0);
        response.setActiveBreak(false);
        response.setBreaks(new ArrayList<>());
        return response;
    }

    public Long getAttendanceId() {
        return attendanceId;
    }

    public void setAttendanceId(Long attendanceId) {
        this.attendanceId = attendanceId;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getEmployeeIdentifier() {
        return employeeIdentifier;
    }

    public void setEmployeeIdentifier(String employeeIdentifier) {
        this.employeeIdentifier = employeeIdentifier;
    }

    public LocalDate getAttendanceDate() {
        return attendanceDate;
    }

    public void setAttendanceDate(LocalDate attendanceDate) {
        this.attendanceDate = attendanceDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(Instant checkInTime) {
        this.checkInTime = checkInTime;
    }

    public Instant getCheckOutTime() {
        return checkOutTime;
    }

    public void setCheckOutTime(Instant checkOutTime) {
        this.checkOutTime = checkOutTime;
    }

    public Integer getTotalBreakMinutes() {
        return totalBreakMinutes;
    }

    public void setTotalBreakMinutes(Integer totalBreakMinutes) {
        this.totalBreakMinutes = totalBreakMinutes;
    }

    public Integer getTotalWorkingMinutes() {
        return totalWorkingMinutes;
    }

    public void setTotalWorkingMinutes(Integer totalWorkingMinutes) {
        this.totalWorkingMinutes = totalWorkingMinutes;
    }

    public boolean isActiveBreak() {
        return activeBreak;
    }

    public void setActiveBreak(boolean activeBreak) {
        this.activeBreak = activeBreak;
    }

    public List<AttendanceBreakDto> getBreaks() {
        return breaks;
    }

    public void setBreaks(List<AttendanceBreakDto> breaks) {
        this.breaks = breaks;
    }
}
