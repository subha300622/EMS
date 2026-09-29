package com.example.ems.training.dto;

import com.example.ems.training.entity.TrainingFeedback;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Training feedback summary metrics")
public class TrainingFeedbackSummaryResponse {

    @Schema(description = "Training program ID", example = "10")
    private Long trainingId;

    @Schema(description = "Total feedback responses count", example = "25")
    private int totalResponses;

    @Schema(description = "Average overall rating", example = "4.5")
    private double averageRating;

    @Schema(description = "Average content quality rating", example = "4.3")
    private double averageContentQuality;

    @Schema(description = "Average trainer rating", example = "4.7")
    private double averageTrainerRating;

    @Schema(description = "List of detailed feedback submissions")
    private List<TrainingFeedback> feedbacks;

    public TrainingFeedbackSummaryResponse() {}

    public Long getTrainingId() { return trainingId; }
    public void setTrainingId(Long trainingId) { this.trainingId = trainingId; }
    public int getTotalResponses() { return totalResponses; }
    public void setTotalResponses(int totalResponses) { this.totalResponses = totalResponses; }
    public double getAverageRating() { return averageRating; }
    public void setAverageRating(double averageRating) { this.averageRating = averageRating; }
    public double getAverageContentQuality() { return averageContentQuality; }
    public void setAverageContentQuality(double averageContentQuality) { this.averageContentQuality = averageContentQuality; }
    public double getAverageTrainerRating() { return averageTrainerRating; }
    public void setAverageTrainerRating(double averageTrainerRating) { this.averageTrainerRating = averageTrainerRating; }
    public List<TrainingFeedback> getFeedbacks() { return feedbacks; }
    public void setFeedbacks(List<TrainingFeedback> feedbacks) { this.feedbacks = feedbacks; }
}
