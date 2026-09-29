package com.example.ems.attendance.dto;

import com.example.ems.attendance.entity.AttendanceRegularizationStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.time.LocalDate;

@Schema(description = "Response DTO for attendance regularization requests")
public class RegularizationResponseDto {

    @Schema(description = "Regularization request ID", example = "501")
    private Long id;

    @Schema(description = "Associated attendance session ID", example = "101")
    private Long attendanceId;

    @Schema(description = "Employee internal ID", example = "125")
    private Long employeeId;

    @Schema(description = "Employee Full Name", example = "John Doe")
    private String employeeName;

    @Schema(description = "Employee Code / Identifier", example = "EMP-125")
    private String employeeCode;

    @Schema(description = "Attendance date", example = "2026-09-10")
    private LocalDate attendanceDate;

    @Schema(description = "Original check-in timestamp")
    private Instant originalCheckInTime;

    @Schema(description = "Original check-out timestamp")
    private Instant originalCheckOutTime;

    @Schema(description = "Requested check-in timestamp")
    private Instant requestedCheckInTime;

    @Schema(description = "Requested check-out timestamp")
    private Instant requestedCheckOutTime;

    @Schema(description = "Status of regularization request", example = "PENDING")
    private AttendanceRegularizationStatus status;

    @Schema(description = "Employee's reason for correction", example = "Forgot to check out")
    private String reason;

    @Schema(description = "Manager or Approver notes", example = "Verified with access logs")
    private String managerNotes;

    @Schema(description = "Rejection reason if rejected")
    private String rejectionReason;

    @Schema(description = "Central workflow instance ID")
    private String workflowInstanceId;

    @Schema(description = "Approved by username/identifier")
    private String approvedBy;

    @Schema(description = "Approval timestamp")
    private Instant approvedAt;

    @Schema(description = "Request submission timestamp")
    private Instant createdAt;

    @Schema(description = "Last update timestamp")
    private Instant updatedAt;

    public RegularizationResponseDto() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    public LocalDate getAttendanceDate() {
        return attendanceDate;
    }

    public void setAttendanceDate(LocalDate attendanceDate) {
        this.attendanceDate = attendanceDate;
    }

    public Instant getOriginalCheckInTime() {
        return originalCheckInTime;
    }

    public void setOriginalCheckInTime(Instant originalCheckInTime) {
        this.originalCheckInTime = originalCheckInTime;
    }

    public Instant getOriginalCheckOutTime() {
        return originalCheckOutTime;
    }

    public void setOriginalCheckOutTime(Instant originalCheckOutTime) {
        this.originalCheckOutTime = originalCheckOutTime;
    }

    public Instant getRequestedCheckInTime() {
        return requestedCheckInTime;
    }

    public void setRequestedCheckInTime(Instant requestedCheckInTime) {
        this.requestedCheckInTime = requestedCheckInTime;
    }

    public Instant getRequestedCheckOutTime() {
        return requestedCheckOutTime;
    }

    public void setRequestedCheckOutTime(Instant requestedCheckOutTime) {
        this.requestedCheckOutTime = requestedCheckOutTime;
    }

    public AttendanceRegularizationStatus getStatus() {
        return status;
    }

    public void setStatus(AttendanceRegularizationStatus status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getManagerNotes() {
        return managerNotes;
    }

    public void setManagerNotes(String managerNotes) {
        this.managerNotes = managerNotes;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public String getWorkflowInstanceId() {
        return workflowInstanceId;
    }

    public void setWorkflowInstanceId(String workflowInstanceId) {
        this.workflowInstanceId = workflowInstanceId;
    }

    public String getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }

    public Instant getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(Instant approvedAt) {
        this.approvedAt = approvedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
