package com.example.ems.attendance.dto.adjustment;

import com.example.ems.attendance.entity.AttendanceAdjustment;
import com.example.ems.attendance.entity.AttendanceAdjustmentStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.time.LocalDate;

@Schema(description = "Attendance Adjustment Response Payload")
public class AdjustmentResponseDto {

    private Long id;
    private Long attendanceId;
    private Long employeeId;
    private String employeeName;
    private String employeeCode;
    private LocalDate attendanceDate;
    private Instant originalCheckInTime;
    private Instant originalCheckOutTime;
    private Instant requestedCheckInTime;
    private Instant requestedCheckOutTime;
    private AttendanceAdjustmentStatus status;
    private String reason;
    private String managerNotes;
    private String rejectionReason;
    private String workflowInstanceId;
    private String approvedBy;
    private Instant approvedAt;
    private Instant createdAt;
    private Instant updatedAt;

    public AdjustmentResponseDto() {}

    public static AdjustmentResponseDto fromEntity(AttendanceAdjustment adj) {
        if (adj == null) return null;
        AdjustmentResponseDto dto = new AdjustmentResponseDto();
        dto.setId(adj.getId());
        if (adj.getAttendance() != null) {
            dto.setAttendanceId(adj.getAttendance().getId());
            dto.setAttendanceDate(adj.getAttendance().getDate());
            dto.setOriginalCheckInTime(adj.getAttendance().getCheckInTime());
            dto.setOriginalCheckOutTime(adj.getAttendance().getCheckOutTime());
        }
        if (adj.getEmployee() != null) {
            dto.setEmployeeId(adj.getEmployee().getId());
            dto.setEmployeeName(adj.getEmployee().getFullName());
            dto.setEmployeeCode(adj.getEmployee().getEmployeeId());
        }
        dto.setRequestedCheckInTime(adj.getRequestedCheckInTime());
        dto.setRequestedCheckOutTime(adj.getRequestedCheckOutTime());
        dto.setStatus(adj.getStatus());
        dto.setReason(adj.getReason());
        dto.setManagerNotes(adj.getManagerNotes());
        dto.setRejectionReason(adj.getRejectionReason());
        dto.setWorkflowInstanceId(adj.getWorkflowInstanceId());
        dto.setApprovedBy(adj.getApprovedBy());
        dto.setApprovedAt(adj.getApprovedAt());
        dto.setCreatedAt(adj.getCreatedAt());
        dto.setUpdatedAt(adj.getUpdatedAt());
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAttendanceId() { return attendanceId; }
    public void setAttendanceId(Long attendanceId) { this.attendanceId = attendanceId; }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public String getEmployeeCode() { return employeeCode; }
    public void setEmployeeCode(String employeeCode) { this.employeeCode = employeeCode; }

    public LocalDate getAttendanceDate() { return attendanceDate; }
    public void setAttendanceDate(LocalDate attendanceDate) { this.attendanceDate = attendanceDate; }

    public Instant getOriginalCheckInTime() { return originalCheckInTime; }
    public void setOriginalCheckInTime(Instant originalCheckInTime) { this.originalCheckInTime = originalCheckInTime; }

    public Instant getOriginalCheckOutTime() { return originalCheckOutTime; }
    public void setOriginalCheckOutTime(Instant originalCheckOutTime) { this.originalCheckOutTime = originalCheckOutTime; }

    public Instant getRequestedCheckInTime() { return requestedCheckInTime; }
    public void setRequestedCheckInTime(Instant requestedCheckInTime) { this.requestedCheckInTime = requestedCheckInTime; }

    public Instant getRequestedCheckOutTime() { return requestedCheckOutTime; }
    public void setRequestedCheckOutTime(Instant requestedCheckOutTime) { this.requestedCheckOutTime = requestedCheckOutTime; }

    public AttendanceAdjustmentStatus getStatus() { return status; }
    public void setStatus(AttendanceAdjustmentStatus status) { this.status = status; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getManagerNotes() { return managerNotes; }
    public void setManagerNotes(String managerNotes) { this.managerNotes = managerNotes; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public String getWorkflowInstanceId() { return workflowInstanceId; }
    public void setWorkflowInstanceId(String workflowInstanceId) { this.workflowInstanceId = workflowInstanceId; }

    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }

    public Instant getApprovedAt() { return approvedAt; }
    public void setApprovedAt(Instant approvedAt) { this.approvedAt = approvedAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
