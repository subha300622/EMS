package com.example.ems.finance.dto.manager;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;

@Schema(description = "Manager Leave Summary metrics")
public class ManagerLeaveSummaryDto implements Serializable {

    @Schema(description = "Number of pending leave requests", example = "4")
    private Long pendingRequests;

    @Schema(description = "Number of leaves approved this month", example = "12")
    private Long approvedThisMonth;

    @Schema(description = "Total team leave days taken this month", example = "38.0")
    private Double teamLeaveDays;

    public ManagerLeaveSummaryDto() {}

    public ManagerLeaveSummaryDto(Long pendingRequests, Long approvedThisMonth, Double teamLeaveDays) {
        this.pendingRequests = pendingRequests;
        this.approvedThisMonth = approvedThisMonth;
        this.teamLeaveDays = teamLeaveDays;
    }

    public Long getPendingRequests() {
        return pendingRequests;
    }

    public void setPendingRequests(Long pendingRequests) {
        this.pendingRequests = pendingRequests;
    }

    public Long getApprovedThisMonth() {
        return approvedThisMonth;
    }

    public void setApprovedThisMonth(Long approvedThisMonth) {
        this.approvedThisMonth = approvedThisMonth;
    }

    public Double getTeamLeaveDays() {
        return teamLeaveDays;
    }

    public void setTeamLeaveDays(Double teamLeaveDays) {
        this.teamLeaveDays = teamLeaveDays;
    }
}
