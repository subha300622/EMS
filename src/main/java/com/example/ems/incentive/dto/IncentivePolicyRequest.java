package com.example.ems.incentive.dto;

import com.example.ems.incentive.entity.IncentiveCalculationMethod;
import com.example.ems.incentive.entity.IncentivePaymentFrequency;
import com.example.ems.incentive.entity.IncentiveType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class IncentivePolicyRequest {

    @Size(max = 255, message = "Policy name must not exceed 255 characters.")
    private String name;

    private String policyCode;

    private String description;

    @NotNull(message = "Incentive type is mandatory.")
    private IncentiveType incentiveType;

    @NotNull(message = "Calculation method is mandatory.")
    private IncentiveCalculationMethod calculationMethod;

    @NotNull(message = "Effective from date is mandatory.")
    private LocalDate effectiveFrom;

    private LocalDate effectiveTo;

    @DecimalMin(value = "0.0", message = "Fixed amount must be non-negative.")
    private BigDecimal fixedAmount;

    @DecimalMin(value = "0.0", message = "Percentage must be non-negative.")
    private BigDecimal percentage;

    @DecimalMin(value = "0.0", message = "Minimum amount must be non-negative.")
    private BigDecimal minimumAmount;

    @DecimalMin(value = "0.0", message = "Maximum amount must be non-negative.")
    private BigDecimal maximumAmount;

    @DecimalMin(value = "0.0", message = "Target value must be non-negative.")
    private BigDecimal targetValue;

    @DecimalMin(value = "0.0", message = "Minimum achievement percentage must be non-negative.")
    private BigDecimal minimumAchievementPercentage;

    @DecimalMin(value = "0.0", message = "Minimum rating must be non-negative.")
    private BigDecimal minimumRating;

    private List<IncentiveSlabTierDto> targetSlabs;

    @Size(max = 500, message = "Formula expression must not exceed 500 characters.")
    private String formulaExpression;

    private String departmentId;
    private String designationId;
    private String employeeType;
    private String branchId;

    private IncentivePaymentFrequency paymentFrequency = IncentivePaymentFrequency.MONTHLY;

    private Boolean approvalRequired = true;

    public IncentivePolicyRequest() {}

    // Getters and Setters
    public String getName() {
        return name != null ? name : policyCode;
    }
    public void setName(String name) {
        this.name = name;
        if (this.policyCode == null) this.policyCode = name;
    }

    public String getPolicyCode() {
        return policyCode != null ? policyCode : name;
    }
    public void setPolicyCode(String policyCode) {
        this.policyCode = policyCode;
        if (this.name == null) this.name = policyCode;
    }

    public String getPolicyName() {
        return name != null ? name : policyCode;
    }
    public void setPolicyName(String policyName) {
        this.name = policyName;
        if (this.policyCode == null) this.policyCode = policyName;
    }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public IncentiveType getIncentiveType() { return incentiveType; }
    public void setIncentiveType(IncentiveType incentiveType) { this.incentiveType = incentiveType; }

    public IncentiveCalculationMethod getCalculationMethod() { return calculationMethod; }
    public void setCalculationMethod(IncentiveCalculationMethod calculationMethod) { this.calculationMethod = calculationMethod; }

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
}
