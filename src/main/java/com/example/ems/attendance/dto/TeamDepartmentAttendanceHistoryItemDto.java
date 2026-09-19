package com.example.ems.attendance.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Schema(description = "Team or Department Member Attendance History Record")
public class TeamDepartmentAttendanceHistoryItemDto {

    @Schema(description = "Attendance record ID", example = "101")
    private Long attendanceId;

    @Schema(description = "Employee database ID", example = "10")
    private Long employeeId;

    @Schema(description = "Employee unique business code", example = "EMP-001")
    private String employeeCode;

    @Schema(description = "Employee full name", example = "John Doe")
    private String employeeName;

    @Schema(description = "Designation", example = "Software Engineer")
    private String designation;

    @Schema(description = "Team name", example = "Backend Core Team")
    private String teamName;

    @Schema(description = "Department name", example = "Engineering")
    private String departmentName;

    @Schema(description = "Attendance date", example = "2026-09-10")
    private LocalDate attendanceDate;

    @Schema(description = "Attendance status (PRESENT, ABSENT, WORKING, COMPLETED, ON_BREAK, etc.)", example = "COMPLETED")
    private String status;

    @Schema(description = "Server check-in timestamp")
    private Instant checkInTime;

    @Schema(description = "Server check-out timestamp")
    private Instant checkOutTime;

    @Schema(description = "Total break duration in minutes", example = "45")
    private Integer totalBreakMinutes;

    @Schema(description = "Total effective working duration in minutes", example = "495")
    private Integer totalWorkingMinutes;

    @Schema(description = "Whether the employee arrived late", example = "false")
    private Boolean isLate;

    @Schema(description = "Late arrival duration HH:mm", example = "00:00")
    private String lateBy;

    @Schema(description = "List of breaks taken during the day")
    private List<AttendanceBreakDto> breaks = new ArrayList<>();

    public TeamDepartmentAttendanceHistoryItemDto() {}

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

    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
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

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
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

    public List<AttendanceBreakDto> getBreaks() {
        return breaks;
    }

    public void setBreaks(List<AttendanceBreakDto> breaks) {
        this.breaks = breaks;
    }
}
