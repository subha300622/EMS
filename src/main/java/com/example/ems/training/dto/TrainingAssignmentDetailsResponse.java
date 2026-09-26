package com.example.ems.training.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;

@Schema(description = "Training assignment detailed view")
public class TrainingAssignmentDetailsResponse {

    @Schema(description = "Assignment ID", example = "101")
    private Long id;

    @Schema(description = "Course details summary")
    private Map<String, Object> course;

    @Schema(description = "Assigned employees summary list")
    private List<Map<String, Object>> assignedTo;

    @Schema(description = "Due date string", example = "2026-10-31")
    private String dueDate;

    @Schema(description = "Priority level", example = "HIGH")
    private String priority;

    @Schema(description = "Overall assignment status", example = "IN_PROGRESS")
    private String status;

    @Schema(description = "Average progress percentage", example = "50")
    private int progress;

    @Schema(description = "Assignment note", example = "Mandatory security training")
    private String note;

    public TrainingAssignmentDetailsResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Map<String, Object> getCourse() { return course; }
    public void setCourse(Map<String, Object> course) { this.course = course; }
    public List<Map<String, Object>> getAssignedTo() { return assignedTo; }
    public void setAssignedTo(List<Map<String, Object>> assignedTo) { this.assignedTo = assignedTo; }
    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = progress; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
