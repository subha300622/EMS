package com.example.ems.performance.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;

public class GoalCommentRequest {

    @NotBlank(message = "Comment is required")
    @Schema(example = "Excellent progress")
    private String comment;

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}
