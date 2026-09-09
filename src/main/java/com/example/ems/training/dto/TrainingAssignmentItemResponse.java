package com.example.ems.training.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Training Assignment Summary Item")
public record TrainingAssignmentItemResponse(
    @Schema(description = "Assignment ID", example = "1")
    Long assignmentId,
    @Schema(description = "Course Name", example = "AWS Cloud Practitioner")
    String courseName,
    @Schema(description = "Due Date", example = "2026-10-15")
    String dueDate,
    @Schema(description = "Priority Level", example = "HIGH")
    String priority,
    @Schema(description = "Number of assigned employees", example = "5")
    int assignedCount
) {}
