package com.example.ems.performance.dto;

import jakarta.validation.constraints.NotBlank;

public class GoalCommentRequest {

    @NotBlank(message = "Comment is required")
    private String comment;

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}
