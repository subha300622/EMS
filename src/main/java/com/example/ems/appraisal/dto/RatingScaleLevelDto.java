package com.example.ems.appraisal.dto;

public class RatingScaleLevelDto {

    private Long id;
    private Long ratingScaleId;
    private String label;
    private Double minScore;
    private Double maxScore;
    private String description;
    private Integer levelOrder = 1;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getRatingScaleId() { return ratingScaleId; }
    public void setRatingScaleId(Long ratingScaleId) { this.ratingScaleId = ratingScaleId; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public Double getMinScore() { return minScore; }
    public void setMinScore(Double minScore) { this.minScore = minScore; }

    public Double getMaxScore() { return maxScore; }
    public void setMaxScore(Double maxScore) { this.maxScore = maxScore; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getLevelOrder() { return levelOrder; }
    public void setLevelOrder(Integer levelOrder) { this.levelOrder = levelOrder; }
}
