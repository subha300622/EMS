package com.example.ems.leave.event;

import java.time.Instant;
import java.time.LocalDate;

public class LeaveApprovedEvent {
    private static final String EVENT_TYPE = "LEAVE_APPROVED";
    private final Long leaveRequestId;
    private final String employeeId;
    private final Long organizationId;
    private final String approvedBy;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final String leaveTypeName;
    private final Instant timestamp;

    public LeaveApprovedEvent(Long leaveRequestId, String employeeId, LocalDate startDate, LocalDate endDate, String leaveTypeName) {
        this(leaveRequestId, employeeId, null, null, startDate, endDate, leaveTypeName, Instant.now());
    }

    public LeaveApprovedEvent(Long leaveRequestId, String employeeId, Long organizationId, String approvedBy, LocalDate startDate, LocalDate endDate, String leaveTypeName) {
        this(leaveRequestId, employeeId, organizationId, approvedBy, startDate, endDate, leaveTypeName, Instant.now());
    }

    public LeaveApprovedEvent(Long leaveRequestId, String employeeId, Long organizationId, String approvedBy, LocalDate startDate, LocalDate endDate, String leaveTypeName, Instant timestamp) {
        this.leaveRequestId = leaveRequestId;
        this.employeeId = employeeId;
        this.organizationId = organizationId;
        this.approvedBy = approvedBy;
        this.startDate = startDate;
        this.endDate = endDate;
        this.leaveTypeName = leaveTypeName;
        this.timestamp = timestamp != null ? timestamp : Instant.now();
    }

    public String getEventType() { return EVENT_TYPE; }
    public Long getLeaveRequestId() { return leaveRequestId; }
    public Long leaveId() { return leaveRequestId; }
    public String getEmployeeId() { return employeeId; }
    public String employeeCode() { return employeeId; }
    public Long getOrganizationId() { return organizationId; }
    public Long organizationId() { return organizationId; }
    public String getApprovedBy() { return approvedBy; }
    public String approvedBy() { return approvedBy; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate startDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public LocalDate endDate() { return endDate; }
    public String getLeaveTypeName() { return leaveTypeName; }
    public String leaveTypeName() { return leaveTypeName; }
    public Instant getTimestamp() { return timestamp; }
    public Instant timestamp() { return timestamp; }
}
