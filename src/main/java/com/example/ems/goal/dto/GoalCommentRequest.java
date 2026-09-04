package com.example.ems.goal.dto;

import jakarta.validation.constraints.NotBlank;

public class GoalCommentRequest {

    @NotBlank(message = "Comment content is required")
    private String comment;

    private Long parentCommentId;

    public GoalCommentRequest() {}

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public Long getParentCommentId() { return parentCommentId; }
    public void setParentCommentId(Long parentCommentId) { this.parentCommentId = parentCommentId; }
}
