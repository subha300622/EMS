package com.example.ems.leave.dto;

import jakarta.validation.constraints.NotNull;

public class LeavePolicyRequest {

    @NotNull(message = "Policy name is required")
    private String name;

    @NotNull(message = "Leave Type ID is required")
    private Long leaveTypeId;

    private Integer carryingLimit;

    private String accrualType;

    private String status;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getLeaveTypeId() {
        return leaveTypeId;
    }

    public void setLeaveTypeId(Long leaveTypeId) {
        this.leaveTypeId = leaveTypeId;
    }

    public Integer getCarryingLimit() {
        return carryingLimit;
    }

    public void setCarryingLimit(Integer carryingLimit) {
        this.carryingLimit = carryingLimit;
    }

    public String getAccrualType() {
        return accrualType;
    }

    public void setAccrualType(String accrualType) {
        this.accrualType = accrualType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
