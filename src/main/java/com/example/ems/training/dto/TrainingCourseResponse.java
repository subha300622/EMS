package com.example.ems.training.dto;

import com.example.ems.training.entity.TrainingCourse;

import java.time.LocalDateTime;

public class TrainingCourseResponse {
    private Long id;
    private String title;
    private String description;
    private String category;
    private String status;
    private Integer durationHours;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public TrainingCourseResponse() {}

    public TrainingCourseResponse(TrainingCourse course) {
        this.id = course.getId();
        this.title = course.getTitle();
        this.description = course.getDescription();
        this.category = course.getCategory();
        this.status = course.getStatus();
        this.durationHours = course.getDurationHours();
        this.createdAt = course.getCreatedAt();
        this.updatedAt = course.getUpdatedAt();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getDurationHours() { return durationHours; }
    public void setDurationHours(Integer durationHours) { this.durationHours = durationHours; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
