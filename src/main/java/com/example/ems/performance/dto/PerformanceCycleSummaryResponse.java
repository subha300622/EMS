package com.example.ems.performance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Schema(description = "Performance Review Cycle Summary Analytics Report")
public class PerformanceCycleSummaryResponse {

    @Schema(description = "Cycle ID", example = "10")
    private Long cycleId;

    @Schema(description = "Report generation timestamp", example = "2026-09-18T10:00:00")
    private LocalDateTime generatedAt;

    @Schema(description = "Total reviews initiated in cycle", example = "150")
    private Long totalReviews;

    @Schema(description = "Total published or locked reviews", example = "120")
    private Long publishedReviews;

    @Schema(description = "Total reviews pending completion", example = "30")
    private Long pendingReviews;

    @Schema(description = "Average final score across evaluated employees", example = "3.85")
    private BigDecimal averageFinalScore;

    @Schema(description = "Rating band distribution count", example = "{\"OUTSTANDING\": 25, \"EXCEEDS_EXPECTATIONS\": 50, \"MEETS_EXPECTATIONS\": 60, \"NEEDS_IMPROVEMENT\": 10, \"UNSATISFACTORY\": 5}")
    private Map<String, Long> ratingDistribution;

    public PerformanceCycleSummaryResponse() {}

    public Long getCycleId() { return cycleId; }
    public void setCycleId(Long cycleId) { this.cycleId = cycleId; }

    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }

    public Long getTotalReviews() { return totalReviews; }
    public void setTotalReviews(Long totalReviews) { this.totalReviews = totalReviews; }

    public Long getPublishedReviews() { return publishedReviews; }
    public void setPublishedReviews(Long publishedReviews) { this.publishedReviews = publishedReviews; }

    public Long getPendingReviews() { return pendingReviews; }
    public void setPendingReviews(Long pendingReviews) { this.pendingReviews = pendingReviews; }

    public BigDecimal getAverageFinalScore() { return averageFinalScore; }
    public void setAverageFinalScore(BigDecimal averageFinalScore) { this.averageFinalScore = averageFinalScore; }

    public Map<String, Long> getRatingDistribution() { return ratingDistribution; }
    public void setRatingDistribution(Map<String, Long> ratingDistribution) { this.ratingDistribution = ratingDistribution; }
}
