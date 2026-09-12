package com.example.ems.onboarding.dto.task;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class OnboardingTaskListResponse {

    private Long onboardingId;
    private int totalTasks;
    private int completedTasks;
    private int pendingTasks;
    private List<TaskItem> tasks;

    public Long getOnboardingId() { return onboardingId; }
    public void setOnboardingId(Long onboardingId) { this.onboardingId = onboardingId; }

    public int getTotalTasks() { return totalTasks; }
    public void setTotalTasks(int totalTasks) { this.totalTasks = totalTasks; }

    public int getCompletedTasks() { return completedTasks; }
    public void setCompletedTasks(int completedTasks) { this.completedTasks = completedTasks; }

    public int getPendingTasks() { return pendingTasks; }
    public void setPendingTasks(int pendingTasks) { this.pendingTasks = pendingTasks; }

    public List<TaskItem> getTasks() { return tasks; }
    public void setTasks(List<TaskItem> tasks) { this.tasks = tasks; }

    public static class TaskItem {
        private Long taskId;
        private String title;
        private String description;
        private Long phaseId;
        private String phaseName;
        private String status;
        private AssignedUser assignedTo;
        private LocalDate dueDate;
        private LocalDateTime completedAt;
        private boolean requiresDocument;
        private Long documentId;

        public Long getTaskId() { return taskId; }
        public void setTaskId(Long taskId) { this.taskId = taskId; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public Long getPhaseId() { return phaseId; }
        public void setPhaseId(Long phaseId) { this.phaseId = phaseId; }

        public String getPhaseName() { return phaseName; }
        public void setPhaseName(String phaseName) { this.phaseName = phaseName; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public AssignedUser getAssignedTo() { return assignedTo; }
        public void setAssignedTo(AssignedUser assignedTo) { this.assignedTo = assignedTo; }

        public LocalDate getDueDate() { return dueDate; }
        public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

        public LocalDateTime getCompletedAt() { return completedAt; }
        public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

        public boolean isRequiresDocument() { return requiresDocument; }
        public void setRequiresDocument(boolean requiresDocument) { this.requiresDocument = requiresDocument; }

        public Long getDocumentId() { return documentId; }
        public void setDocumentId(Long documentId) { this.documentId = documentId; }
    }

    public static class AssignedUser {
        private String employeeId;
        private String fullName;

        public AssignedUser(String employeeId, String fullName) {
            this.employeeId = employeeId;
            this.fullName = fullName;
        }

        public String getEmployeeId() { return employeeId; }
        public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
    }
}
