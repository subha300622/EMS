package com.example.ems.bonus.dto;

import com.example.ems.bonus.entity.BonusCalculationMethod;
import com.example.ems.bonus.entity.BonusPaymentFrequency;
import com.example.ems.bonus.entity.BonusType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public class BonusPolicyRequest {

    @NotBlank(message = "Policy name is required")
    private String name;

    private String description;

    @NotNull(message = "Bonus type is required")
    private BonusType bonusType = BonusType.PERFORMANCE;

    @NotNull(message = "Calculation method is required")
    private BonusCalculationMethod calculationMethod = BonusCalculationMethod.FIXED_AMOUNT;

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

    private BonusPaymentFrequency paymentFrequency = BonusPaymentFrequency.YEARLY;

    private Boolean approvalRequired = true;

    @NotNull(message = "Effective from date is required")
    private LocalDate effectiveFrom;

    private LocalDate effectiveTo;

    public BonusPolicyRequest() {}

    // Getters and Setters
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
}
