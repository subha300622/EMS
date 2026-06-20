package com.example.ems.performance.dto;
import io.swagger.v3.oas.annotations.media.Schema;

public class GoalDecisionRequest {
    @Schema(example = "Excellent progress")
    private String comments;
    @Schema(example = "Personal business")
    private String reason;

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
