package com.example.ems.offboarding.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class SubmitResignationRequest {

    @NotBlank(message = "Reason is required")
    private String reason;

    @NotBlank(message = "Reason category is required")
    private String reasonCategory;

    @NotNull(message = "Resignation date is required")
    private LocalDate resignationDate;

    @NotNull(message = "Requested last working day is required")
    private LocalDate requestedLastWorkingDay;

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
