package com.example.ems.performance.entity;

import com.example.ems.organization.entity.Organization;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "performance_review_cycles", indexes = {
        @Index(name = "idx_perf_cycle_org", columnList = "organization_id"),
        @Index(name = "idx_perf_cycle_status", columnList = "organization_id, status")
})
public class PerformanceReviewCycle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    @JsonIgnore
    private Organization organization;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 100)
    private String code;

    @Column(name = "period_type", nullable = false, length = 50)
    private String periodType; // ANNUAL, BIANNUAL, QUARTERLY, MONTHLY

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "self_review_deadline")
    private LocalDate selfReviewDeadline;

    @Column(name = "manager_review_deadline")
    private LocalDate managerReviewDeadline;

    @Column(nullable = false, length = 50)
    private String status = "DRAFT"; // DRAFT, ACTIVE, EVALUATION, COMPLETED, ARCHIVED

    @Column(name = "calculation_version", nullable = false)
    private Integer calculationVersion = 1;

    @Column(name = "formula_version", nullable = false, length = 20)
    private String formulaVersion = "v1.0";

    @Column(name = "kpi_weight", precision = 5, scale = 2, nullable = false)
    private BigDecimal kpiWeight = new BigDecimal("50.00");

    @Column(name = "manager_weight", precision = 5, scale = 2, nullable = false)
    private BigDecimal managerWeight = new BigDecimal("30.00");

    @Column(name = "self_weight", precision = 5, scale = 2, nullable = false)
    private BigDecimal selfWeight = new BigDecimal("10.00");

    @Column(name = "attendance_weight", precision = 5, scale = 2, nullable = false)
    private BigDecimal attendanceWeight = new BigDecimal("10.00");

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public PerformanceReviewCycle() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Organization getOrganization() { return organization; }
    public void setOrganization(Organization organization) { this.organization = organization; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getPeriodType() { return periodType; }
    public void setPeriodType(String periodType) { this.periodType = periodType; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public LocalDate getSelfReviewDeadline() { return selfReviewDeadline; }
    public void setSelfReviewDeadline(LocalDate selfReviewDeadline) { this.selfReviewDeadline = selfReviewDeadline; }

    public LocalDate getManagerReviewDeadline() { return managerReviewDeadline; }
    public void setManagerReviewDeadline(LocalDate managerReviewDeadline) { this.managerReviewDeadline = managerReviewDeadline; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getCalculationVersion() { return calculationVersion; }
    public void setCalculationVersion(Integer calculationVersion) { this.calculationVersion = calculationVersion; }

    public String getFormulaVersion() { return formulaVersion; }
    public void setFormulaVersion(String formulaVersion) { this.formulaVersion = formulaVersion; }

    public BigDecimal getKpiWeight() { return kpiWeight; }
    public void setKpiWeight(BigDecimal kpiWeight) { this.kpiWeight = kpiWeight; }

    public BigDecimal getManagerWeight() { return managerWeight; }
    public void setManagerWeight(BigDecimal managerWeight) { this.managerWeight = managerWeight; }

    public BigDecimal getSelfWeight() { return selfWeight; }
    public void setSelfWeight(BigDecimal selfWeight) { this.selfWeight = selfWeight; }

    public BigDecimal getAttendanceWeight() { return attendanceWeight; }
    public void setAttendanceWeight(BigDecimal attendanceWeight) { this.attendanceWeight = attendanceWeight; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
