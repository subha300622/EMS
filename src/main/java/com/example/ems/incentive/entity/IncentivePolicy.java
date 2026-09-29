package com.example.ems.incentive.entity;

import com.example.ems.organization.entity.Organization;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "incentive_policies", indexes = {
    @Index(name = "idx_inc_policy_org_status", columnList = "organization_id, status"),
    @Index(name = "idx_inc_policy_org_dates", columnList = "organization_id, effective_from, effective_to")
})
public class IncentivePolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(nullable = false)
    private String name;

    @Transient
    private String policyCode;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "incentive_type", nullable = false)
    private IncentiveType incentiveType;

    @Enumerated(EnumType.STRING)
    @Column(name = "calculation_method", nullable = false)
    private IncentiveCalculationMethod calculationMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IncentivePolicyStatus status = IncentivePolicyStatus.DRAFT;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Column(name = "fixed_amount", precision = 15, scale = 2)
    private BigDecimal fixedAmount;

    @Column(precision = 7, scale = 4)
    private BigDecimal percentage;

    @Column(name = "minimum_amount", precision = 15, scale = 2)
    private BigDecimal minimumAmount;

    @Column(name = "maximum_amount", precision = 15, scale = 2)
    private BigDecimal maximumAmount;

    @Column(name = "target_value", precision = 15, scale = 2)
    private BigDecimal targetValue;

    @Column(name = "minimum_achievement_percentage", precision = 7, scale = 2)
    private BigDecimal minimumAchievementPercentage;

    @Column(name = "minimum_rating", precision = 4, scale = 2)
    private BigDecimal minimumRating;

    @Column(name = "target_slabs_json", columnDefinition = "TEXT")
    private String targetSlabsJson;

    @Column(name = "formula_expression", length = 500)
    private String formulaExpression;

    @Column(name = "department_id", length = 50)
    private String departmentId;

    @Column(name = "designation_id", length = 50)
    private String designationId;

    @Column(name = "employee_type", length = 50)
    private String employeeType;

    @Column(name = "branch_id", length = 50)
    private String branchId;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_frequency", nullable = false)
    private IncentivePaymentFrequency paymentFrequency = IncentivePaymentFrequency.MONTHLY;

    @Column(name = "approval_required", nullable = false)
    private Boolean approvalRequired = true;

    @Column(name = "policy_version", nullable = false)
    private Long policyVersion = 1L;

    @Version
    private Long version = 0L;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public IncentivePolicy() {}

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final IncentivePolicy policy = new IncentivePolicy();

        public Builder id(Long id) { policy.setId(id); return this; }
        public Builder organization(Organization org) { policy.setOrganization(org); return this; }
        public Builder name(String name) { policy.setName(name); return this; }
        public Builder policyCode(String code) { policy.setPolicyCode(code); return this; }
        public Builder policyName(String name) { policy.setName(name); return this; }
        public Builder description(String desc) { policy.setDescription(desc); return this; }
        public Builder incentiveType(IncentiveType type) { policy.setIncentiveType(type); return this; }
        public Builder calculationMethod(IncentiveCalculationMethod method) { policy.setCalculationMethod(method); return this; }
        public Builder status(IncentivePolicyStatus status) { policy.setStatus(status); return this; }
        public Builder effectiveFrom(LocalDate from) { policy.setEffectiveFrom(from); return this; }
        public Builder effectiveTo(LocalDate to) { policy.setEffectiveTo(to); return this; }
        public Builder fixedAmount(BigDecimal amt) { policy.setFixedAmount(amt); return this; }
        public Builder percentage(BigDecimal pct) { policy.setPercentage(pct); return this; }
        public Builder minimumAmount(BigDecimal min) { policy.setMinimumAmount(min); return this; }
        public Builder maximumAmount(BigDecimal max) { policy.setMaximumAmount(max); return this; }
        public Builder targetValue(BigDecimal target) { policy.setTargetValue(target); return this; }
        public Builder minimumAchievementPercentage(BigDecimal minAch) { policy.setMinimumAchievementPercentage(minAch); return this; }
        public Builder minimumRating(BigDecimal minRating) { policy.setMinimumRating(minRating); return this; }
        public Builder targetSlabsJson(String json) { policy.setTargetSlabsJson(json); return this; }
        public Builder formulaExpression(String formula) { policy.setFormulaExpression(formula); return this; }
        public Builder departmentId(String deptId) { policy.setDepartmentId(deptId); return this; }
        public Builder designationId(String desigId) { policy.setDesignationId(desigId); return this; }
        public Builder employeeType(String type) { policy.setEmployeeType(type); return this; }
        public Builder branchId(String branchId) { policy.setBranchId(branchId); return this; }
        public Builder paymentFrequency(IncentivePaymentFrequency freq) { policy.setPaymentFrequency(freq); return this; }
        public Builder approvalRequired(Boolean req) { policy.setApprovalRequired(req); return this; }
        public Builder policyVersion(Long ver) { policy.setPolicyVersion(ver); return this; }
        public Builder targetSlabs(java.util.List<com.example.ems.incentive.dto.IncentiveSlabTierDto> slabs) {
            if (slabs != null) {
                try {
                    policy.setTargetSlabsJson(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(slabs));
                } catch (Exception ignored) {}
            }
            return this;
        }

        public IncentivePolicy build() {
            if (policy.getName() == null && policy.getPolicyCode() != null) {
                policy.setName(policy.getPolicyCode());
            }
            if (policy.getPolicyCode() == null && policy.getName() != null) {
                policy.setPolicyCode(policy.getName());
            }
            return policy;
        }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Organization getOrganization() { return organization; }
    public void setOrganization(Organization organization) { this.organization = organization; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPolicyCode() { return policyCode != null ? policyCode : name; }
    public void setPolicyCode(String policyCode) { this.policyCode = policyCode; }

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

    public String getTargetSlabsJson() { return targetSlabsJson; }
    public void setTargetSlabsJson(String targetSlabsJson) { this.targetSlabsJson = targetSlabsJson; }

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
