package com.example.ems.support.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SupportTicketCommentRequest {

    @NotBlank(message = "Comment cannot be empty")
    @Size(max = 5000, message = "Comment cannot exceed 5000 characters")
    private String comment;

    private Boolean isInternal = false;

    public SupportTicketCommentRequest() {}

    public SupportTicketCommentRequest(String comment, Boolean isInternal) {
        this.comment = comment;
        this.isInternal = isInternal != null ? isInternal : false;
    }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public Boolean getIsInternal() { return isInternal != null ? isInternal : false; }
    public void setIsInternal(Boolean internal) { isInternal = internal; }
}
