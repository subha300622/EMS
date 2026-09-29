package com.example.ems.onboarding.dto.comment;

import jakarta.validation.constraints.NotBlank;

public class OnboardingCommentCreateRequest {

    @NotBlank(message = "comment is required")
    private String comment;

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}
