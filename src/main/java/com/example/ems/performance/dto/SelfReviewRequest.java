package com.example.ems.performance.dto;

import com.example.ems.employee.entity.Employee;

import jakarta.validation.constraints.NotNull;

public class SelfReviewRequest {

    @NotNull(message = "Employee ID is required")
    private Long employeeId;

    private Long cycleId;

    private String achievements;

    private String areasForImprovement;

    private String comments;

    private Integer rating; // 1-5

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public Long getCycleId() { return cycleId; }
    public void setCycleId(Long cycleId) { this.cycleId = cycleId; }

    public String getAchievements() { return achievements; }
    public void setAchievements(String achievements) { this.achievements = achievements; }

    public String getAreasForImprovement() { return areasForImprovement; }
    public void setAreasForImprovement(String areasForImprovement) { this.areasForImprovement = areasForImprovement; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
}
