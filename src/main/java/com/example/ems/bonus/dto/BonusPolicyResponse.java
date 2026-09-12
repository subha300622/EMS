package com.example.ems.bonus.dto;

import com.example.ems.bonus.entity.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class BonusPolicyResponse {

    private Long id;
    private Long organizationId;
    private String name;
    private String description;
    private BonusType bonusType;
    private BonusCalculationMethod calculationMethod;
    private BonusPolicyStatus status;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private BigDecimal fixedAmount;
    private BigDecimal percentage;
    private BigDecimal minimumAmount;
    private BigDecimal maximumAmount;
    private BigDecimal targetValue;
    private BigDecimal minimumRating;
    private String ratingSlabsJson;
    private String formulaExpression;
    private String departmentId;
    private String designationId;
    private String employeeType;
    private String branchId;
    private BonusPaymentFrequency paymentFrequency;
    private Boolean approvalRequired;
    private Long policyVersion;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public BonusPolicyResponse() {}

    public static BonusPolicyResponse fromEntity(BonusPolicy policy) {
        if (policy == null) return null;
        BonusPolicyResponse dto = new BonusPolicyResponse();
        dto.setId(policy.getId());
        dto.setOrganizationId(policy.getOrganization() != null ? policy.getOrganization().getId() : null);
        dto.setName(policy.getName());
        dto.setDescription(policy.getDescription());
        dto.setBonusType(policy.getBonusType());
        dto.setCalculationMethod(policy.getCalculationMethod());
        dto.setStatus(policy.getStatus());
        dto.setEffectiveFrom(policy.getEffectiveFrom());
        dto.setEffectiveTo(policy.getEffectiveTo());
        dto.setFixedAmount(policy.getFixedAmount());
        dto.setPercentage(policy.getPercentage());
        dto.setMinimumAmount(policy.getMinimumAmount());
        dto.setMaximumAmount(policy.getMaximumAmount());
        dto.setTargetValue(policy.getTargetValue());
        dto.setMinimumRating(policy.getMinimumRating());
        dto.setRatingSlabsJson(policy.getRatingSlabsJson());
        dto.setFormulaExpression(policy.getFormulaExpression());
        dto.setDepartmentId(policy.getDepartmentId());
        dto.setDesignationId(policy.getDesignationId());
        dto.setEmployeeType(policy.getEmployeeType());
        dto.setBranchId(policy.getBranchId());
        dto.setPaymentFrequency(policy.getPaymentFrequency());
        dto.setApprovalRequired(policy.getApprovalRequired());
        dto.setPolicyVersion(policy.getPolicyVersion());
        dto.setCreatedAt(policy.getCreatedAt());
        dto.setUpdatedAt(policy.getUpdatedAt());
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public BonusPolicyStatus getStatus() {
        return status;
    }

    public void setStatus(BonusPolicyStatus status) {
        this.status = status;
    }

    public LocalDate getEffectiveFrom() {
        return effectiveFrom;
    }

    public void setEffectiveFrom(LocalDate effectiveFrom) {
        this.effectiveFrom = effectiveFrom;
    }

    public LocalDate getEffectiveTo() {
        return effectiveTo;
    }

    public void setEffectiveTo(LocalDate effectiveTo) {
        this.effectiveTo = effectiveTo;
    }

    public BigDecimal getFixedAmount() {
        return fixedAmount;
    }

    public void setFixedAmount(BigDecimal fixedAmount) {
        this.fixedAmount = fixedAmount;
    }

    public BigDecimal getPercentage() {
        return percentage;
    }

    public void setPercentage(BigDecimal percentage) {
        this.percentage = percentage;
    }

    public BigDecimal getMinimumAmount() {
        return minimumAmount;
    }

    public void setMinimumAmount(BigDecimal minimumAmount) {
        this.minimumAmount = minimumAmount;
    }

    public BigDecimal getMaximumAmount() {
        return maximumAmount;
    }

    public void setMaximumAmount(BigDecimal maximumAmount) {
        this.maximumAmount = maximumAmount;
    }

    public BigDecimal getTargetValue() {
        return targetValue;
    }

    public void setTargetValue(BigDecimal targetValue) {
        this.targetValue = targetValue;
    }

    public BigDecimal getMinimumRating() {
        return minimumRating;
    }

    public void setMinimumRating(BigDecimal minimumRating) {
        this.minimumRating = minimumRating;
    }

    public String getRatingSlabsJson() {
        return ratingSlabsJson;
    }

    public void setRatingSlabsJson(String ratingSlabsJson) {
        this.ratingSlabsJson = ratingSlabsJson;
    }

    public String getFormulaExpression() {
        return formulaExpression;
    }

    public void setFormulaExpression(String formulaExpression) {
        this.formulaExpression = formulaExpression;
    }

    public String getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(String departmentId) {
        this.departmentId = departmentId;
    }

    public String getDesignationId() {
        return designationId;
    }

    public void setDesignationId(String designationId) {
        this.designationId = designationId;
    }

    public String getEmployeeType() {
        return employeeType;
    }

    public void setEmployeeType(String employeeType) {
        this.employeeType = employeeType;
    }

    public String getBranchId() {
        return branchId;
    }

    public void setBranchId(String branchId) {
        this.branchId = branchId;
    }

    public BonusPaymentFrequency getPaymentFrequency() {
        return paymentFrequency;
    }

    public void setPaymentFrequency(BonusPaymentFrequency paymentFrequency) {
        this.paymentFrequency = paymentFrequency;
    }

    public Boolean getApprovalRequired() {
        return approvalRequired;
    }

    public void setApprovalRequired(Boolean approvalRequired) {
        this.approvalRequired = approvalRequired;
    }

    public Long getPolicyVersion() {
        return policyVersion;
    }

    public void setPolicyVersion(Long policyVersion) {
        this.policyVersion = policyVersion;
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
