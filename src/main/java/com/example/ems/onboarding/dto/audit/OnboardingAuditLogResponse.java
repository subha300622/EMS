package com.example.ems.onboarding.dto.audit;

import java.time.LocalDateTime;

public class OnboardingAuditLogResponse {

    private Long auditId;
    private Long onboardingId;
    private String action;
    private String entityType;
    private Long entityId;
    private String performedBy;
    private String performedByName;
    private LocalDateTime timestamp;
    private String details;

    public Long getAuditId() { return auditId; }
    public void setAuditId(Long auditId) { this.auditId = auditId; }

    public Long getOnboardingId() { return onboardingId; }
    public void setOnboardingId(Long onboardingId) { this.onboardingId = onboardingId; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }

    public Long getEntityId() { return entityId; }
    public void setEntityId(Long entityId) { this.entityId = entityId; }

    public String getPerformedBy() { return performedBy; }
    public void setPerformedBy(String performedBy) { this.performedBy = performedBy; }

    public String getPerformedByName() { return performedByName; }
    public void setPerformedByName(String performedByName) { this.performedByName = performedByName; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
}
