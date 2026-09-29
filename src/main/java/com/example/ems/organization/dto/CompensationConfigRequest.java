package com.example.ems.organization.dto;

public class CompensationConfigRequest {

    private Boolean overtimeEnabled;
    private Boolean incentiveEnabled;
    private Boolean bonusEnabled;

    public CompensationConfigRequest() {}

    public CompensationConfigRequest(Boolean overtimeEnabled, Boolean incentiveEnabled, Boolean bonusEnabled) {
        this.overtimeEnabled = overtimeEnabled;
        this.incentiveEnabled = incentiveEnabled;
        this.bonusEnabled = bonusEnabled;
    }

    public Boolean getOvertimeEnabled() {
        return overtimeEnabled;
    }

    public void setOvertimeEnabled(Boolean overtimeEnabled) {
        this.overtimeEnabled = overtimeEnabled;
    }

    public Boolean getIncentiveEnabled() {
        return incentiveEnabled;
    }

    public void setIncentiveEnabled(Boolean incentiveEnabled) {
        this.incentiveEnabled = incentiveEnabled;
    }

    public Boolean getBonusEnabled() {
        return bonusEnabled;
    }

    public void setBonusEnabled(Boolean bonusEnabled) {
        this.bonusEnabled = bonusEnabled;
    }
}
