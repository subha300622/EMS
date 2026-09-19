package com.example.ems.incentive.entity;

import com.example.ems.employee.entity.Employee;
import com.example.ems.organization.entity.Organization;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "incentive_records", uniqueConstraints = {
        @UniqueConstraint(name = "uk_inc_record_org_emp_pol_period", columnNames = {"organization_id", "employee_id", "policy_id", "period_start", "period_end"})
})
public class IncentiveRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "policy_id", nullable = false)
    private IncentivePolicy policy;

    @Column(name = "policy_version", nullable = false)
    private Long policyVersion = 1L;

    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    @Enumerated(EnumType.STRING)
    @Column(name = "incentive_type", nullable = false)
    private IncentiveType incentiveType;

    @Enumerated(EnumType.STRING)
    @Column(name = "calculation_method", nullable = false)
    private IncentiveCalculationMethod calculationMethod;

    @Column(name = "target_value", precision = 15, scale = 2)
    private BigDecimal targetValue;

    @Column(name = "achieved_value", precision = 15, scale = 2)
    private BigDecimal achievedValue;

    @Column(name = "achievement_percentage", precision = 7, scale = 2)
    private BigDecimal achievementPercentage;

    @Column(name = "performance_rating", precision = 4, scale = 2)
    private BigDecimal performanceRating;

    @Column(name = "calculated_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal calculatedAmount;

    @Column(name = "adjusted_amount", precision = 15, scale = 2)
    private BigDecimal adjustedAmount;

    @Column(name = "approved_amount", precision = 15, scale = 2)
    private BigDecimal approvedAmount;

    @Column(name = "adjustment_reason", columnDefinition = "TEXT")
    private String adjustmentReason;

    @Column(name = "adjusted_by")
    private String adjustedBy;

    @Column(name = "adjusted_at")
    private LocalDateTime adjustedAt;

    @Column(name = "approved_by")
    private String approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "rejected_by")
    private String rejectedBy;

    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IncentiveStatus status = IncentiveStatus.CALCULATED;

    @Column(name = "workflow_instance_id", length = 100)
    private String workflowInstanceId;

    @Column(name = "payroll_run_id")
    private Long payrollRunId;

    @Enumerated(EnumType.STRING)
    @Column(name = "payroll_status", nullable = false)
    private IncentivePayrollStatus payrollStatus = IncentivePayrollStatus.PENDING;

    @Version
    private Long version = 0L;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public IncentiveRecord() {}

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final IncentiveRecord record = new IncentiveRecord();

        public Builder id(Long id) { record.setId(id); return this; }
        public Builder organization(Organization org) { record.setOrganization(org); return this; }
        public Builder organizationId(Long orgId) {
            Organization org = new Organization();
            org.setId(orgId);
            record.setOrganization(org);
            return this;
        }
        public Builder employee(Employee emp) { record.setEmployee(emp); return this; }
        public Builder employeeId(Long empId) {
            Employee emp = new Employee();
            emp.setId(empId);
            record.setEmployee(emp);
            return this;
        }
        public Builder policy(IncentivePolicy pol) { record.setPolicy(pol); return this; }
        public Builder policyVersion(Long ver) { record.setPolicyVersion(ver); return this; }
        public Builder periodStart(LocalDate start) { record.setPeriodStart(start); return this; }
        public Builder periodEnd(LocalDate end) { record.setPeriodEnd(end); return this; }
        public Builder incentiveType(IncentiveType type) { record.setIncentiveType(type); return this; }
        public Builder calculationMethod(IncentiveCalculationMethod method) { record.setCalculationMethod(method); return this; }
        public Builder targetValue(BigDecimal target) { record.setTargetValue(target); return this; }
        public Builder achievedValue(BigDecimal achieved) { record.setAchievedValue(achieved); return this; }
        public Builder achievementPercentage(BigDecimal pct) { record.setAchievementPercentage(pct); return this; }
        public Builder performanceRating(BigDecimal rating) { record.setPerformanceRating(rating); return this; }
        public Builder calculatedAmount(BigDecimal amt) { record.setCalculatedAmount(amt); return this; }
        public Builder adjustedAmount(BigDecimal amt) { record.setAdjustedAmount(amt); return this; }
        public Builder approvedAmount(BigDecimal amt) { record.setApprovedAmount(amt); return this; }
        public Builder adjustmentReason(String reason) { record.setAdjustmentReason(reason); return this; }
        public Builder adjustedBy(String by) { record.setAdjustedBy(by); return this; }
        public Builder adjustedAt(LocalDateTime at) { record.setAdjustedAt(at); return this; }
        public Builder approvedBy(String by) { record.setApprovedBy(by); return this; }
        public Builder approvedAt(LocalDateTime at) { record.setApprovedAt(at); return this; }
        public Builder rejectionReason(String reason) { record.setRejectionReason(reason); return this; }
        public Builder rejectedBy(String by) { record.setRejectedBy(by); return this; }
        public Builder rejectedAt(LocalDateTime at) { record.setRejectedAt(at); return this; }
        public Builder status(IncentiveStatus status) { record.setStatus(status); return this; }
        public Builder workflowInstanceId(String wfId) { record.setWorkflowInstanceId(wfId); return this; }
        public Builder payrollRunId(Long runId) { record.setPayrollRunId(runId); return this; }
        public Builder payrollStatus(IncentivePayrollStatus status) { record.setPayrollStatus(status); return this; }
        public Builder version(Long ver) { record.setVersion(ver); return this; }

        public IncentiveRecord build() {
            return record;
        }
    }

    /**
     * Resolves the effective payable amount: approvedAmount > adjustedAmount > calculatedAmount.
     */
    public BigDecimal getEffectiveAmount() {
        if (approvedAmount != null) return approvedAmount;
        if (adjustedAmount != null) return adjustedAmount;
        return calculatedAmount;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Organization getOrganization() { return organization; }
    public void setOrganization(Organization organization) { this.organization = organization; }

    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }

    public IncentivePolicy getPolicy() { return policy; }
    public void setPolicy(IncentivePolicy policy) { this.policy = policy; }

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
