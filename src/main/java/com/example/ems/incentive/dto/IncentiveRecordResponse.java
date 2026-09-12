package com.example.ems.incentive.dto;

import com.example.ems.incentive.entity.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class IncentiveRecordResponse {

    private Long id;
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
    private BigDecimal adjustedAmount;
    private BigDecimal approvedAmount;
    private BigDecimal effectiveAmount;

    private String adjustmentReason;
    private String adjustedBy;
    private LocalDateTime adjustedAt;

    private String approvedBy;
    private LocalDateTime approvedAt;

    private String rejectionReason;
    private String rejectedBy;
    private LocalDateTime rejectedAt;

    private IncentiveStatus status;
    private String workflowInstanceId;
    private Long payrollRunId;
    private IncentivePayrollStatus payrollStatus;

    private Long version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public IncentiveRecordResponse() {}

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final IncentiveRecordResponse res = new IncentiveRecordResponse();

        public Builder id(Long id) { res.setId(id); return this; }
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
        public Builder adjustedAmount(BigDecimal amt) { res.setAdjustedAmount(amt); return this; }
        public Builder approvedAmount(BigDecimal amt) { res.setApprovedAmount(amt); return this; }
        public Builder effectiveAmount(BigDecimal amt) { res.setEffectiveAmount(amt); return this; }
        public Builder adjustmentReason(String reason) { res.setAdjustmentReason(reason); return this; }
        public Builder adjustedBy(String by) { res.setAdjustedBy(by); return this; }
        public Builder adjustedAt(LocalDateTime at) { res.setAdjustedAt(at); return this; }
        public Builder approvedBy(String by) { res.setApprovedBy(by); return this; }
        public Builder approvedAt(LocalDateTime at) { res.setApprovedAt(at); return this; }
        public Builder rejectionReason(String reason) { res.setRejectionReason(reason); return this; }
        public Builder rejectedBy(String by) { res.setRejectedBy(by); return this; }
        public Builder rejectedAt(LocalDateTime at) { res.setRejectedAt(at); return this; }
        public Builder status(IncentiveStatus status) { res.setStatus(status); return this; }
        public Builder workflowInstanceId(String wfId) { res.setWorkflowInstanceId(wfId); return this; }
        public Builder payrollRunId(Long runId) { res.setPayrollRunId(runId); return this; }
        public Builder payrollStatus(IncentivePayrollStatus status) { res.setPayrollStatus(status); return this; }
        public Builder version(Long ver) { res.setVersion(ver); return this; }
        public Builder createdAt(LocalDateTime time) { res.setCreatedAt(time); return this; }
        public Builder updatedAt(LocalDateTime time) { res.setUpdatedAt(time); return this; }

        public IncentiveRecordResponse build() {
            return res;
        }
    }

    public static IncentiveRecordResponse fromEntity(IncentiveRecord record) {
        if (record == null) return null;
        IncentiveRecordResponse res = new IncentiveRecordResponse();
        res.setId(record.getId());
        if (record.getEmployee() != null) {
            res.setEmployeeId(record.getEmployee().getId());
            res.setEmployeeName(record.getEmployee().getFullName());
            res.setEmployeeCode(record.getEmployee().getEmployeeId());
        }
        if (record.getPolicy() != null) {
            res.setPolicyId(record.getPolicy().getId());
            res.setPolicyName(record.getPolicy().getName());
        }
        res.setPolicyVersion(record.getPolicyVersion());
        res.setPeriodStart(record.getPeriodStart());
        res.setPeriodEnd(record.getPeriodEnd());
        res.setIncentiveType(record.getIncentiveType());
        res.setCalculationMethod(record.getCalculationMethod());
        res.setTargetValue(record.getTargetValue());
        res.setAchievedValue(record.getAchievedValue());
        res.setAchievementPercentage(record.getAchievementPercentage());
        res.setPerformanceRating(record.getPerformanceRating());
        res.setCalculatedAmount(record.getCalculatedAmount());
        res.setAdjustedAmount(record.getAdjustedAmount());
        res.setApprovedAmount(record.getApprovedAmount());
        res.setEffectiveAmount(record.getEffectiveAmount());

        res.setAdjustmentReason(record.getAdjustmentReason());
        res.setAdjustedBy(record.getAdjustedBy());
        res.setAdjustedAt(record.getAdjustedAt());

        res.setApprovedBy(record.getApprovedBy());
        res.setApprovedAt(record.getApprovedAt());

        res.setRejectionReason(record.getRejectionReason());
        res.setRejectedBy(record.getRejectedBy());
        res.setRejectedAt(record.getRejectedAt());

        res.setStatus(record.getStatus());
        res.setWorkflowInstanceId(record.getWorkflowInstanceId());
        res.setPayrollRunId(record.getPayrollRunId());
        res.setPayrollStatus(record.getPayrollStatus());
        res.setVersion(record.getVersion());
        res.setCreatedAt(record.getCreatedAt());
        res.setUpdatedAt(record.getUpdatedAt());
        return res;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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

    public BigDecimal getAdjustedAmount() { return adjustedAmount; }
    public void setAdjustedAmount(BigDecimal adjustedAmount) { this.adjustedAmount = adjustedAmount; }

    public BigDecimal getApprovedAmount() { return approvedAmount; }
    public void setApprovedAmount(BigDecimal approvedAmount) { this.approvedAmount = approvedAmount; }

    public BigDecimal getEffectiveAmount() { return effectiveAmount; }
    public void setEffectiveAmount(BigDecimal effectiveAmount) { this.effectiveAmount = effectiveAmount; }

    public String getAdjustmentReason() { return adjustmentReason; }
    public void setAdjustmentReason(String adjustmentReason) { this.adjustmentReason = adjustmentReason; }

    public String getAdjustedBy() { return adjustedBy; }
    public void setAdjustedBy(String adjustedBy) { this.adjustedBy = adjustedBy; }

    public LocalDateTime getAdjustedAt() { return adjustedAt; }
    public void setAdjustedAt(LocalDateTime adjustedAt) { this.adjustedAt = adjustedAt; }

    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }

    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public String getRejectedBy() { return rejectedBy; }
    public void setRejectedBy(String rejectedBy) { this.rejectedBy = rejectedBy; }

    public LocalDateTime getRejectedAt() { return rejectedAt; }
    public void setRejectedAt(LocalDateTime rejectedAt) { this.rejectedAt = rejectedAt; }

    public IncentiveStatus getStatus() { return status; }
    public void setStatus(IncentiveStatus status) { this.status = status; }

    public String getWorkflowInstanceId() { return workflowInstanceId; }
    public void setWorkflowInstanceId(String workflowInstanceId) { this.workflowInstanceId = workflowInstanceId; }

    public Long getPayrollRunId() { return payrollRunId; }
    public void setPayrollRunId(Long payrollRunId) { this.payrollRunId = payrollRunId; }

    public IncentivePayrollStatus getPayrollStatus() { return payrollStatus; }
    public void setPayrollStatus(IncentivePayrollStatus payrollStatus) { this.payrollStatus = payrollStatus; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
