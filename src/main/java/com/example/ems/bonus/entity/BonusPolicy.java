package com.example.ems.bonus.entity;

import com.example.ems.organization.entity.Organization;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "bonus_policies", indexes = {
    @Index(name = "idx_bonus_policy_org_status", columnList = "organization_id, status"),
    @Index(name = "idx_bonus_policy_org_dates", columnList = "organization_id, effective_from, effective_to")
})
public class BonusPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "bonus_type", nullable = false)
    private BonusType bonusType = BonusType.PERFORMANCE;

    @Enumerated(EnumType.STRING)
    @Column(name = "calculation_method", nullable = false)
    private BonusCalculationMethod calculationMethod = BonusCalculationMethod.FIXED_AMOUNT;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BonusPolicyStatus status = BonusPolicyStatus.DRAFT;

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

    @Column(name = "minimum_rating", precision = 4, scale = 2)
    private BigDecimal minimumRating;

    @Column(name = "rating_slabs_json", columnDefinition = "TEXT")
    private String ratingSlabsJson;

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
    private BonusPaymentFrequency paymentFrequency = BonusPaymentFrequency.YEARLY;

    @Column(name = "approval_required", nullable = false)
    private Boolean approvalRequired = true;

    @Column(name = "policy_version", nullable = false)
    private Long policyVersion = 1L;

    @Version
    private Long version;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public BonusPolicy() {}

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
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
        return approvalRequired != null ? approvalRequired : true;
    }

    public void setApprovalRequired(Boolean approvalRequired) {
        this.approvalRequired = approvalRequired;
    }

    public Long getPolicyVersion() {
        return policyVersion != null ? policyVersion : 1L;
    }

    public void setPolicyVersion(Long policyVersion) {
        this.policyVersion = policyVersion;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
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
