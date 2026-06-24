package com.example.ems.leave.dto;

public class LeaveApprovalSummaryDto {
    private long pending;
    private long approvedToday;
    private long rejectedToday;

    public LeaveApprovalSummaryDto() {}

    public LeaveApprovalSummaryDto(long pending, long approvedToday, long rejectedToday) {
        this.pending = pending;
        this.approvedToday = approvedToday;
        this.rejectedToday = rejectedToday;
    }

    public long getPending() { return pending; }
    public void setPending(long pending) { this.pending = pending; }

    public long getApprovedToday() { return approvedToday; }
    public void setApprovedToday(long approvedToday) { this.approvedToday = approvedToday; }

    public long getRejectedToday() { return rejectedToday; }
    public void setRejectedToday(long rejectedToday) { this.rejectedToday = rejectedToday; }
}
