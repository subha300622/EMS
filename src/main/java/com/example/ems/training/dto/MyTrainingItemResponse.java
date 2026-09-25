package com.example.ems.training.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Employee training summary item")
public class MyTrainingItemResponse {

    @Schema(description = "Assignment ID", example = "101")
    private Long assignmentId;

    @Schema(description = "Course title", example = "Security Compliance Training")
    private String courseName;

    @Schema(description = "Priority level", example = "HIGH")
    private String priority;

    @Schema(description = "Due date string", example = "2026-10-15")
    private String dueDate;

    @Schema(description = "Training completion status", example = "IN_PROGRESS")
    private String status;

    @Schema(description = "Progress percentage", example = "75")
    private Integer progress;

    @Schema(description = "Assigned by name", example = "Jane Manager")
    private String assignedBy;

    public MyTrainingItemResponse() {}

    public Long getAssignmentId() { return assignmentId; }
    public void setAssignmentId(Long assignmentId) { this.assignmentId = assignmentId; }
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getProgress() { return progress; }
    public void setProgress(Integer progress) { this.progress = progress; }
    public String getAssignedBy() { return assignedBy; }
    public void setAssignedBy(String assignedBy) { this.assignedBy = assignedBy; }
}
