package com.example.ems.training.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Team Risk Response")
public class TeamRiskResponse {

    @Schema(description = "Employee ID", example = "1")
    private Long employeeId;
    @Schema(description = "Employee full name", example = "John Doe")
    private String employeeName;
    @Schema(description = "Count of overdue trainings", example = "2")
    private int overdueCount;
    @Schema(description = "Calculated risk level", example = "HIGH")
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
