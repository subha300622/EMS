package com.example.ems.training.dto;

public class TeamRiskResponse {

    private Long employeeId;
    private String employeeName;
    private int overdueCount;
    private String riskLevel;

    public TeamRiskResponse() {}

    public TeamRiskResponse(Long employeeId, String employeeName, int overdueCount, String riskLevel) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.overdueCount = overdueCount;
        this.riskLevel = riskLevel;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public int getOverdueCount() {
        return overdueCount;
    }

    public void setOverdueCount(int overdueCount) {
        this.overdueCount = overdueCount;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }
}
