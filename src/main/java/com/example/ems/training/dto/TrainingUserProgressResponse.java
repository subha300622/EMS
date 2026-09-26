package com.example.ems.training.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Training User Progress Summary")
public record TrainingUserProgressResponse(
    @Schema(description = "Assignment ID", example = "1")
    Long assignmentId,
    @Schema(description = "Course Name", example = "AWS Cloud Practitioner")
    String courseName,
    @Schema(description = "Priority Level", example = "HIGH")
    String priority,
    @Schema(description = "Due Date", example = "2026-10-15")
    String dueDate,
    @Schema(description = "Status", example = "IN_PROGRESS")
    String status,
    @Schema(description = "Progress Percentage", example = "60")
    int progress,
    @Schema(description = "Assigned By", example = "HR Manager")
    String assignedBy
) {}
