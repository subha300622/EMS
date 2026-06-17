package com.example.ems.common.dto;

public class ApprovalItemDto {
    private String id;
    private String type;
    private String requesterName;
    private String details;
    private String createdAt;
    private String status;

    public ApprovalItemDto() {}

    public ApprovalItemDto(String id, String type, String requesterName, String details, String createdAt, String status) {
        this.id = id;
        this.type = type;
        this.requesterName = requesterName;
        this.details = details;
        this.createdAt = createdAt;
        this.status = status;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getRequesterName() { return requesterName; }
    public void setRequesterName(String requesterName) { this.requesterName = requesterName; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
