package com.example.ems.common.dto;
import io.swagger.v3.oas.annotations.media.Schema;

public class NotificationDetailResponse {
    @Schema(example = "1")
    private Long id;
    @Schema(example = "Project Deliverables")
    private String title;
    @Schema(example = "string")
    private String message;
    @Schema(example = "string")
    private String type;
    @Schema(example = "true")
    private boolean read;
    @Schema(example = "string")
    private String actionUrl;
    @Schema(example = "string")
    private String createdAt;

    public NotificationDetailResponse() {}

    public NotificationDetailResponse(Long id, String title, String message, String type, boolean read, String actionUrl, String createdAt) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.type = type;
        this.read = read;
        this.actionUrl = actionUrl;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public String getActionUrl() {
        return actionUrl;
    }

    public void setActionUrl(String actionUrl) {
        this.actionUrl = actionUrl;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
