package com.example.ems.bonus.dto;

import com.example.ems.bonus.entity.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class BonusRecordResponse {

    private Long id;
    private Long organizationId;
    private Long employeeId;
    private String employeeName;
    private String employeeCode;
    private Long policyId;
    private String policyName;
    private Long policyVersion;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private BonusType bonusType;
    private BonusCalculationMethod calculationMethod;
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
    private BonusStatus status;
    private String workflowInstanceId;
    private Long payrollRunId;
    private BonusPayrollStatus payrollStatus;
    private LocalDateTime payrollPostedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public BonusRecordResponse() {}

    public static BonusRecordResponse fromEntity(BonusRecord record) {
        if (record == null) return null;
        BonusRecordResponse dto = new BonusRecordResponse();
        dto.setId(record.getId());
        dto.setOrganizationId(record.getOrganization() != null ? record.getOrganization().getId() : null);
        dto.setEmployeeId(record.getEmployee() != null ? record.getEmployee().getId() : null);
        dto.setEmployeeName(record.getEmployee() != null ? record.getEmployee().getFullName() : null);
        dto.setEmployeeCode(record.getEmployee() != null ? record.getEmployee().getEmployeeId() : null);
        dto.setPolicyId(record.getPolicy() != null ? record.getPolicy().getId() : null);
        dto.setPolicyName(record.getPolicy() != null ? record.getPolicy().getName() : null);
        dto.setPolicyVersion(record.getPolicyVersion());
        dto.setPeriodStart(record.getPeriodStart());
        dto.setPeriodEnd(record.getPeriodEnd());
        dto.setBonusType(record.getBonusType());
        dto.setCalculationMethod(record.getCalculationMethod());
        dto.setTargetValue(record.getTargetValue());
        dto.setAchievedValue(record.getAchievedValue());
        dto.setAchievementPercentage(record.getAchievementPercentage());
        dto.setPerformanceRating(record.getPerformanceRating());
        dto.setCalculatedAmount(record.getCalculatedAmount());
        dto.setAdjustedAmount(record.getAdjustedAmount());
        dto.setApprovedAmount(record.getApprovedAmount());
        dto.setEffectiveAmount(record.getEffectiveAmount());
        dto.setAdjustmentReason(record.getAdjustmentReason());
        dto.setAdjustedBy(record.getAdjustedBy());
        dto.setAdjustedAt(record.getAdjustedAt());
        dto.setApprovedBy(record.getApprovedBy());
        dto.setApprovedAt(record.getApprovedAt());
        dto.setRejectionReason(record.getRejectionReason());
        dto.setRejectedBy(record.getRejectedBy());
        dto.setRejectedAt(record.getRejectedAt());
        dto.setStatus(record.getStatus());
        dto.setWorkflowInstanceId(record.getWorkflowInstanceId());
        dto.setPayrollRunId(record.getPayrollRunId());
        dto.setPayrollStatus(record.getPayrollStatus());
        dto.setPayrollPostedAt(record.getPayrollPostedAt());
        dto.setCreatedAt(record.getCreatedAt());
        dto.setUpdatedAt(record.getUpdatedAt());
        return dto;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
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

    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    public Long getPolicyId() {
        return policyId;
    }

    public void setPolicyId(Long policyId) {
        this.policyId = policyId;
    }

    public String getPolicyName() {
        return policyName;
    }

    public void setPolicyName(String policyName) {
        this.policyName = policyName;
    }

    public Long getPolicyVersion() {
        return policyVersion;
    }

    public void setPolicyVersion(Long policyVersion) {
        this.policyVersion = policyVersion;
    }

    public LocalDate getPeriodStart() {
        return periodStart;
    }

    public void setPeriodStart(LocalDate periodStart) {
        this.periodStart = periodStart;
    }

    public LocalDate getPeriodEnd() {
        return periodEnd;
    }

    public void setPeriodEnd(LocalDate periodEnd) {
        this.periodEnd = periodEnd;
    }

    public BonusType getBonusType() {
        return bonusType;
    }

    public void setBonusType(BonusType bonusType) {
        this.bonusType = bonusType;
    }

    public BonusCalculationMethod getCalculationMethod() {
        return calculationMethod;
    }

    public void setCalculationMethod(BonusCalculationMethod calculationMethod) {
        this.calculationMethod = calculationMethod;
    }

    public BigDecimal getTargetValue() {
        return targetValue;
    }

    public void setTargetValue(BigDecimal targetValue) {
        this.targetValue = targetValue;
    }

    public BigDecimal getAchievedValue() {
        return achievedValue;
    }

    public void setAchievedValue(BigDecimal achievedValue) {
        this.achievedValue = achievedValue;
    }

    public BigDecimal getAchievementPercentage() {
        return achievementPercentage;
    }

    public void setAchievementPercentage(BigDecimal achievementPercentage) {
        this.achievementPercentage = achievementPercentage;
    }

    public BigDecimal getPerformanceRating() {
        return performanceRating;
    }

    public void setPerformanceRating(BigDecimal performanceRating) {
        this.performanceRating = performanceRating;
    }

    public BigDecimal getCalculatedAmount() {
        return calculatedAmount;
    }

    public void setCalculatedAmount(BigDecimal calculatedAmount) {
        this.calculatedAmount = calculatedAmount;
    }

    public BigDecimal getAdjustedAmount() {
        return adjustedAmount;
    }

    public void setAdjustedAmount(BigDecimal adjustedAmount) {
        this.adjustedAmount = adjustedAmount;
    }

    public BigDecimal getApprovedAmount() {
        return approvedAmount;
    }

    public void setApprovedAmount(BigDecimal approvedAmount) {
        this.approvedAmount = approvedAmount;
    }

    public BigDecimal getEffectiveAmount() {
        return effectiveAmount;
    }

    public void setEffectiveAmount(BigDecimal effectiveAmount) {
        this.effectiveAmount = effectiveAmount;
    }

    public String getAdjustmentReason() {
        return adjustmentReason;
    }

    public void setAdjustmentReason(String adjustmentReason) {
        this.adjustmentReason = adjustmentReason;
    }

    public String getAdjustedBy() {
        return adjustedBy;
    }

    public void setAdjustedBy(String adjustedBy) {
        this.adjustedBy = adjustedBy;
    }

    public LocalDateTime getAdjustedAt() {
        return adjustedAt;
    }

    public void setAdjustedAt(LocalDateTime adjustedAt) {
        this.adjustedAt = adjustedAt;
    }

    public String getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }

    public LocalDateTime getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(LocalDateTime approvedAt) {
        this.approvedAt = approvedAt;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public String getRejectedBy() {
        return rejectedBy;
    }

    public void setRejectedBy(String rejectedBy) {
        this.rejectedBy = rejectedBy;
    }

    public LocalDateTime getRejectedAt() {
        return rejectedAt;
    }

    public void setRejectedAt(LocalDateTime rejectedAt) {
        this.rejectedAt = rejectedAt;
    }

    public BonusStatus getStatus() {
        return status;
    }

    public void setStatus(BonusStatus status) {
        this.status = status;
    }

    public String getWorkflowInstanceId() {
        return workflowInstanceId;
    }

    public void setWorkflowInstanceId(String workflowInstanceId) {
        this.workflowInstanceId = workflowInstanceId;
    }

    public Long getPayrollRunId() {
        return payrollRunId;
    }

    public void setPayrollRunId(Long payrollRunId) {
        this.payrollRunId = payrollRunId;
    }

    public BonusPayrollStatus getPayrollStatus() {
        return payrollStatus;
    }

    public void setPayrollStatus(BonusPayrollStatus payrollStatus) {
        this.payrollStatus = payrollStatus;
    }

    public LocalDateTime getPayrollPostedAt() {
        return payrollPostedAt;
    }

    public void setPayrollPostedAt(LocalDateTime payrollPostedAt) {
        this.payrollPostedAt = payrollPostedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
