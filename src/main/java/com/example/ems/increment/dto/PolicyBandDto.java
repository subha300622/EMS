package com.example.ems.increment.dto;

public class PolicyBandDto {

    private Double minRating;
    private Double maxRating;
    private Double incrementPercentage;

    public PolicyBandDto() {}

    public PolicyBandDto(Double minRating, Double maxRating, Double incrementPercentage) {
        this.minRating = minRating;
        this.maxRating = maxRating;
        this.incrementPercentage = incrementPercentage;
    }

    public Double getMinRating() { return minRating; }
    public void setMinRating(Double minRating) { this.minRating = minRating; }

    public Double getMaxRating() { return maxRating; }
    public void setMaxRating(Double maxRating) { this.maxRating = maxRating; }

    public Double getIncrementPercentage() { return incrementPercentage; }
    public void setIncrementPercentage(Double incrementPercentage) { this.incrementPercentage = incrementPercentage; }
}
