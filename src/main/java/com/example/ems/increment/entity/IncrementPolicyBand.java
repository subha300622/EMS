package com.example.ems.increment.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "increment_policy_bands", indexes = {
        @Index(name = "idx_increment_band_policy", columnList = "policy_id")
})
public class IncrementPolicyBand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", nullable = false)
    @JsonIgnore
    private IncrementPolicy policy;

    @Column(name = "min_rating", nullable = false)
    private Double minRating;

    @Column(name = "max_rating", nullable = false)
    private Double maxRating;

    @Column(name = "increment_percentage", nullable = false)
    private Double incrementPercentage;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public IncrementPolicyBand() {}

    public IncrementPolicyBand(Double minRating, Double maxRating, Double incrementPercentage) {
        this.minRating = minRating;
        this.maxRating = maxRating;
        this.incrementPercentage = incrementPercentage;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public IncrementPolicy getPolicy() { return policy; }
    public void setPolicy(IncrementPolicy policy) { this.policy = policy; }

    public Double getMinRating() { return minRating; }
    public void setMinRating(Double minRating) { this.minRating = minRating; }

    public Double getMaxRating() { return maxRating; }
    public void setMaxRating(Double maxRating) { this.maxRating = maxRating; }

    public Double getIncrementPercentage() { return incrementPercentage; }
    public void setIncrementPercentage(Double incrementPercentage) { this.incrementPercentage = incrementPercentage; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
