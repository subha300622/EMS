package com.example.ems.onboarding.dto;
import io.swagger.v3.oas.annotations.media.Schema;



import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class OnboardingTrainingRequest {

    @NotNull(message = "Onboarding ID is required")
    @Schema(example = "1")
    private Long onboardingId;

    @NotBlank(message = "Course name is required")
    @Schema(example = "string")
    private String courseName;

    public Long getOnboardingId() { return onboardingId; }
    public void setOnboardingId(Long onboardingId) { this.onboardingId = onboardingId; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
}
