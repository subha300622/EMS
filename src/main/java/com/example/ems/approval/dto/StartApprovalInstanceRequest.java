package com.example.ems.approval.dto;

import java.util.Map;

public class StartApprovalInstanceRequest {

    private String entityType; // e.g. "LEAVE_REQUEST", "SCHEDULE_SWAP"
    private String entityId;   // e.g. "LR-10001" or "10"
    private Map<String, Object> context;

    public StartApprovalInstanceRequest() {}

    public StartApprovalInstanceRequest(String entityType, String entityId, Map<String, Object> context) {
        this.entityType = entityType;
        this.entityId = entityId;
        this.context = context;
    }

    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }

    public String getEntityId() { return entityId; }
    public void setEntityId(String entityId) { this.entityId = entityId; }

    public Map<String, Object> getContext() { return context; }
    public void setContext(Map<String, Object> context) { this.context = context; }
}
