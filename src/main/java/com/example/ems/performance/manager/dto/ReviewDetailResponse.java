package com.example.ems.performance.manager.dto;

import java.util.List;

public class ReviewDetailResponse {
    private EmployeeInfo employee;
    private ReviewSummary summary;
    private List<CompetencyInfo> competencies;
    private List<GoalInfo> goals;

    public EmployeeInfo getEmployee() { return employee; }
    public void setEmployee(EmployeeInfo employee) { this.employee = employee; }

    public ReviewSummary getSummary() { return summary; }
    public void setSummary(ReviewSummary summary) { this.summary = summary; }

    public List<CompetencyInfo> getCompetencies() { return competencies; }
    public void setCompetencies(List<CompetencyInfo> competencies) { this.competencies = competencies; }

    public List<GoalInfo> getGoals() { return goals; }
    public void setGoals(List<GoalInfo> goals) { this.goals = goals; }

    public static class EmployeeInfo {
        private Long id;
        private String name;
        private String department;
        private String role;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDepartment() { return department; }
        public void setDepartment(String department) { this.department = department; }

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
    }

    public static class ReviewSummary {
        private String finalScore; // Label e.g. "Exceptional"
        private Double managerRating;
        private Double selfRating;
        private Integer goalsMet;

        public String getFinalScore() { return finalScore; }
        public void setFinalScore(String finalScore) { this.finalScore = finalScore; }

        public Double getManagerRating() { return managerRating; }
        public void setManagerRating(Double managerRating) { this.managerRating = managerRating; }

        public Double getSelfRating() { return selfRating; }
        public void setSelfRating(Double selfRating) { this.selfRating = selfRating; }

        public Integer getGoalsMet() { return goalsMet; }
        public void setGoalsMet(Integer goalsMet) { this.goalsMet = goalsMet; }
    }

    public static class CompetencyInfo {
        private String name;
        private Double selfScore;
        private Double managerScore;
        private String feedback;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public Double getSelfScore() { return selfScore; }
        public void setSelfScore(Double selfScore) { this.selfScore = selfScore; }

        public Double getManagerScore() { return managerScore; }
        public void setManagerScore(Double managerScore) { this.managerScore = managerScore; }

        public String getFeedback() { return feedback; }
        public void setFeedback(String feedback) { this.feedback = feedback; }
    }

    public static class GoalInfo {
        private String title;
        private Integer progress;
        private String status; // e.g. MET / NOT_MET

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public Integer getProgress() { return progress; }
        public void setProgress(Integer progress) { this.progress = progress; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}
