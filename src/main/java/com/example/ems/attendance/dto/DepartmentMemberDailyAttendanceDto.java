package com.example.ems.attendance.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Department Member Daily Attendance Detail")
public class DepartmentMemberDailyAttendanceDto {

    @Schema(description = "Employee database ID", example = "10")
    private Long employeeId;

    @Schema(description = "Employee unique business code", example = "EMP-001")
    private String employeeCode;

    @Schema(description = "Employee full name", example = "John Doe")
    private String fullName;

    @Schema(description = "Designation", example = "Senior Software Engineer")
    private String designation;

    @Schema(description = "Team name", example = "Backend Core Team")
    private String teamName;

    @Schema(description = "Daily attendance status", example = "PRESENT")
    private String status;

    @Schema(description = "Check-in timestamp")
    private Instant checkInTime;

    @Schema(description = "Check-out timestamp")
    private Instant checkOutTime;

    @Schema(description = "Total working duration in minutes", example = "480")
    private Integer totalWorkingMinutes = 0;

    @Schema(description = "Total break duration in minutes", example = "45")
    private Integer totalBreakMinutes = 0;

    @Schema(description = "Whether the employee arrived late", example = "false")
    private Boolean isLate = false;

    @Schema(description = "Late duration HH:mm", example = "00:00")
    private String lateBy = "00:00";

    public DepartmentMemberDailyAttendanceDto() {}

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
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

    public Integer getTotalWorkingMinutes() {
        return totalWorkingMinutes;
    }

    public void setTotalWorkingMinutes(Integer totalWorkingMinutes) {
        this.totalWorkingMinutes = totalWorkingMinutes;
    }

    public Integer getTotalBreakMinutes() {
        return totalBreakMinutes;
    }

    public void setTotalBreakMinutes(Integer totalBreakMinutes) {
        this.totalBreakMinutes = totalBreakMinutes;
    }

    public Boolean getIsLate() {
        return isLate;
    }

    public void setIsLate(Boolean isLate) {
        this.isLate = isLate;
    }

    public String getLateBy() {
        return lateBy;
    }

    public void setLateBy(String lateBy) {
        this.lateBy = lateBy;
    }
}
