package com.example.ems.leave.event;

import java.time.LocalDate;

public class LeaveApprovedEvent {
    private final String eventType = "LEAVE_APPROVED";
    private final Long leaveRequestId;
    private final String employeeId;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final String leaveTypeName;

    public LeaveApprovedEvent(Long leaveRequestId, String employeeId, LocalDate startDate, LocalDate endDate, String leaveTypeName) {
        this.leaveRequestId = leaveRequestId;
        this.employeeId = employeeId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.leaveTypeName = leaveTypeName;
    }

    public String getEventType() { return eventType; }
    public Long getLeaveRequestId() { return leaveRequestId; }
    public String getEmployeeId() { return employeeId; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public String getLeaveTypeName() { return leaveTypeName; }
}
