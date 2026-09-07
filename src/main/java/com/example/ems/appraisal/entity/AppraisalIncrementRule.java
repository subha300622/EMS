package com.example.ems.appraisal.entity;

import com.example.ems.organization.entity.Organization;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "appraisal_increment_rules")
public class AppraisalIncrementRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", nullable = false)
    private AppraisalIncrementPolicy policy;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performance_category_id")
    private AppraisalPerformanceCategory performanceCategory;

    @Column(name = "performance_category_name", length = 100)
    private String performanceCategoryName;

    @Column(name = "min_rating", nullable = false)
    private Double minRating;

    @Column(name = "max_rating", nullable = false)
    private Double maxRating;

    @Column(nullable = false)
    private Boolean eligible = true;

    @Column(name = "increment_percentage", nullable = false)
    private Double incrementPercentage = 0.0;

    @Column(name = "bonus_percentage")
    private Double bonusPercentage = 0.0;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public AppraisalIncrementPolicy getPolicy() { return policy; }
    public void setPolicy(AppraisalIncrementPolicy policy) { this.policy = policy; }

    public Organization getOrganization() { return organization; }
    public void setOrganization(Organization organization) { this.organization = organization; }

    public AppraisalPerformanceCategory getPerformanceCategory() { return performanceCategory; }
    public void setPerformanceCategory(AppraisalPerformanceCategory performanceCategory) {
        this.performanceCategory = performanceCategory;
        if (performanceCategory != null) {
            this.performanceCategoryName = performanceCategory.getName();
        }
    }

    public String getPerformanceCategoryName() { return performanceCategoryName; }
    public void setPerformanceCategoryName(String performanceCategoryName) { this.performanceCategoryName = performanceCategoryName; }

    public Double getMinRating() { return minRating; }
    public void setMinRating(Double minRating) { this.minRating = minRating; }

    public Double getMaxRating() { return maxRating; }
    public void setMaxRating(Double maxRating) { this.maxRating = maxRating; }

    public Boolean getEligible() { return eligible; }
    public void setEligible(Boolean eligible) { this.eligible = eligible; }

    public Double getIncrementPercentage() { return incrementPercentage; }
    public void setIncrementPercentage(Double incrementPercentage) { this.incrementPercentage = incrementPercentage; }

    public Double getBonusPercentage() { return bonusPercentage; }
    public void setBonusPercentage(Double bonusPercentage) { this.bonusPercentage = bonusPercentage; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
