package com.example.ems.training.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public class TrainingCourseRequest {

    @NotBlank(message = "Course title is required")
    @Schema(example = "Project Deliverables")
    private String title;

    @Schema(example = "Detailed description of the item")
    private String description;

    @Schema(example = "string")
    private String category;

    @Positive(message = "Duration hours must be positive")
    @Schema(example = "1")
    private Integer durationHours;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Integer getDurationHours() { return durationHours; }
    public void setDurationHours(Integer durationHours) { this.durationHours = durationHours; }
}
