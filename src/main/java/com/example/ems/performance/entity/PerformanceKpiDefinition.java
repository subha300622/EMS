package com.example.ems.performance.entity;

import com.example.ems.organization.entity.Organization;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "performance_kpi_definitions", indexes = {
        @Index(name = "idx_perf_kpi_org", columnList = "organization_id"),
        @Index(name = "idx_perf_kpi_cycle", columnList = "cycle_id")
})
public class PerformanceKpiDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    @JsonIgnore
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cycle_id")
    private PerformanceReviewCycle cycle;

    @Column(nullable = false, length = 100)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 50)
    private String category; // TECHNICAL, OPERATIONAL, LEADERSHIP, CORE_VALUES, STRATEGIC

    @Column(name = "measurement_type", nullable = false, length = 50)
    private String measurementType; // HIGHER_IS_BETTER, LOWER_IS_BETTER, PERCENTAGE, BOOLEAN, RATING_BASED

    @Column(name = "default_weight", precision = 5, scale = 2, nullable = false)
    private BigDecimal defaultWeight = BigDecimal.ZERO;

    @Column(name = "min_threshold", precision = 10, scale = 2)
    private BigDecimal minThreshold = BigDecimal.ZERO;

    @Column(name = "max_threshold", precision = 10, scale = 2)
    private BigDecimal maxThreshold = new BigDecimal("100.00");

    @Column(name = "target_value", precision = 10, scale = 2)
    private BigDecimal targetValue;

    @Column(length = 50)
    private String unit;

    @Column(nullable = false, length = 50)
    private String status = "ACTIVE"; // DRAFT, ACTIVE, INACTIVE

    @Column(name = "is_mandatory", nullable = false)
    private Boolean isMandatory = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public PerformanceKpiDefinition() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Organization getOrganization() { return organization; }
    public void setOrganization(Organization organization) { this.organization = organization; }

    public PerformanceReviewCycle getCycle() { return cycle; }
    public void setCycle(PerformanceReviewCycle cycle) { this.cycle = cycle; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getMeasurementType() { return measurementType; }
    public void setMeasurementType(String measurementType) { this.measurementType = measurementType; }

    public BigDecimal getDefaultWeight() { return defaultWeight; }
    public void setDefaultWeight(BigDecimal defaultWeight) { this.defaultWeight = defaultWeight; }

    public BigDecimal getMinThreshold() { return minThreshold; }
    public void setMinThreshold(BigDecimal minThreshold) { this.minThreshold = minThreshold; }

    public BigDecimal getMaxThreshold() { return maxThreshold; }
    public void setMaxThreshold(BigDecimal maxThreshold) { this.maxThreshold = maxThreshold; }

    public BigDecimal getTargetValue() { return targetValue; }
    public void setTargetValue(BigDecimal targetValue) { this.targetValue = targetValue; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Boolean getIsMandatory() { return isMandatory; }
    public void setIsMandatory(Boolean isMandatory) { this.isMandatory = isMandatory; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
