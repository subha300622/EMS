package com.example.ems.offboarding.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class HandoverRequest {

    @NotNull(message = "Offboarding ID is required")
    @Schema(example = "1")
    private Long offboardingId;

    @NotBlank(message = "Task description/name is required")
    @Schema(example = "string")
    private String taskName;

    @NotNull(message = "Recipient Employee ID is required")
    @Schema(example = "1")
    private Long recipientId;

    public Long getOffboardingId() {
        return offboardingId;
    }

    public void setOffboardingId(Long offboardingId) {
        this.offboardingId = offboardingId;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public Long getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(Long recipientId) {
        this.recipientId = recipientId;
    }
}
