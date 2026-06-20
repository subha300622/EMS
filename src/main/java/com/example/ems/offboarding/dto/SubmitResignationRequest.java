package com.example.ems.offboarding.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class SubmitResignationRequest {

    @NotBlank(message = "Reason is required")
    @Schema(example = "Personal business")
    private String reason;

    @NotBlank(message = "Reason category is required")
    @Schema(example = "Personal business")
    private String reasonCategory;

    @NotNull(message = "Resignation date is required")
    @Schema(example = "2026-06-19")
    private LocalDate resignationDate;

    @NotNull(message = "Requested last working day is required")
    @Schema(example = "2026-06-19")
    private LocalDate requestedLastWorkingDay;

    @Schema(example = "Excellent progress")
    private String comments;

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getReasonCategory() { return reasonCategory; }
    public void setReasonCategory(String reasonCategory) { this.reasonCategory = reasonCategory; }

    public LocalDate getResignationDate() { return resignationDate; }
    public void setResignationDate(LocalDate resignationDate) { this.resignationDate = resignationDate; }

    public LocalDate getRequestedLastWorkingDay() { return requestedLastWorkingDay; }
    public void setRequestedLastWorkingDay(LocalDate requestedLastWorkingDay) { this.requestedLastWorkingDay = requestedLastWorkingDay; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }
}
