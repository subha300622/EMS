package com.example.ems.payroll.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "payroll_runs",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_payroll_run_period",
            columnNames = {"organization_id", "period_start", "period_end"}
        )
    }
)
public class PayrollRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private PayrollRunStatus status;

    @Column(name = "total_employees", nullable = false)
    private Integer totalEmployees = 0;

    @Column(name = "processed_employees", nullable = false)
    private Integer processedEmployees = 0;

    @Column(name = "total_gross", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalGross = BigDecimal.ZERO;

    @Column(name = "total_benefits", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalBenefits = BigDecimal.ZERO;

    @Column(name = "total_deductions", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalDeductions = BigDecimal.ZERO;

    @Column(name = "total_net", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalNet = BigDecimal.ZERO;

    @Column(name = "currency", nullable = false, length = 10)
    private String currency = "INR";

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "finalized_at")
    private LocalDateTime finalizedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public PayrollRun() {}

    public PayrollRun(Long organizationId, LocalDate periodStart, LocalDate periodEnd, String currency) {
        this.organizationId = organizationId;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.currency = currency != null ? currency : "INR";
        this.status = PayrollRunStatus.DRAFT;
        this.totalEmployees = 0;
        this.processedEmployees = 0;
        this.totalGross = BigDecimal.ZERO;
        this.totalBenefits = BigDecimal.ZERO;
        this.totalDeductions = BigDecimal.ZERO;
        this.totalNet = BigDecimal.ZERO;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = PayrollRunStatus.DRAFT;
        }
        if (this.currency == null) {
            this.currency = "INR";
        }
        if (this.totalGross == null) this.totalGross = BigDecimal.ZERO;
        if (this.totalBenefits == null) this.totalBenefits = BigDecimal.ZERO;
        if (this.totalDeductions == null) this.totalDeductions = BigDecimal.ZERO;
        if (this.totalNet == null) this.totalNet = BigDecimal.ZERO;
        if (this.totalEmployees == null) this.totalEmployees = 0;
        if (this.processedEmployees == null) this.processedEmployees = 0;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
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

    public PayrollRunStatus getStatus() {
        return status;
    }

    public void setStatus(PayrollRunStatus status) {
        this.status = status;
    }

    public Integer getTotalEmployees() {
        return totalEmployees;
    }

    public void setTotalEmployees(Integer totalEmployees) {
        this.totalEmployees = totalEmployees;
    }

    public Integer getProcessedEmployees() {
        return processedEmployees;
    }

    public void setProcessedEmployees(Integer processedEmployees) {
        this.processedEmployees = processedEmployees;
    }

    public BigDecimal getTotalGross() {
        return totalGross;
    }

    public void setTotalGross(BigDecimal totalGross) {
        this.totalGross = totalGross;
    }

    public BigDecimal getTotalBenefits() {
        return totalBenefits;
    }

    public void setTotalBenefits(BigDecimal totalBenefits) {
        this.totalBenefits = totalBenefits;
    }

    public BigDecimal getTotalDeductions() {
        return totalDeductions;
    }

    public void setTotalDeductions(BigDecimal totalDeductions) {
        this.totalDeductions = totalDeductions;
    }

    public BigDecimal getTotalNet() {
        return totalNet;
    }

    public void setTotalNet(BigDecimal totalNet) {
        this.totalNet = totalNet;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public Long getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(Long updatedBy) {
        this.updatedBy = updatedBy;
    }

    public LocalDateTime getFinalizedAt() {
        return finalizedAt;
    }

    public void setFinalizedAt(LocalDateTime finalizedAt) {
        this.finalizedAt = finalizedAt;
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
