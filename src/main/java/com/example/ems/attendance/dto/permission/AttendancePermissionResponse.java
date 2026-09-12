package com.example.ems.attendance.dto.permission;

import com.example.ems.attendance.entity.AttendancePermission;
import com.example.ems.attendance.entity.AttendancePermissionStatus;
import com.example.ems.attendance.entity.AttendancePermissionType;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

public class AttendancePermissionResponse {

    private Long id;
    private Long organizationId;
    private Long employeeId;
    private String employeeName;
    private String employeeCode;
    private LocalDate attendanceDate;
    private AttendancePermissionType permissionType;
    private Integer requestedMinutes;
    private LocalTime expectedTime;
    private LocalTime actualTime;
    private String reason;
    private AttendancePermissionStatus status;
    private String rejectionReason;
    private String workflowInstanceId;
    private String approvedBy;
    private Instant approvedAt;
    private Instant createdAt;
    private Instant updatedAt;

    public AttendancePermissionResponse() {}

    public static AttendancePermissionResponse fromEntity(AttendancePermission permission) {
        if (permission == null) {
            return null;
        }
        AttendancePermissionResponse dto = new AttendancePermissionResponse();
        dto.setId(permission.getId());
        if (permission.getOrganization() != null) {
            dto.setOrganizationId(permission.getOrganization().getId());
        }
        if (permission.getEmployee() != null) {
            dto.setEmployeeId(permission.getEmployee().getId());
            dto.setEmployeeName(permission.getEmployee().getFirstName() + " " + permission.getEmployee().getLastName());
            dto.setEmployeeCode(permission.getEmployee().getEmployeeId());
        }
        dto.setAttendanceDate(permission.getAttendanceDate());
        dto.setPermissionType(permission.getPermissionType());
        dto.setRequestedMinutes(permission.getRequestedMinutes());
        dto.setExpectedTime(permission.getExpectedTime());
        dto.setActualTime(permission.getActualTime());
        dto.setReason(permission.getReason());
        dto.setStatus(permission.getStatus());
        dto.setRejectionReason(permission.getRejectionReason());
        dto.setWorkflowInstanceId(permission.getWorkflowInstanceId());
        dto.setApprovedBy(permission.getApprovedBy());
        dto.setApprovedAt(permission.getApprovedAt());
        dto.setCreatedAt(permission.getCreatedAt());
        dto.setUpdatedAt(permission.getUpdatedAt());
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getOrganizationId() { return organizationId; }
    public void setOrganizationId(Long organizationId) { this.organizationId = organizationId; }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public String getEmployeeCode() { return employeeCode; }
    public void setEmployeeCode(String employeeCode) { this.employeeCode = employeeCode; }

    public LocalDate getAttendanceDate() { return attendanceDate; }
    public void setAttendanceDate(LocalDate attendanceDate) { this.attendanceDate = attendanceDate; }

    public AttendancePermissionType getPermissionType() { return permissionType; }
    public void setPermissionType(AttendancePermissionType permissionType) { this.permissionType = permissionType; }

    public Integer getRequestedMinutes() { return requestedMinutes; }
    public void setRequestedMinutes(Integer requestedMinutes) { this.requestedMinutes = requestedMinutes; }

    public LocalTime getExpectedTime() { return expectedTime; }
    public void setExpectedTime(LocalTime expectedTime) { this.expectedTime = expectedTime; }

    public LocalTime getActualTime() { return actualTime; }
    public void setActualTime(LocalTime actualTime) { this.actualTime = actualTime; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public AttendancePermissionStatus getStatus() { return status; }
    public void setStatus(AttendancePermissionStatus status) { this.status = status; }

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
