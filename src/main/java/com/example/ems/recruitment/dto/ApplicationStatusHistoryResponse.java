package com.example.ems.recruitment.dto;

import com.example.ems.recruitment.entity.ApplicationStatus;
import com.example.ems.recruitment.entity.ApplicationStatusHistory;
import java.time.LocalDateTime;

public class ApplicationStatusHistoryResponse {

    private Long id;
    private Long applicationId;
    private ApplicationStatus oldStatus;
    private ApplicationStatus newStatus;
    private String changedBy;
    private String reason;
    private LocalDateTime createdAt;

    public ApplicationStatusHistoryResponse() {}

    public ApplicationStatusHistoryResponse(ApplicationStatusHistory history) {
        this.id = history.getId();
        if (history.getApplication() != null) {
            this.applicationId = history.getApplication().getId();
        }
        this.oldStatus = history.getOldStatus();
        this.newStatus = history.getNewStatus();
        this.changedBy = history.getChangedBy();
        this.reason = history.getReason();
        this.createdAt = history.getCreatedAt();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getApplicationId() { return applicationId; }
    public void setApplicationId(Long applicationId) { this.applicationId = applicationId; }

    public ApplicationStatus getOldStatus() { return oldStatus; }
    public void setOldStatus(ApplicationStatus oldStatus) { this.oldStatus = oldStatus; }

    public ApplicationStatus getNewStatus() { return newStatus; }
    public void setNewStatus(ApplicationStatus newStatus) { this.newStatus = newStatus; }

    public String getChangedBy() { return changedBy; }
    public void setChangedBy(String changedBy) { this.changedBy = changedBy; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
