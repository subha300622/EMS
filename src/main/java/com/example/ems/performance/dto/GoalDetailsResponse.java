package com.example.ems.performance.dto;

import java.util.List;

public class GoalDetailsResponse {
    private Long id;
    private String goalCode;
    private String title;
    private String description;
    private String category;
    private Integer weightage;
    private String target;
    private String achievement;
    private Double progressPercentage;
    private String status;
    private String priority;
    private String startDate;
    private String dueDate;
    private List<MilestoneDTO> milestones;
    private ReviewDTO employeeReview;
    private ReviewDTO managerReview;

    public static class MilestoneDTO {
        private Long id;
        private String title;
        private String status;

        public MilestoneDTO(Long id, String title, String status) {
            this.id = id;
            this.title = title;
            this.status = status;
        }

        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    public static class ReviewDTO {
        private Integer rating;
        private String comments;

        public ReviewDTO(Integer rating, String comments) {
            this.rating = rating;
            this.comments = comments;
        }

        // Getters and Setters
        public Integer getRating() { return rating; }
        public void setRating(Integer rating) { this.rating = rating; }
        public String getComments() { return comments; }
        public void setComments(String comments) { this.comments = comments; }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getGoalCode() { return goalCode; }
    public void setGoalCode(String goalCode) { this.goalCode = goalCode; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public Integer getWeightage() { return weightage; }
    public void setWeightage(Integer weightage) { this.weightage = weightage; }
    public String getTarget() { return target; }
    public void setTarget(String target) { this.target = target; }
    public String getAchievement() { return achievement; }
    public void setAchievement(String achievement) { this.achievement = achievement; }
    public Double getProgressPercentage() { return progressPercentage; }
    public void setProgressPercentage(Double progressPercentage) { this.progressPercentage = progressPercentage; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }
    public List<MilestoneDTO> getMilestones() { return milestones; }
    public void setMilestones(List<MilestoneDTO> milestones) { this.milestones = milestones; }
    public ReviewDTO getEmployeeReview() { return employeeReview; }
    public void setEmployeeReview(ReviewDTO employeeReview) { this.employeeReview = employeeReview; }
    public ReviewDTO getManagerReview() { return managerReview; }
    public void setManagerReview(ReviewDTO managerReview) { this.managerReview = managerReview; }
}
