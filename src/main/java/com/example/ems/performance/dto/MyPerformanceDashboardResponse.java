package com.example.ems.performance.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public class MyPerformanceDashboardResponse {
    @Schema(example = "string")
    private String employeeName;
    @Schema(example = "string")
    private String employeeId;
    @Schema(example = "Software Engineer")
    private String designation;
    @Schema(example = "Engineering")
    private String department;
    @Schema(example = "100.00")
    private Double overallRating;
    @Schema(example = "1")
    private Integer activeGoals;
    @Schema(example = "1")
    private Integer completedGoals;
    @Schema(example = "100.00")
    private Double goalCompletionPercentage;
    @Schema(example = "string")
    private String nextReviewDate;
    private List<RecentActivity> recentActivities;

    // Added fields
    private String selfReviewDueDate;
    private Double currentRating;
    private Integer goalsMet;
    private Integer totalGoals;
    private String reviewStatus;
    private String myBand;
    private SelfAssessmentRatings selfAssessment;

    public static class SelfAssessmentRatings {
        private Double leadershipOwnership;
        private Double technicalExcellence;
        private Double deliveryManagement;
        private Double communicationInfluence;
        private Double teamMentorship;
        private Double innovationInitiative;

        public SelfAssessmentRatings() {}
        public SelfAssessmentRatings(Double leadershipOwnership, Double technicalExcellence, Double deliveryManagement, Double communicationInfluence, Double teamMentorship, Double innovationInitiative) {
            this.leadershipOwnership = leadershipOwnership;
            this.technicalExcellence = technicalExcellence;
            this.deliveryManagement = deliveryManagement;
            this.communicationInfluence = communicationInfluence;
            this.teamMentorship = teamMentorship;
            this.innovationInitiative = innovationInitiative;
        }

        public Double getLeadershipOwnership() { return leadershipOwnership; }
        public void setLeadershipOwnership(Double leadershipOwnership) { this.leadershipOwnership = leadershipOwnership; }

        public Double getTechnicalExcellence() { return technicalExcellence; }
        public void setTechnicalExcellence(Double technicalExcellence) { this.technicalExcellence = technicalExcellence; }

        public Double getDeliveryManagement() { return deliveryManagement; }
        public void setDeliveryManagement(Double deliveryManagement) { this.deliveryManagement = deliveryManagement; }

        public Double getCommunicationInfluence() { return communicationInfluence; }
        public void setCommunicationInfluence(Double communicationInfluence) { this.communicationInfluence = communicationInfluence; }

        public Double getTeamMentorship() { return teamMentorship; }
        public void setTeamMentorship(Double teamMentorship) { this.teamMentorship = teamMentorship; }

        public Double getInnovationInitiative() { return innovationInitiative; }
        public void setInnovationInitiative(Double innovationInitiative) { this.innovationInitiative = innovationInitiative; }
    }

    public static class RecentActivity {
        @Schema(example = "string")
        private String event;
        @Schema(example = "string")
        private String date;

        public RecentActivity(String event, String date) {
            this.event = event;
            this.date = date;
        }

        public String getEvent() { return event; }
        public void setEvent(String event) { this.event = event; }
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
    }

    // Getters and Setters
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public Double getOverallRating() { return overallRating; }
    public void setOverallRating(Double overallRating) { this.overallRating = overallRating; }
    public Integer getActiveGoals() { return activeGoals; }
    public void setActiveGoals(Integer activeGoals) { this.activeGoals = activeGoals; }
    public Integer getCompletedGoals() { return completedGoals; }
    public void setCompletedGoals(Integer completedGoals) { this.completedGoals = completedGoals; }
    public Double getGoalCompletionPercentage() { return goalCompletionPercentage; }
    public void setGoalCompletionPercentage(Double goalCompletionPercentage) { this.goalCompletionPercentage = goalCompletionPercentage; }
    public String getNextReviewDate() { return nextReviewDate; }
    public void setNextReviewDate(String nextReviewDate) { this.nextReviewDate = nextReviewDate; }
    public List<RecentActivity> getRecentActivities() { return recentActivities; }
    public void setRecentActivities(List<RecentActivity> recentActivities) { this.recentActivities = recentActivities; }

    public String getSelfReviewDueDate() { return selfReviewDueDate; }
    public void setSelfReviewDueDate(String selfReviewDueDate) { this.selfReviewDueDate = selfReviewDueDate; }
    public Double getCurrentRating() { return currentRating; }
    public void setCurrentRating(Double currentRating) { this.currentRating = currentRating; }
    public Integer getGoalsMet() { return goalsMet; }
    public void setGoalsMet(Integer goalsMet) { this.goalsMet = goalsMet; }
    public Integer getTotalGoals() { return totalGoals; }
    public void setTotalGoals(Integer totalGoals) { this.totalGoals = totalGoals; }
    public String getReviewStatus() { return reviewStatus; }
    public void setReviewStatus(String reviewStatus) { this.reviewStatus = reviewStatus; }
    public String getMyBand() { return myBand; }
    public void setMyBand(String myBand) { this.myBand = myBand; }
    public SelfAssessmentRatings getSelfAssessment() { return selfAssessment; }
    public void setSelfAssessment(SelfAssessmentRatings selfAssessment) { this.selfAssessment = selfAssessment; }
}
