package com.example.ems.appraisal.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "increment_policies")
public class IncrementPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Integer rating; // 1-5 rating

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal recommendedPercentage; // e.g. 10.00 for 10%

    private String description;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public BigDecimal getRecommendedPercentage() { return recommendedPercentage; }
    public void setRecommendedPercentage(BigDecimal recommendedPercentage) { this.recommendedPercentage = recommendedPercentage; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
