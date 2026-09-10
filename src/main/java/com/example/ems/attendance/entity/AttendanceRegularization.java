package com.example.ems.attendance.entity;

import com.example.ems.employee.entity.Employee;
import com.example.ems.organization.entity.Organization;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "attendance_regularizations")
public class AttendanceRegularization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attendance_id")
    private Attendance attendance;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    @JsonIgnore
    private Organization organization;

    @Column(nullable = false)
    private LocalDate date;

    @Column(name = "requested_check_in_time")
    private Instant requestedCheckInTime;

    @Column(name = "requested_check_out_time")
    private Instant requestedCheckOutTime;

    @Column(name = "proposed_punch_in_time")
    private LocalTime proposedPunchInTime;

    @Column(name = "proposed_punch_out_time")
    private LocalTime proposedPunchOutTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttendanceRegularizationStatus status = AttendanceRegularizationStatus.PENDING;

    @Column(length = 500)
    private String reason;

    @Column(name = "manager_notes", length = 500)
    private String managerNotes;

    @Column(name = "workflow_instance_id")
    private String workflowInstanceId;

    @Column(name = "approved_by")
    private String approvedBy;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt = Instant.now();

    public AttendanceRegularization() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Attendance getAttendance() {
        return attendance;
    }

    public void setAttendance(Attendance attendance) {
        this.attendance = attendance;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
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

    public LocalTime getProposedPunchInTime() {
        return proposedPunchInTime;
    }

    public void setProposedPunchInTime(LocalTime proposedPunchInTime) {
        this.proposedPunchInTime = proposedPunchInTime;
    }

    public LocalTime getProposedPunchOutTime() {
        return proposedPunchOutTime;
    }

    public void setProposedPunchOutTime(LocalTime proposedPunchOutTime) {
        this.proposedPunchOutTime = proposedPunchOutTime;
    }

    public AttendanceRegularizationStatus getStatus() {
        return status;
    }

    public void setStatus(AttendanceRegularizationStatus status) {
        this.status = status;
    }

    public void setStatus(String statusStr) {
        if (statusStr != null) {
            try {
                this.status = AttendanceRegularizationStatus.valueOf(statusStr.toUpperCase());
            } catch (Exception e) {
                this.status = AttendanceRegularizationStatus.PENDING;
            }
        }
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

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
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

    @PreUpdate
    public void onPreUpdate() {
        this.updatedAt = Instant.now();
    }
}
