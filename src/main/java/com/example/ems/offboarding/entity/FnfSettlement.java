package com.example.ems.offboarding.entity;

import com.example.ems.employee.entity.Employee;
import com.example.ems.organization.entity.Organization;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity(name = "ExitFnfSettlement")
@Table(name = "exit_fnf_settlements", uniqueConstraints = {
                @UniqueConstraint(name = "uq_exit_fnf_settlement_exit", columnNames = { "exit_id" })
}, indexes = {
                @Index(name = "idx_exit_fnf_settlements_org_status", columnList = "organization_id, status")
})
public class FnfSettlement {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "organization_id", nullable = false)
        @JsonIgnore
        private Organization organization;

        @OneToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "exit_id", nullable = false)
        @JsonIgnore
        private EmployeeExit exit;

        @Column(name = "salary_days_worked")
        private Integer salaryDaysWorked = 0;

        @Column(name = "salary_amount", precision = 15, scale = 2)
        private BigDecimal salaryAmount = BigDecimal.ZERO;

        @Column(name = "unpaid_salary", precision = 15, scale = 2)
        private BigDecimal unpaidSalary = BigDecimal.ZERO;

        @Column(name = "leave_encashment", precision = 15, scale = 2)
        private BigDecimal leaveEncashment = BigDecimal.ZERO;

        @Column(name = "bonus", precision = 15, scale = 2)
        private BigDecimal bonus = BigDecimal.ZERO;

        @Column(name = "incentives", precision = 15, scale = 2)
        private BigDecimal incentives = BigDecimal.ZERO;

        @Column(name = "overtime", precision = 15, scale = 2)
        private BigDecimal overtime = BigDecimal.ZERO;

        @Column(name = "reimbursements", precision = 15, scale = 2)
        private BigDecimal reimbursements = BigDecimal.ZERO;

        @Column(name = "gratuity", precision = 15, scale = 2)
        private BigDecimal gratuity = BigDecimal.ZERO;

        @Column(name = "other_allowances", precision = 15, scale = 2)
        private BigDecimal otherAllowances = BigDecimal.ZERO;

        @Column(name = "total_earnings", precision = 15, scale = 2)
        private BigDecimal totalEarnings = BigDecimal.ZERO;

        @Column(name = "notice_period_recovery", precision = 15, scale = 2)
        private BigDecimal noticePeriodRecovery = BigDecimal.ZERO;

        @Column(name = "asset_damage", precision = 15, scale = 2)
        private BigDecimal assetDamage = BigDecimal.ZERO;

        @Column(name = "tax_deduction", precision = 15, scale = 2)
        private BigDecimal taxDeduction = BigDecimal.ZERO;

        @Column(name = "loan_recovery", precision = 15, scale = 2)
        private BigDecimal loanRecovery = BigDecimal.ZERO;

        @Column(name = "other_deductions", precision = 15, scale = 2)
        private BigDecimal otherDeductions = BigDecimal.ZERO;

        @Column(name = "total_deductions", precision = 15, scale = 2)
        private BigDecimal totalDeductions = BigDecimal.ZERO;

        @Column(name = "net_settlement", precision = 15, scale = 2)
        private BigDecimal netSettlement = BigDecimal.ZERO;

        @Column(nullable = false, length = 50)
        private String status = "DRAFT";

        @Column(name = "payment_method", length = 50)
        private String paymentMethod;

        @Column(name = "payment_date")
        private LocalDate paymentDate;

        @Column(name = "payment_reference", length = 100)
        private String paymentReference;

        @Column(name = "payment_remarks", columnDefinition = "TEXT")
        private String paymentRemarks;

        @Column(name = "paid_amount", precision = 15, scale = 2)
        private BigDecimal paidAmount;

        @Column(name = "paid_at")
        private LocalDateTime paidAt;

        @Column(name = "idempotency_key", length = 100)
        private String idempotencyKey;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "paid_by_id")
        @JsonIgnoreProperties({ "manager", "team", "organization" })
        private Employee paidBy;

        @JdbcTypeCode(SqlTypes.JSON)
        @Column(name = "snapshot_data", columnDefinition = "jsonb")
        private String snapshotData;

        @Column(name = "snapshot_version")
        private Integer snapshotVersion = 1;

        @Column(name = "snapshot_hash", length = 64)
        private String snapshotHash;

        @Column(name = "snapshot_created_at")
        private LocalDateTime snapshotCreatedAt;

        @Column(name = "submitted_at")
        private LocalDateTime submittedAt;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "submitted_by_id")
        @JsonIgnoreProperties({ "manager", "team", "organization" })
        private Employee submittedBy;

        @Column(name = "finalized_at")
        private LocalDateTime finalizedAt;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "finalized_by_id")
        @JsonIgnoreProperties({ "manager", "team", "organization" })
        private Employee finalizedBy;

        @Column(name = "failure_reason", columnDefinition = "TEXT")
        private String failureReason;

        @Column(name = "payment_attempts")
        private Integer paymentAttempts = 0;

        @Version
        @Column(name = "version")
        private Long version = 0L;

        @Column(name = "created_at", nullable = false)
        private LocalDateTime createdAt = LocalDateTime.now();

        @Column(name = "updated_at", nullable = false)
        private LocalDateTime updatedAt = LocalDateTime.now();

        public FnfSettlement() {
        }

        public Long getId() {
                return id;
        }

        public void setId(Long id) {
                this.id = id;
        }

        public String getIdempotencyKey() {
                return idempotencyKey;
        }

        public void setIdempotencyKey(String idempotencyKey) {
                this.idempotencyKey = idempotencyKey;
        }

        public Organization getOrganization() {
                return organization;
        }

        public void setOrganization(Organization organization) {
                this.organization = organization;
        }

        public EmployeeExit getExit() {
                return exit;
        }

        public void setExit(EmployeeExit exit) {
                this.exit = exit;
        }

        public Integer getSalaryDaysWorked() {
                return salaryDaysWorked;
        }

        public void setSalaryDaysWorked(Integer salaryDaysWorked) {
                this.salaryDaysWorked = salaryDaysWorked;
        }

        public BigDecimal getSalaryAmount() {
                return salaryAmount;
        }

        public void setSalaryAmount(BigDecimal salaryAmount) {
                this.salaryAmount = salaryAmount;
        }

        public BigDecimal getUnpaidSalary() {
                return unpaidSalary;
        }

        public void setUnpaidSalary(BigDecimal unpaidSalary) {
                this.unpaidSalary = unpaidSalary;
        }

        public BigDecimal getLeaveEncashment() {
                return leaveEncashment;
        }

        public void setLeaveEncashment(BigDecimal leaveEncashment) {
                this.leaveEncashment = leaveEncashment;
        }

        public BigDecimal getBonus() {
                return bonus;
        }

        public void setBonus(BigDecimal bonus) {
                this.bonus = bonus;
        }

        public BigDecimal getIncentives() {
                return incentives;
        }

        public void setIncentives(BigDecimal incentives) {
                this.incentives = incentives;
        }

        public BigDecimal getOvertime() {
                return overtime;
        }

        public void setOvertime(BigDecimal overtime) {
                this.overtime = overtime;
        }

        public BigDecimal getReimbursements() {
                return reimbursements;
        }

        public void setReimbursements(BigDecimal reimbursements) {
                this.reimbursements = reimbursements;
        }

        public BigDecimal getGratuity() {
                return gratuity;
        }

        public void setGratuity(BigDecimal gratuity) {
                this.gratuity = gratuity;
        }

        public BigDecimal getOtherAllowances() {
                return otherAllowances;
        }

        public void setOtherAllowances(BigDecimal otherAllowances) {
                this.otherAllowances = otherAllowances;
        }

        public BigDecimal getTotalEarnings() {
                return totalEarnings;
        }

        public void setTotalEarnings(BigDecimal totalEarnings) {
                this.totalEarnings = totalEarnings;
        }

        public BigDecimal getNoticePeriodRecovery() {
                return noticePeriodRecovery;
        }

        public void setNoticePeriodRecovery(BigDecimal noticePeriodRecovery) {
                this.noticePeriodRecovery = noticePeriodRecovery;
        }

        public BigDecimal getAssetDamage() {
                return assetDamage;
        }

        public void setAssetDamage(BigDecimal assetDamage) {
                this.assetDamage = assetDamage;
        }

        public BigDecimal getTaxDeduction() {
                return taxDeduction;
        }

        public void setTaxDeduction(BigDecimal taxDeduction) {
                this.taxDeduction = taxDeduction;
        }

        public BigDecimal getOtherDeductions() {
                return otherDeductions;
        }

        public void setOtherDeductions(BigDecimal otherDeductions) {
                this.otherDeductions = otherDeductions;
        }

        public BigDecimal getTotalDeductions() {
                return totalDeductions;
        }

        public void setTotalDeductions(BigDecimal totalDeductions) {
                this.totalDeductions = totalDeductions;
        }

        public BigDecimal getNetSettlement() {
                return netSettlement;
        }

        public void setNetSettlement(BigDecimal netSettlement) {
                this.netSettlement = netSettlement;
        }

        public String getStatus() {
                return status;
        }

        public void setStatus(String status) {
                this.status = status;
        }

        public String getPaymentMethod() {
                return paymentMethod;
        }

        public void setPaymentMethod(String paymentMethod) {
                this.paymentMethod = paymentMethod;
        }

        public LocalDate getPaymentDate() {
                return paymentDate;
        }

        public void setPaymentDate(LocalDate paymentDate) {
                this.paymentDate = paymentDate;
        }

        public String getPaymentReference() {
                return paymentReference;
        }

        public void setPaymentReference(String paymentReference) {
                this.paymentReference = paymentReference;
        }

        public String getPaymentRemarks() {
                return paymentRemarks;
        }

        public void setPaymentRemarks(String paymentRemarks) {
                this.paymentRemarks = paymentRemarks;
        }

        public BigDecimal getPaidAmount() {
                return paidAmount;
        }

        public void setPaidAmount(BigDecimal paidAmount) {
                this.paidAmount = paidAmount;
        }

        public LocalDateTime getPaidAt() {
                return paidAt;
        }

        public void setPaidAt(LocalDateTime paidAt) {
                this.paidAt = paidAt;
        }

        public Employee getPaidBy() {
                return paidBy;
        }

        public void setPaidBy(Employee paidBy) {
                this.paidBy = paidBy;
        }

        public BigDecimal getLoanRecovery() {
                return loanRecovery;
        }

        public void setLoanRecovery(BigDecimal loanRecovery) {
                this.loanRecovery = loanRecovery;
        }

        public String getSnapshotData() {
                return snapshotData;
        }

        public void setSnapshotData(String snapshotData) {
                this.snapshotData = snapshotData;
        }

        public Integer getSnapshotVersion() {
                return snapshotVersion;
        }

        public void setSnapshotVersion(Integer snapshotVersion) {
                this.snapshotVersion = snapshotVersion;
        }

        public String getSnapshotHash() {
                return snapshotHash;
        }

        public void setSnapshotHash(String snapshotHash) {
                this.snapshotHash = snapshotHash;
        }

        public LocalDateTime getSnapshotCreatedAt() {
                return snapshotCreatedAt;
        }

        public void setSnapshotCreatedAt(LocalDateTime snapshotCreatedAt) {
                this.snapshotCreatedAt = snapshotCreatedAt;
        }

        public LocalDateTime getSubmittedAt() {
                return submittedAt;
        }

        public void setSubmittedAt(LocalDateTime submittedAt) {
                this.submittedAt = submittedAt;
        }

        public Employee getSubmittedBy() {
                return submittedBy;
        }

        public void setSubmittedBy(Employee submittedBy) {
                this.submittedBy = submittedBy;
        }

        public Long getSubmittedById() {
                return submittedBy != null ? submittedBy.getId() : null;
        }

        public LocalDateTime getFinalizedAt() {
                return finalizedAt;
        }

        public void setFinalizedAt(LocalDateTime finalizedAt) {
                this.finalizedAt = finalizedAt;
        }

        public Employee getFinalizedBy() {
                return finalizedBy;
        }

        public void setFinalizedBy(Employee finalizedBy) {
                this.finalizedBy = finalizedBy;
        }

        public Long getFinalizedById() {
                return finalizedBy != null ? finalizedBy.getId() : null;
        }

        public String getFailureReason() {
                return failureReason;
        }

        public void setFailureReason(String failureReason) {
                this.failureReason = failureReason;
        }

        public Integer getPaymentAttempts() {
                return paymentAttempts;
        }

        public void setPaymentAttempts(Integer paymentAttempts) {
                this.paymentAttempts = paymentAttempts;
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
