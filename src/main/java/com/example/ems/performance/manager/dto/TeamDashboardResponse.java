package com.example.ems.performance.manager.dto;

import java.util.List;

public class TeamDashboardResponse {
    private int totalReports;
    private int completed;
    private int pending;
    private double avgTeamRating;
    private String teamBand;
    private List<ReviewSummary> reviews;

    public int getTotalReports() { return totalReports; }
    public void setTotalReports(int totalReports) { this.totalReports = totalReports; }

    public int getCompleted() { return completed; }
    public void setCompleted(int completed) { this.completed = completed; }

    public int getPending() { return pending; }
    public void setPending(int pending) { this.pending = pending; }

    public double getAvgTeamRating() { return avgTeamRating; }
    public void setAvgTeamRating(double avgTeamRating) { this.avgTeamRating = avgTeamRating; }

    public String getTeamBand() { return teamBand; }
    public void setTeamBand(String teamBand) { this.teamBand = teamBand; }

    public List<ReviewSummary> getReviews() { return reviews; }
    public void setReviews(List<ReviewSummary> reviews) { this.reviews = reviews; }

    public static class ReviewSummary {
        private Long employeeId;
        private String employeeName;
        private String designation;
        private Double selfRating;
        private Double managerRating;
        private Integer goalsMet;
        private String finalScore; // Label e.g. "Exceptional"
        private String status;

        public Long getEmployeeId() { return employeeId; }
        public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

        public String getEmployeeName() { return employeeName; }
        public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

        public String getDesignation() { return designation; }
        public void setDesignation(String designation) { this.designation = designation; }

        public Double getSelfRating() { return selfRating; }
        public void setSelfRating(Double selfRating) { this.selfRating = selfRating; }

        public Double getManagerRating() { return managerRating; }
        public void setManagerRating(Double managerRating) { this.managerRating = managerRating; }

        public Integer getGoalsMet() { return goalsMet; }
        public void setGoalsMet(Integer goalsMet) { this.goalsMet = goalsMet; }

        public String getFinalScore() { return finalScore; }
        public void setFinalScore(String finalScore) { this.finalScore = finalScore; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}
