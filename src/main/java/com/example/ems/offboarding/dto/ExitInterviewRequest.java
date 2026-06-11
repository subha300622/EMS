package com.example.ems.offboarding.dto;

import com.example.ems.offboarding.entity.Offboarding;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class ExitInterviewRequest {

    @NotNull(message = "Offboarding ID is required")
    private Long offboardingId;

    @NotNull(message = "Exit interview date is required")
    private LocalDate interviewDate;

    @NotBlank(message = "Interviewer name is required")
    private String interviewerName;

    public Long getOffboardingId() { return offboardingId; }
    public void setOffboardingId(Long offboardingId) { this.offboardingId = offboardingId; }

    public LocalDate getInterviewDate() { return interviewDate; }
    public void setInterviewDate(LocalDate interviewDate) { this.interviewDate = interviewDate; }

    public String getInterviewerName() { return interviewerName; }
    public void setInterviewerName(String interviewerName) { this.interviewerName = interviewerName; }
}
