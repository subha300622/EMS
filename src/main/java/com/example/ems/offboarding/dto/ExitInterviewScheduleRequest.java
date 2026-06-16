package com.example.ems.offboarding.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class ExitInterviewScheduleRequest {

    @NotNull(message = "Preferred date is required")
    private LocalDate preferredDate;

    @NotBlank(message = "Preferred time is required")
    private String preferredTime;

    private String comments;

    public LocalDate getPreferredDate() { return preferredDate; }
    public void setPreferredDate(LocalDate preferredDate) { this.preferredDate = preferredDate; }

    public String getPreferredTime() { return preferredTime; }
    public void setPreferredTime(String preferredTime) { this.preferredTime = preferredTime; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }
}
