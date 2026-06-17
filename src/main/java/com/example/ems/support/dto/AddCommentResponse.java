package com.example.ems.support.dto;

public class AddCommentResponse {
    private Long commentId;
    private String createdBy;
    private String createdAt;
    private String message;

    public AddCommentResponse() {}

    public AddCommentResponse(Long commentId, String createdBy, String createdAt, String message) {
        this.commentId = commentId;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.message = message;
    }

    public Long getCommentId() { return commentId; }
    public void setCommentId(Long commentId) { this.commentId = commentId; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
