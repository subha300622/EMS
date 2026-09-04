package com.example.ems.leave.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

public class LeaveRequest {

    @Schema(example = "1")
    private Long leaveTypeId;

    @Schema(example = "2026-09-01")
    private LocalDate startDate;

    @Schema(example = "2026-09-03")
    private LocalDate endDate;

    @Schema(example = "2026-09-01")
    private LocalDate fromDate;

    @Schema(example = "2026-09-03")
    private LocalDate toDate;

    @Schema(example = "FULL_DAY")
    private String durationType = "FULL_DAY";

    @Schema(example = "Personal work")
    private String reason;

    public LeaveRequest() {}

    public LeaveRequest(Long leaveTypeId, LocalDate startDate, LocalDate endDate, String reason) {
        this.leaveTypeId = leaveTypeId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.reason = reason;
    }

    public Long getLeaveTypeId() { return leaveTypeId; }
    public void setLeaveTypeId(Long leaveTypeId) { this.leaveTypeId = leaveTypeId; }

    public LocalDate getStartDate() {
        return startDate != null ? startDate : fromDate;
    }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() {
        return endDate != null ? endDate : toDate;
    }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public LocalDate getFromDate() { return getStartDate(); }
    public void setFromDate(LocalDate fromDate) {
        this.fromDate = fromDate;
        if (this.startDate == null) this.startDate = fromDate;
    }

    public LocalDate getToDate() { return getEndDate(); }
    public void setToDate(LocalDate toDate) {
        this.toDate = toDate;
        if (this.endDate == null) this.endDate = toDate;
    }

    public String getDurationType() { return durationType != null ? durationType : "FULL_DAY"; }
    public void setDurationType(String durationType) { this.durationType = durationType; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
