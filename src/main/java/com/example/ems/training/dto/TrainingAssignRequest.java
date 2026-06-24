package com.example.ems.training.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public class TrainingAssignRequest {

    @NotNull(message = "Course ID is required")
    private Long courseId;

    private Long departmentId;

    private Long assignedBy;

    private List<Long> assignedToEmployeeIds;

    @NotNull(message = "Due date is required")
    private LocalDate dueDate;

    private String priority;

    private String note;

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public Long getAssignedBy() {
        return assignedBy;
    }

    public void setAssignedBy(Long assignedBy) {
        this.assignedBy = assignedBy;
    }

    public List<Long> getAssignedToEmployeeIds() {
        return assignedToEmployeeIds;
    }

    public void setAssignedToEmployeeIds(List<Long> assignedToEmployeeIds) {
        this.assignedToEmployeeIds = assignedToEmployeeIds;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
