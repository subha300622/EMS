package com.example.ems.common.dto;
import io.swagger.v3.oas.annotations.media.Schema;

public class ApprovalItemDto {
    @Schema(example = "string")
    private String id;
    @Schema(example = "string")
    private String type;
    @Schema(example = "string")
    private String requesterName;
    @Schema(example = "string")
    private String details;
    @Schema(example = "string")
    private String createdAt;
    @Schema(example = "ACTIVE")
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
