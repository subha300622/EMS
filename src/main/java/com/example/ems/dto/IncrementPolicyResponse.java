package com.example.ems.dto;

import com.example.ems.entity.IncrementPolicy;
import java.math.BigDecimal;

public class IncrementPolicyResponse {
    private Long id;
    private Integer rating;
    private BigDecimal recommendedPercentage;
    private String description;

    public IncrementPolicyResponse() {}

    public IncrementPolicyResponse(IncrementPolicy policy) {
        this.id = policy.getId();
        this.rating = policy.getRating();
        this.recommendedPercentage = policy.getRecommendedPercentage();
        this.description = policy.getDescription();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public BigDecimal getRecommendedPercentage() { return recommendedPercentage; }
    public void setRecommendedPercentage(BigDecimal recommendedPercentage) { this.recommendedPercentage = recommendedPercentage; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
