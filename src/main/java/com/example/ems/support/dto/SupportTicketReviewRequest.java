package com.example.ems.support.dto;

import jakarta.validation.constraints.NotBlank;

public class SupportTicketReviewRequest {

    @NotBlank(message = "Action is required (ACCEPT, REJECT, DUPLICATE)")
    private String action;

    private String priority;
    private Double estimatedHours;
    private Long categoryId;
    private String reason;
    private Long duplicateOfTicketId;

    public SupportTicketReviewRequest() {}

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public Double getEstimatedHours() { return estimatedHours; }
    public void setEstimatedHours(Double estimatedHours) { this.estimatedHours = estimatedHours; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public Long getDuplicateOfTicketId() { return duplicateOfTicketId; }
    public void setDuplicateOfTicketId(Long duplicateOfTicketId) { this.duplicateOfTicketId = duplicateOfTicketId; }
}
