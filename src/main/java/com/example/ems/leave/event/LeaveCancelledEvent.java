package com.example.ems.leave.event;

import java.time.Instant;
import java.time.LocalDate;

public class LeaveCancelledEvent {
    private static final String EVENT_TYPE = "LEAVE_CANCELLED";
    private final Long leaveRequestId;
    private final String employeeId;
    private final Long organizationId;
    private final String cancelledBy;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final Instant timestamp;

    public LeaveCancelledEvent(Long leaveRequestId, String employeeId, LocalDate startDate, LocalDate endDate) {
        this(leaveRequestId, employeeId, null, null, startDate, endDate, Instant.now());
    }

    public LeaveCancelledEvent(Long leaveRequestId, String employeeId, Long organizationId, String cancelledBy, LocalDate startDate, LocalDate endDate) {
        this(leaveRequestId, employeeId, organizationId, cancelledBy, startDate, endDate, Instant.now());
    }

    public LeaveCancelledEvent(Long leaveRequestId, String employeeId, Long organizationId, String cancelledBy, LocalDate startDate, LocalDate endDate, Instant timestamp) {
        this.leaveRequestId = leaveRequestId;
        this.employeeId = employeeId;
        this.organizationId = organizationId;
        this.cancelledBy = cancelledBy;
        this.startDate = startDate;
        this.endDate = endDate;
        this.timestamp = timestamp != null ? timestamp : Instant.now();
    }

    public String getEventType() { return EVENT_TYPE; }
    public Long getLeaveRequestId() { return leaveRequestId; }
    public Long leaveId() { return leaveRequestId; }
    public String getEmployeeId() { return employeeId; }
    public String employeeCode() { return employeeId; }
    public Long getOrganizationId() { return organizationId; }
    public Long organizationId() { return organizationId; }
    public String getCancelledBy() { return cancelledBy; }
    public String cancelledBy() { return cancelledBy; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate startDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public LocalDate endDate() { return endDate; }
    public Instant getTimestamp() { return timestamp; }
    public Instant timestamp() { return timestamp; }
}
