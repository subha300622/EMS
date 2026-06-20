package com.example.ems.appraisal.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import com.example.ems.appraisal.entity.IncrementPolicy;

import java.math.BigDecimal;

public class IncrementPolicyResponse {
    @Schema(example = "1")
    private Long id;
    @Schema(example = "1")
    private Integer rating;
    @Schema(example = "100.00")
    private BigDecimal recommendedPercentage;
    @Schema(example = "Detailed description of the item")
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
