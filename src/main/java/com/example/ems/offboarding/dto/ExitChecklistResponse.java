package com.example.ems.offboarding.dto;

import java.time.LocalDateTime;
import java.util.List;

public class ExitChecklistResponse {

    private List<ChecklistItem> checklist;
    private ChecklistSummary summary;

    public ExitChecklistResponse() {}

    public ExitChecklistResponse(List<ChecklistItem> checklist, ChecklistSummary summary) {
        this.checklist = checklist;
        this.summary = summary;
    }

    public List<ChecklistItem> getChecklist() { return checklist; }
    public void setChecklist(List<ChecklistItem> checklist) { this.checklist = checklist; }

    public ChecklistSummary getSummary() { return summary; }
    public void setSummary(ChecklistSummary summary) { this.summary = summary; }

    public static class ChecklistItem {
        private Long taskId;
        private String taskName;
        private String assignedTo;
        private String status;
        private LocalDateTime completedAt;
        private Boolean actionRequired;
        private List<String> allowedActions;
        private Long assetId;

        public ChecklistItem() {}

        public ChecklistItem(Long taskId, String taskName, String assignedTo, String status, LocalDateTime completedAt, Boolean actionRequired, List<String> allowedActions, Long assetId) {
            this.taskId = taskId;
            this.taskName = taskName;
            this.assignedTo = assignedTo;
            this.status = status;
            this.completedAt = completedAt;
            this.actionRequired = actionRequired;
            this.allowedActions = allowedActions;
            this.assetId = assetId;
        }

        public Long getTaskId() { return taskId; }
        public void setTaskId(Long taskId) { this.taskId = taskId; }

        public String getTaskName() { return taskName; }
        public void setTaskName(String taskName) { this.taskName = taskName; }

        public String getAssignedTo() { return assignedTo; }
        public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public LocalDateTime getCompletedAt() { return completedAt; }
        public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

        public Boolean getActionRequired() { return actionRequired; }
        public void setActionRequired(Boolean actionRequired) { this.actionRequired = actionRequired; }

        public List<String> getAllowedActions() { return allowedActions; }
        public void setAllowedActions(List<String> allowedActions) { this.allowedActions = allowedActions; }

        public Long getAssetId() { return assetId; }
        public void setAssetId(Long assetId) { this.assetId = assetId; }
    }

    public static class ChecklistSummary {
        private int completed;
        private int pending;

        public ChecklistSummary() {}

        public ChecklistSummary(int completed, int pending) {
            this.completed = completed;
            this.pending = pending;
        }

        public int getCompleted() { return completed; }
        public void setCompleted(int completed) { this.completed = completed; }

        public int getPending() { return pending; }
        public void setPending(int pending) { this.pending = pending; }
    }
}
