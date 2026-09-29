package com.example.ems.incentive.dto;

import com.example.ems.incentive.entity.IncentiveCalculationMethod;
import com.example.ems.incentive.entity.IncentiveType;

import java.math.BigDecimal;
import java.time.LocalDate;

public class IncentivePreviewResponse {

    private Long employeeId;
    private String employeeName;
    private String employeeCode;
    private Long policyId;
    private String policyName;
    private Long policyVersion;
    private LocalDate periodStart;
    private LocalDate periodEnd;

    private IncentiveType incentiveType;
    private IncentiveCalculationMethod calculationMethod;

    private BigDecimal targetValue;
    private BigDecimal achievedValue;
    private BigDecimal achievementPercentage;
    private BigDecimal performanceRating;

    private BigDecimal calculatedAmount;
    private Boolean eligible;
    private String ineligibilityReason;
    private Boolean approvalRequired;

    public IncentivePreviewResponse() {}

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final IncentivePreviewResponse res = new IncentivePreviewResponse();

        public Builder employeeId(Long empId) { res.setEmployeeId(empId); return this; }
        public Builder employeeName(String name) { res.setEmployeeName(name); return this; }
        public Builder employeeCode(String code) { res.setEmployeeCode(code); return this; }
        public Builder policyId(Long polId) { res.setPolicyId(polId); return this; }
        public Builder policyName(String name) { res.setPolicyName(name); return this; }
        public Builder policyVersion(Long ver) { res.setPolicyVersion(ver); return this; }
        public Builder periodStart(LocalDate start) { res.setPeriodStart(start); return this; }
        public Builder periodEnd(LocalDate end) { res.setPeriodEnd(end); return this; }
        public Builder incentiveType(IncentiveType type) { res.setIncentiveType(type); return this; }
        public Builder calculationMethod(IncentiveCalculationMethod method) { res.setCalculationMethod(method); return this; }
        public Builder targetValue(BigDecimal target) { res.setTargetValue(target); return this; }
        public Builder achievedValue(BigDecimal achieved) { res.setAchievedValue(achieved); return this; }
        public Builder achievementPercentage(BigDecimal pct) { res.setAchievementPercentage(pct); return this; }
        public Builder performanceRating(BigDecimal rating) { res.setPerformanceRating(rating); return this; }
        public Builder calculatedAmount(BigDecimal amt) { res.setCalculatedAmount(amt); return this; }
        public Builder eligible(Boolean eligible) { res.setEligible(eligible); return this; }
        public Builder ineligibilityReason(String reason) { res.setIneligibilityReason(reason); return this; }
        public Builder approvalRequired(Boolean req) { res.setApprovalRequired(req); return this; }

        public IncentivePreviewResponse build() {
            return res;
        }
    }

    // Getters and Setters
    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public String getEmployeeCode() { return employeeCode; }
    public void setEmployeeCode(String employeeCode) { this.employeeCode = employeeCode; }

    public Long getPolicyId() { return policyId; }
    public void setPolicyId(Long policyId) { this.policyId = policyId; }

    public String getPolicyName() { return policyName; }
    public void setPolicyName(String policyName) { this.policyName = policyName; }

    public Long getPolicyVersion() { return policyVersion; }
    public void setPolicyVersion(Long policyVersion) { this.policyVersion = policyVersion; }

    public LocalDate getPeriodStart() { return periodStart; }
    public void setPeriodStart(LocalDate periodStart) { this.periodStart = periodStart; }

    public LocalDate getPeriodEnd() { return periodEnd; }
    public void setPeriodEnd(LocalDate periodEnd) { this.periodEnd = periodEnd; }

    public IncentiveType getIncentiveType() { return incentiveType; }
    public void setIncentiveType(IncentiveType incentiveType) { this.incentiveType = incentiveType; }

    public IncentiveCalculationMethod getCalculationMethod() { return calculationMethod; }
    public void setCalculationMethod(IncentiveCalculationMethod calculationMethod) { this.calculationMethod = calculationMethod; }

    public BigDecimal getTargetValue() { return targetValue; }
    public void setTargetValue(BigDecimal targetValue) { this.targetValue = targetValue; }

    public BigDecimal getAchievedValue() { return achievedValue; }
    public void setAchievedValue(BigDecimal achievedValue) { this.achievedValue = achievedValue; }

    public BigDecimal getAchievementPercentage() { return achievementPercentage; }
    public void setAchievementPercentage(BigDecimal achievementPercentage) { this.achievementPercentage = achievementPercentage; }

    public BigDecimal getPerformanceRating() { return performanceRating; }
    public void setPerformanceRating(BigDecimal performanceRating) { this.performanceRating = performanceRating; }

    public BigDecimal getCalculatedAmount() { return calculatedAmount; }
    public void setCalculatedAmount(BigDecimal calculatedAmount) { this.calculatedAmount = calculatedAmount; }

    public Boolean getEligible() { return eligible; }
    public boolean isEligible() { return Boolean.TRUE.equals(eligible); }
    public void setEligible(Boolean eligible) { this.eligible = eligible; }

    public String getIneligibilityReason() { return ineligibilityReason; }
    public void setIneligibilityReason(String ineligibilityReason) { this.ineligibilityReason = ineligibilityReason; }

    public Boolean getApprovalRequired() { return approvalRequired; }
    public void setApprovalRequired(Boolean approvalRequired) { this.approvalRequired = approvalRequired; }
}
