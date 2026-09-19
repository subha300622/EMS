package com.example.ems.support.dto;

public class PlatformUpdateTicketRequest {

    private String priority;
    private Long categoryId;
    private String status;

    public PlatformUpdateTicketRequest() {}

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
