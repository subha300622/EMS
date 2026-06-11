package com.example.ems.offboarding.dto;

import com.example.ems.employee.entity.Employee;
import com.example.ems.offboarding.entity.Offboarding;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class HandoverRequest {

    @NotNull(message = "Offboarding ID is required")
    private Long offboardingId;

    @NotBlank(message = "Task description/name is required")
    private String taskName;

    @NotNull(message = "Recipient Employee ID is required")
    private Long recipientId;

    public Long getOffboardingId() { return offboardingId; }
    public void setOffboardingId(Long offboardingId) { this.offboardingId = offboardingId; }

    public String getTaskName() { return taskName; }
    public void setTaskName(String taskName) { this.taskName = taskName; }

    public Long getRecipientId() { return recipientId; }
    public void setRecipientId(Long recipientId) { this.recipientId = recipientId; }
}
