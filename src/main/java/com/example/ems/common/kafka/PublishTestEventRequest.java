package com.example.ems.common.kafka;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request body for publishing test domain event")
public record PublishTestEventRequest(
    @Schema(description = "Test notification title", example = "Kafka Outbox Verification")
    String title,

    @Schema(description = "Test notification message", example = "Integration pipeline test successful.")
    String message,

    @Schema(description = "Target user ID", example = "1")
    Long userId
) {
    public String getEffectiveTitle() {
        return (title != null && !title.isBlank()) ? title : "Kafka Outbox Verification";
    }

    public String getEffectiveMessage() {
        return (message != null && !message.isBlank()) ? message : "Integration pipeline test successful.";
    }

    public Long getEffectiveUserId() {
        return userId != null ? userId : 1L;
    }
}
