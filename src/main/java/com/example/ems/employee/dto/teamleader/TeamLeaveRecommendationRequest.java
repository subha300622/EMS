package com.example.ems.employee.dto.teamleader;

import jakarta.validation.constraints.NotBlank;

public class TeamLeaveRecommendationRequest {

    @NotBlank(message = "Decision is required")
    private String decision;

    private String comment;

    public TeamLeaveRecommendationRequest() {}

    public TeamLeaveRecommendationRequest(String decision, String comment) {
        this.decision = decision;
        this.comment = comment;
    }

    public String getDecision() { return decision; }
    public void setDecision(String decision) { this.decision = decision; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}
