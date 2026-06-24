package com.example.ems.performance.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public class SelfAssessmentRequest {
    @Schema(example = "1.0")
    private Double selfRating;
    @Schema(example = "string")
    private String selfReview;
    private List<String> achievements;
    private List<String> strengths;
    private List<String> improvementAreas;

    private Double leadershipOwnershipRating;
    private Double technicalExcellenceRating;
    private Double deliveryManagementRating;
    private Double communicationInfluenceRating;
    private Double teamMentorshipRating;
    private Double innovationInitiativeRating;

    // Getters and Setters
    public Double getSelfRating() { return selfRating; }
    public void setSelfRating(Double selfRating) { this.selfRating = selfRating; }
    public String getSelfReview() { return selfReview; }
    public void setSelfReview(String selfReview) { this.selfReview = selfReview; }
    public List<String> getAchievements() { return achievements; }
    public void setAchievements(List<String> achievements) { this.achievements = achievements; }
    public List<String> getStrengths() { return strengths; }
    public void setStrengths(List<String> strengths) { this.strengths = strengths; }
    public List<String> getImprovementAreas() { return improvementAreas; }
    public void setImprovementAreas(List<String> improvementAreas) { this.improvementAreas = improvementAreas; }

    public Double getLeadershipOwnershipRating() { return leadershipOwnershipRating; }
    public void setLeadershipOwnershipRating(Double leadershipOwnershipRating) { this.leadershipOwnershipRating = leadershipOwnershipRating; }

    public Double getTechnicalExcellenceRating() { return technicalExcellenceRating; }
    public void setTechnicalExcellenceRating(Double technicalExcellenceRating) { this.technicalExcellenceRating = technicalExcellenceRating; }

    public Double getDeliveryManagementRating() { return deliveryManagementRating; }
    public void setDeliveryManagementRating(Double deliveryManagementRating) { this.deliveryManagementRating = deliveryManagementRating; }

    public Double getCommunicationInfluenceRating() { return communicationInfluenceRating; }
    public void setCommunicationInfluenceRating(Double communicationInfluenceRating) { this.communicationInfluenceRating = communicationInfluenceRating; }

    public Double getTeamMentorshipRating() { return teamMentorshipRating; }
    public void setTeamMentorshipRating(Double teamMentorshipRating) { this.teamMentorshipRating = teamMentorshipRating; }

    public Double getInnovationInitiativeRating() { return innovationInitiativeRating; }
    public void setInnovationInitiativeRating(Double innovationInitiativeRating) { this.innovationInitiativeRating = innovationInitiativeRating; }
}
