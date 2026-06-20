package com.example.ems.offboarding.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class ExitInterviewRequest {

    @NotNull(message = "Offboarding ID is required")
    @Schema(example = "1")
    private Long offboardingId;

    @NotNull(message = "Exit interview date is required")
    @Schema(example = "2026-06-19")
    private LocalDate interviewDate;

    @NotBlank(message = "Interviewer name is required")
    @Schema(example = "string")
    private String interviewerName;

    public Long getOffboardingId() {
        return offboardingId;
    }

    public void setOffboardingId(Long offboardingId) {
        this.offboardingId = offboardingId;
    }

    public LocalDate getInterviewDate() {
        return interviewDate;
    }

    public void setInterviewDate(LocalDate interviewDate) {
        this.interviewDate = interviewDate;
    }

    public String getInterviewerName() {
        return interviewerName;
    }

    public void setInterviewerName(String interviewerName) {
        this.interviewerName = interviewerName;
    }
}
