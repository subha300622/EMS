package com.example.ems.appraisal.dto;

import java.util.ArrayList;
import java.util.List;

public class RatingScaleDto {

    private Long id;
    private String name;
    private String scaleType = "NUMERIC";
    private Double minRating = 1.0;
    private Double maxRating = 5.0;
    private Double stepValue = 0.1;
    private Boolean active = true;
    private List<RatingScaleLevelDto> levels = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getScaleType() { return scaleType; }
    public void setScaleType(String scaleType) { this.scaleType = scaleType; }

    public Double getMinRating() { return minRating; }
    public void setMinRating(Double minRating) { this.minRating = minRating; }

    public Double getMaxRating() { return maxRating; }
    public void setMaxRating(Double maxRating) { this.maxRating = maxRating; }

    public Double getStepValue() { return stepValue; }
    public void setStepValue(Double stepValue) { this.stepValue = stepValue; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public List<RatingScaleLevelDto> getLevels() { return levels; }
    public void setLevels(List<RatingScaleLevelDto> levels) { this.levels = levels; }
}
