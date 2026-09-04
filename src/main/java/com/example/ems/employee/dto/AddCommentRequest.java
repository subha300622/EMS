package com.example.ems.employee.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request body for adding a comment to an announcement")
public record AddCommentRequest(
    @Schema(description = "Comment content text", example = "Looking forward to this event!")
    @NotBlank(message = "Content is required")
    String content
) {}
