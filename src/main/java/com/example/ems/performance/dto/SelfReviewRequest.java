package com.example.ems.performance.dto;
import io.swagger.v3.oas.annotations.media.Schema;



import jakarta.validation.constraints.NotNull;

public class SelfReviewRequest {

    @NotNull(message = "Employee ID is required")
    @Schema(example = "1")
    private Long employeeId;

    @Schema(example = "1")
    private Long cycleId;

    @Schema(example = "string")
    private String achievements;

    @Schema(example = "string")
    private String areasForImprovement;

    @Schema(example = "Excellent progress")
    private String comments;

    @Schema(example = "1")
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
