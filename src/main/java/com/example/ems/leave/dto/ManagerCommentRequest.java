package com.example.ems.leave.dto;

import jakarta.validation.constraints.Size;

public class ManagerCommentRequest {
    @Size(max = 255, message = "Comment must not exceed 255 characters")
    private String comment;

    public ManagerCommentRequest() {}

    public ManagerCommentRequest(String comment) {
        this.comment = comment;
    }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}
