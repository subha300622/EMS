package com.example.ems.performance.entity;

import com.example.ems.employee.entity.Employee;
import com.example.ems.organization.entity.Organization;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "performance_calculation_runs", indexes = {
        @Index(name = "idx_perf_calc_runs_review", columnList = "review_id")
})
public class PerformanceCalculationRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    @JsonIgnore
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "review_id", nullable = false)
    @JsonIgnore
    private PerformanceReviewRecord review;

    @Column(name = "calculation_version", nullable = false)
    private Integer calculationVersion;

    @Column(name = "formula_version", nullable = false, length = 20)
    private String formulaVersion;

    @Column(name = "input_snapshot_hash", nullable = false, length = 64)
    private String inputSnapshotHash;

    @Column(name = "kpi_score", precision = 5, scale = 2, nullable = false)
    private BigDecimal kpiScore;

    @Column(name = "manager_score", precision = 5, scale = 2, nullable = false)
    private BigDecimal managerScore;

    @Column(name = "self_score", precision = 5, scale = 2, nullable = false)
    private BigDecimal selfScore;

    @Column(name = "attendance_score", precision = 5, scale = 2, nullable = false)
    private BigDecimal attendanceScore;

    @Column(name = "final_score", precision = 5, scale = 2, nullable = false)
    private BigDecimal finalScore;

    @Column(name = "rating_band", nullable = false, length = 50)
    private String ratingBand;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "calculated_by_id")
    @JsonIgnoreProperties({"manager", "team", "organization"})
    private Employee calculatedBy;

    @Column(name = "calculated_at", nullable = false)
    private LocalDateTime calculatedAt = LocalDateTime.now();

    public PerformanceCalculationRun() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Organization getOrganization() { return organization; }
    public void setOrganization(Organization organization) { this.organization = organization; }

    public PerformanceReviewRecord getReview() { return review; }
    public void setReview(PerformanceReviewRecord review) { this.review = review; }

    public Integer getCalculationVersion() { return calculationVersion; }
    public void setCalculationVersion(Integer calculationVersion) { this.calculationVersion = calculationVersion; }

    public String getFormulaVersion() { return formulaVersion; }
    public void setFormulaVersion(String formulaVersion) { this.formulaVersion = formulaVersion; }

    public String getInputSnapshotHash() { return inputSnapshotHash; }
    public void setInputSnapshotHash(String inputSnapshotHash) { this.inputSnapshotHash = inputSnapshotHash; }

    public BigDecimal getKpiScore() { return kpiScore; }
    public void setKpiScore(BigDecimal kpiScore) { this.kpiScore = kpiScore; }

    public BigDecimal getManagerScore() { return managerScore; }
    public void setManagerScore(BigDecimal managerScore) { this.managerScore = managerScore; }

    public BigDecimal getSelfScore() { return selfScore; }
    public void setSelfScore(BigDecimal selfScore) { this.selfScore = selfScore; }

    public BigDecimal getAttendanceScore() { return attendanceScore; }
    public void setAttendanceScore(BigDecimal attendanceScore) { this.attendanceScore = attendanceScore; }

    public BigDecimal getFinalScore() { return finalScore; }
    public void setFinalScore(BigDecimal finalScore) { this.finalScore = finalScore; }

    public String getRatingBand() { return ratingBand; }
    public void setRatingBand(String ratingBand) { this.ratingBand = ratingBand; }

    public Employee getCalculatedBy() { return calculatedBy; }
    public void setCalculatedBy(Employee calculatedBy) { this.calculatedBy = calculatedBy; }

    public LocalDateTime getCalculatedAt() { return calculatedAt; }
    public void setCalculatedAt(LocalDateTime calculatedAt) { this.calculatedAt = calculatedAt; }
}
