package com.example.ems.leave.dto;
import io.swagger.v3.oas.annotations.media.Schema;



import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class LeaveRequest {

    @NotNull(message = "Leave type ID is required")
    @Schema(example = "1")
    private Long leaveTypeId;

    @NotNull(message = "Start date is required")
    @Schema(example = "2026-06-19")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    @Schema(example = "2026-06-19")
    private LocalDate endDate;

    @Schema(example = "Personal business")
    private String reason;

    public LeaveRequest() {}

    public LeaveRequest(Long leaveTypeId, LocalDate startDate, LocalDate endDate, String reason) {
        this.leaveTypeId = leaveTypeId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.reason = reason;
    }

    public Long getLeaveTypeId() {
        return leaveTypeId;
    }

    public void setLeaveTypeId(Long leaveTypeId) {
        this.leaveTypeId = leaveTypeId;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
