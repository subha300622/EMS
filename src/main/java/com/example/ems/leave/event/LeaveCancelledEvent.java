package com.example.ems.leave.event;

import java.time.LocalDate;

public class LeaveCancelledEvent {
    private final String eventType = "LEAVE_CANCELLED";
    private final Long leaveRequestId;
    private final String employeeId;
    private final LocalDate startDate;
    private final LocalDate endDate;

    public LeaveCancelledEvent(Long leaveRequestId, String employeeId, LocalDate startDate, LocalDate endDate) {
        this.leaveRequestId = leaveRequestId;
        this.employeeId = employeeId;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public String getEventType() { return eventType; }
    public Long getLeaveRequestId() { return leaveRequestId; }
    public String getEmployeeId() { return employeeId; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
}
