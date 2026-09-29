package com.example.ems.increment.entity;

import com.example.ems.organization.entity.Organization;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity(name = "EnterpriseIncrementPolicy")
@Table(name = "enterprise_increment_policies", indexes = {
        @Index(name = "idx_ent_inc_policy_org_active", columnList = "organization_id, active"),
        @Index(name = "idx_ent_inc_policy_org_ver", columnList = "organization_id, version")
})
public class IncrementPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    @JsonIgnore
    private Organization organization;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer version = 1;

    @Column(name = "appraisal_required", nullable = false)
    private Boolean appraisalRequired = false;

    @Column(name = "minimum_rating", nullable = false)
    private Double minimumRating = 3.0;

    @Column(name = "minimum_goal_achievement_percentage")
    private Double minimumGoalAchievementPercentage = 70.0;

    @Column(name = "minimum_attendance_percentage")
    private Double minimumAttendancePercentage = 90.0;

    @Column(name = "minimum_service_months")
    private Integer minimumServiceMonths = 12;

    @Column(name = "maximum_increment_percentage", nullable = false)
    private Double maximumIncrementPercentage = 20.0;

    @Column(name = "minimum_increment_percentage", nullable = false)
    private Double minimumIncrementPercentage = 0.0;

    @Enumerated(EnumType.STRING)
    @Column(name = "effective_date_rule", nullable = false)
    private EffectiveDateRule effectiveDateRule = EffectiveDateRule.FIXED_DATE;

    @Column(name = "effective_date")
    private LocalDate effectiveDate;

    @Column(name = "budget_limit", nullable = false, precision = 15, scale = 2)
    private BigDecimal budgetLimit = BigDecimal.ZERO;

    @Column(nullable = false)
    private Boolean active = true;

    @OneToMany(mappedBy = "policy", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<IncrementPolicyBand> bands = new ArrayList<>();

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void addBand(IncrementPolicyBand band) {
        bands.add(band);
        band.setPolicy(this);
    }

    public void removeBand(IncrementPolicyBand band) {
        bands.remove(band);
        band.setPolicy(null);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Organization getOrganization() { return organization; }
    public void setOrganization(Organization organization) { this.organization = organization; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }

    public Boolean getAppraisalRequired() { return appraisalRequired != null ? appraisalRequired : false; }
    public void setAppraisalRequired(Boolean appraisalRequired) { this.appraisalRequired = appraisalRequired; }

    public Double getMinimumRating() { return minimumRating; }
    public void setMinimumRating(Double minimumRating) { this.minimumRating = minimumRating; }

    public Double getMinimumGoalAchievementPercentage() { return minimumGoalAchievementPercentage; }
    public void setMinimumGoalAchievementPercentage(Double minimumGoalAchievementPercentage) { this.minimumGoalAchievementPercentage = minimumGoalAchievementPercentage; }

    public Double getMinimumAttendancePercentage() { return minimumAttendancePercentage; }
    public void setMinimumAttendancePercentage(Double minimumAttendancePercentage) { this.minimumAttendancePercentage = minimumAttendancePercentage; }

    public Integer getMinimumServiceMonths() { return minimumServiceMonths; }
    public void setMinimumServiceMonths(Integer minimumServiceMonths) { this.minimumServiceMonths = minimumServiceMonths; }

    public Double getMaximumIncrementPercentage() { return maximumIncrementPercentage; }
    public void setMaximumIncrementPercentage(Double maximumIncrementPercentage) { this.maximumIncrementPercentage = maximumIncrementPercentage; }

    public Double getMinimumIncrementPercentage() { return minimumIncrementPercentage; }
    public void setMinimumIncrementPercentage(Double minimumIncrementPercentage) { this.minimumIncrementPercentage = minimumIncrementPercentage; }

    public EffectiveDateRule getEffectiveDateRule() { return effectiveDateRule; }
    public void setEffectiveDateRule(EffectiveDateRule effectiveDateRule) { this.effectiveDateRule = effectiveDateRule; }

    public LocalDate getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }

    public BigDecimal getBudgetLimit() { return budgetLimit; }
    public void setBudgetLimit(BigDecimal budgetLimit) { this.budgetLimit = budgetLimit; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public List<IncrementPolicyBand> getBands() { return bands; }
    public void setBands(List<IncrementPolicyBand> bands) { this.bands = bands; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
