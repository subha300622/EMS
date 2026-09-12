package com.example.ems.leave.dto;

import java.time.LocalDate;

public class LeaveCalendarEventDto {

    private Long leaveId;
    private Long employeeId;
    private String employeeCode;
    private String employeeName;
    private String employeeEmail;
    private String department;
    private Long teamId;
    private String teamName;
    private Long leaveTypeId;
    private String leaveTypeName;
    private String color;
    private LocalDate startDate;
    private LocalDate endDate;
    private Double durationDays;
    private String durationType;
    private String status;
    private String reason;

    public LeaveCalendarEventDto() {}

    public LeaveCalendarEventDto(Long leaveId, Long employeeId, String employeeCode, String employeeName,
                                 String employeeEmail, String department, Long teamId, String teamName,
                                 Long leaveTypeId, String leaveTypeName, String color, LocalDate startDate,
                                 LocalDate endDate, Double durationDays, String durationType, String status,
                                 String reason) {
        this.leaveId = leaveId;
        this.employeeId = employeeId;
        this.employeeCode = employeeCode;
        this.employeeName = employeeName;
        this.employeeEmail = employeeEmail;
        this.department = department;
        this.teamId = teamId;
        this.teamName = teamName;
        this.leaveTypeId = leaveTypeId;
        this.leaveTypeName = leaveTypeName;
        this.color = color;
        this.startDate = startDate;
        this.endDate = endDate;
        this.durationDays = durationDays;
        this.durationType = durationType;
        this.status = status;
        this.reason = reason;
    }

    public Long getLeaveId() {
        return leaveId;
    }

    public void setLeaveId(Long leaveId) {
        this.leaveId = leaveId;
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

    public String getEmployeeEmail() {
        return employeeEmail;
    }

    public void setEmployeeEmail(String employeeEmail) {
        this.employeeEmail = employeeEmail;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public Long getTeamId() {
        return teamId;
    }

    public void setTeamId(Long teamId) {
        this.teamId = teamId;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public Long getLeaveTypeId() {
        return leaveTypeId;
    }

    public void setLeaveTypeId(Long leaveTypeId) {
        this.leaveTypeId = leaveTypeId;
    }

    public String getLeaveTypeName() {
        return leaveTypeName;
    }

    public void setLeaveTypeName(String leaveTypeName) {
        this.leaveTypeName = leaveTypeName;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Double getDurationDays() {
        return durationDays;
    }

    public void setDurationDays(Double durationDays) {
        this.durationDays = durationDays;
    }

    public String getDurationType() {
        return durationType;
    }

    public void setDurationType(String durationType) {
        this.durationType = durationType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
