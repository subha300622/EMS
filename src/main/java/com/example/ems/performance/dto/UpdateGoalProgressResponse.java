package com.example.ems.performance.dto;
import io.swagger.v3.oas.annotations.media.Schema;

public class UpdateGoalProgressResponse {
    @Schema(example = "string")
    private String message;
    @Schema(example = "100.00")
    private Double newProgress;
    @Schema(example = "ACTIVE")
    private String status;

    public UpdateGoalProgressResponse(String message, Double newProgress, String status) {
        this.message = message;
        this.newProgress = newProgress;
        this.status = status;
    }

    // Getters and Setters
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Double getNewProgress() { return newProgress; }
    public void setNewProgress(Double newProgress) { this.newProgress = newProgress; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
