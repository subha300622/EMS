package com.example.ems.incentive.dto;

import com.example.ems.incentive.entity.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

public class IncentivePolicyResponse {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private Long id;
    private String name;
    private String policyCode;
    private String policyName;
    private String description;
    private IncentiveType incentiveType;
    private IncentiveCalculationMethod calculationMethod;
    private IncentivePolicyStatus status;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private BigDecimal fixedAmount;
    private BigDecimal percentage;
    private BigDecimal minimumAmount;
    private BigDecimal maximumAmount;
    private BigDecimal targetValue;
    private BigDecimal minimumAchievementPercentage;
    private BigDecimal minimumRating;
    private List<IncentiveSlabTierDto> targetSlabs;
    private String formulaExpression;

    private String departmentId;
    private String designationId;
    private String employeeType;
    private String branchId;

    private IncentivePaymentFrequency paymentFrequency;
    private Boolean approvalRequired;
    private Long policyVersion;
    private Long version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public IncentivePolicyResponse() {}

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final IncentivePolicyResponse res = new IncentivePolicyResponse();

        public Builder id(Long id) { res.setId(id); return this; }
        public Builder name(String name) { res.setName(name); return this; }
        public Builder policyCode(String code) { res.setPolicyCode(code); return this; }
        public Builder policyName(String name) { res.setPolicyName(name); return this; }
        public Builder description(String desc) { res.setDescription(desc); return this; }
        public Builder incentiveType(IncentiveType type) { res.setIncentiveType(type); return this; }
        public Builder calculationMethod(IncentiveCalculationMethod method) { res.setCalculationMethod(method); return this; }
        public Builder status(IncentivePolicyStatus status) { res.setStatus(status); return this; }
        public Builder effectiveFrom(LocalDate from) { res.setEffectiveFrom(from); return this; }
        public Builder effectiveTo(LocalDate to) { res.setEffectiveTo(to); return this; }
        public Builder fixedAmount(BigDecimal amt) { res.setFixedAmount(amt); return this; }
        public Builder percentage(BigDecimal pct) { res.setPercentage(pct); return this; }
        public Builder minimumAmount(BigDecimal min) { res.setMinimumAmount(min); return this; }
        public Builder maximumAmount(BigDecimal max) { res.setMaximumAmount(max); return this; }
        public Builder targetValue(BigDecimal target) { res.setTargetValue(target); return this; }
        public Builder minimumAchievementPercentage(BigDecimal minAch) { res.setMinimumAchievementPercentage(minAch); return this; }
        public Builder minimumRating(BigDecimal minRating) { res.setMinimumRating(minRating); return this; }
        public Builder targetSlabs(List<IncentiveSlabTierDto> slabs) { res.setTargetSlabs(slabs); return this; }
        public Builder formulaExpression(String formula) { res.setFormulaExpression(formula); return this; }
        public Builder departmentId(String deptId) { res.setDepartmentId(deptId); return this; }
        public Builder designationId(String desigId) { res.setDesignationId(desigId); return this; }
        public Builder employeeType(String type) { res.setEmployeeType(type); return this; }
        public Builder branchId(String branchId) { res.setBranchId(branchId); return this; }
        public Builder paymentFrequency(IncentivePaymentFrequency freq) { res.setPaymentFrequency(freq); return this; }
        public Builder approvalRequired(Boolean req) { res.setApprovalRequired(req); return this; }
        public Builder policyVersion(Long ver) { res.setPolicyVersion(ver); return this; }
        public Builder version(Long ver) { res.setVersion(ver); return this; }
        public Builder createdAt(LocalDateTime time) { res.setCreatedAt(time); return this; }
        public Builder updatedAt(LocalDateTime time) { res.setUpdatedAt(time); return this; }

        public IncentivePolicyResponse build() {
            if (res.getName() == null && res.getPolicyCode() != null) res.setName(res.getPolicyCode());
            if (res.getPolicyCode() == null && res.getName() != null) res.setPolicyCode(res.getName());
            if (res.getPolicyName() == null && res.getName() != null) res.setPolicyName(res.getName());
            return res;
        }
    }

    public static IncentivePolicyResponse fromEntity(IncentivePolicy policy) {
        if (policy == null) return null;
        IncentivePolicyResponse res = new IncentivePolicyResponse();
        res.setId(policy.getId());
        res.setName(policy.getName());
        res.setPolicyCode(policy.getPolicyCode());
        res.setPolicyName(policy.getName());
        res.setDescription(policy.getDescription());
        res.setIncentiveType(policy.getIncentiveType());
        res.setCalculationMethod(policy.getCalculationMethod());
        res.setStatus(policy.getStatus());
        res.setEffectiveFrom(policy.getEffectiveFrom());
        res.setEffectiveTo(policy.getEffectiveTo());
        res.setFixedAmount(policy.getFixedAmount());
        res.setPercentage(policy.getPercentage());
        res.setMinimumAmount(policy.getMinimumAmount());
        res.setMaximumAmount(policy.getMaximumAmount());
        res.setTargetValue(policy.getTargetValue());
        res.setMinimumAchievementPercentage(policy.getMinimumAchievementPercentage());
        res.setMinimumRating(policy.getMinimumRating());
        res.setFormulaExpression(policy.getFormulaExpression());

        if (policy.getTargetSlabsJson() != null && !policy.getTargetSlabsJson().trim().isEmpty()) {
            try {
                List<IncentiveSlabTierDto> slabs = MAPPER.readValue(policy.getTargetSlabsJson(),
                        new TypeReference<List<IncentiveSlabTierDto>>() {});
                res.setTargetSlabs(slabs);
            } catch (Exception e) {
                res.setTargetSlabs(Collections.emptyList());
            }
        }

        res.setDepartmentId(policy.getDepartmentId());
        res.setDesignationId(policy.getDesignationId());
        res.setEmployeeType(policy.getEmployeeType());
        res.setBranchId(policy.getBranchId());

        res.setPaymentFrequency(policy.getPaymentFrequency());
        res.setApprovalRequired(policy.getApprovalRequired());
        res.setPolicyVersion(policy.getPolicyVersion());
        res.setVersion(policy.getVersion());
        res.setCreatedAt(policy.getCreatedAt());
        res.setUpdatedAt(policy.getUpdatedAt());
        return res;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name != null ? name : policyCode; }
    public void setName(String name) { this.name = name; }

    public String getPolicyCode() { return policyCode != null ? policyCode : name; }
    public void setPolicyCode(String policyCode) { this.policyCode = policyCode; }

    public String getPolicyName() { return policyName != null ? policyName : name; }
    public void setPolicyName(String policyName) { this.policyName = policyName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public IncentiveType getIncentiveType() { return incentiveType; }
    public void setIncentiveType(IncentiveType incentiveType) { this.incentiveType = incentiveType; }

    public IncentiveCalculationMethod getCalculationMethod() { return calculationMethod; }
    public void setCalculationMethod(IncentiveCalculationMethod calculationMethod) { this.calculationMethod = calculationMethod; }

    public IncentivePolicyStatus getStatus() { return status; }
    public void setStatus(IncentivePolicyStatus status) { this.status = status; }

    public LocalDate getEffectiveFrom() { return effectiveFrom; }
    public void setEffectiveFrom(LocalDate effectiveFrom) { this.effectiveFrom = effectiveFrom; }

    public LocalDate getEffectiveTo() { return effectiveTo; }
    public void setEffectiveTo(LocalDate effectiveTo) { this.effectiveTo = effectiveTo; }

    public BigDecimal getFixedAmount() { return fixedAmount; }
    public void setFixedAmount(BigDecimal fixedAmount) { this.fixedAmount = fixedAmount; }

    public BigDecimal getPercentage() { return percentage; }
    public void setPercentage(BigDecimal percentage) { this.percentage = percentage; }

    public BigDecimal getMinimumAmount() { return minimumAmount; }
    public void setMinimumAmount(BigDecimal minimumAmount) { this.minimumAmount = minimumAmount; }

    public BigDecimal getMaximumAmount() { return maximumAmount; }
    public void setMaximumAmount(BigDecimal maximumAmount) { this.maximumAmount = maximumAmount; }

    public BigDecimal getTargetValue() { return targetValue; }
    public void setTargetValue(BigDecimal targetValue) { this.targetValue = targetValue; }

    public BigDecimal getMinimumAchievementPercentage() { return minimumAchievementPercentage; }
    public void setMinimumAchievementPercentage(BigDecimal minimumAchievementPercentage) { this.minimumAchievementPercentage = minimumAchievementPercentage; }

    public BigDecimal getMinimumRating() { return minimumRating; }
    public void setMinimumRating(BigDecimal minimumRating) { this.minimumRating = minimumRating; }

    public List<IncentiveSlabTierDto> getTargetSlabs() { return targetSlabs; }
    public void setTargetSlabs(List<IncentiveSlabTierDto> targetSlabs) { this.targetSlabs = targetSlabs; }

    public String getFormulaExpression() { return formulaExpression; }
    public void setFormulaExpression(String formulaExpression) { this.formulaExpression = formulaExpression; }

    public String getDepartmentId() { return departmentId; }
    public void setDepartmentId(String departmentId) { this.departmentId = departmentId; }

    public String getDesignationId() { return designationId; }
    public void setDesignationId(String designationId) { this.designationId = designationId; }

    public String getEmployeeType() { return employeeType; }
    public void setEmployeeType(String employeeType) { this.employeeType = employeeType; }

    public String getBranchId() { return branchId; }
    public void setBranchId(String branchId) { this.branchId = branchId; }

    public IncentivePaymentFrequency getPaymentFrequency() { return paymentFrequency; }
    public void setPaymentFrequency(IncentivePaymentFrequency paymentFrequency) { this.paymentFrequency = paymentFrequency; }

    public Boolean getApprovalRequired() { return approvalRequired; }
    public void setApprovalRequired(Boolean approvalRequired) { this.approvalRequired = approvalRequired; }

    public Long getPolicyVersion() { return policyVersion; }
    public void setPolicyVersion(Long policyVersion) { this.policyVersion = policyVersion; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
