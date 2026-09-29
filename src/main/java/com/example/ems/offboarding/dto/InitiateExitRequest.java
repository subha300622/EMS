package com.example.ems.offboarding.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public class InitiateExitRequest {

    @NotNull(message = "Employee ID is required")
    @Schema(description = "ID of the employee being offboarded", example = "25")
    private Long employeeId;

    @Schema(description = "Exit type: RESIGNATION, TERMINATION, RETIREMENT, CONTRACT_END, etc.", example = "RESIGNATION")
    private String exitType = "RESIGNATION";

    @NotNull(message = "Last working date is required")
    @Schema(description = "Last working day / effective exit date", example = "2026-10-10")
    private LocalDate lastWorkingDate;

    @Schema(description = "Resignation submission date (required/used if exitType is RESIGNATION)", example = "2026-09-15")
    private LocalDate resignationDate;

    @Schema(description = "Notice period in days", example = "30")
    private Integer noticePeriodDays;

    @Schema(description = "Reason category", example = "CAREER_GROWTH")
    private String reasonCategory;

    @Schema(description = "Detailed reason or comments for exit", example = "Moving to another opportunity")
    private String reasonDetails;

    @Schema(description = "ID of the HR manager assigned to oversee this exit", example = "12")
    private Long assignedHrOwnerId;

    @Schema(description = "List of recipients to notify about exit initiation", example = "[\"EMPLOYEE\", \"DIRECT_MANAGER\", \"IT_TEAM\", \"FINANCE\"]")
    private List<String> notificationRecipients;

    public InitiateExitRequest() {}

    public InitiateExitRequest(Long employeeId, String exitType, LocalDate lastWorkingDate, LocalDate resignationDate,
                               Integer noticePeriodDays, String reasonCategory, String reasonDetails,
                               Long assignedHrOwnerId, List<String> notificationRecipients) {
        this.employeeId = employeeId;
        this.exitType = exitType;
        this.lastWorkingDate = lastWorkingDate;
        this.resignationDate = resignationDate;
        this.noticePeriodDays = noticePeriodDays;
        this.reasonCategory = reasonCategory;
        this.reasonDetails = reasonDetails;
        this.assignedHrOwnerId = assignedHrOwnerId;
        this.notificationRecipients = notificationRecipients;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public String getExitType() {
        return exitType;
    }

    public void setExitType(String exitType) {
        this.exitType = exitType;
    }

    public LocalDate getLastWorkingDate() {
        return lastWorkingDate;
    }

    public void setLastWorkingDate(LocalDate lastWorkingDate) {
        this.lastWorkingDate = lastWorkingDate;
    }

    public LocalDate getResignationDate() {
        return resignationDate;
    }

    public void setResignationDate(LocalDate resignationDate) {
        this.resignationDate = resignationDate;
    }

    public Integer getNoticePeriodDays() {
        return noticePeriodDays;
    }

    public void setNoticePeriodDays(Integer noticePeriodDays) {
        this.noticePeriodDays = noticePeriodDays;
    }

    public String getReasonCategory() {
        return reasonCategory;
    }

    public void setReasonCategory(String reasonCategory) {
        this.reasonCategory = reasonCategory;
    }

    public String getReasonDetails() {
        return reasonDetails;
    }

    public void setReasonDetails(String reasonDetails) {
        this.reasonDetails = reasonDetails;
    }

    public Long getAssignedHrOwnerId() {
        return assignedHrOwnerId;
    }

    public void setAssignedHrOwnerId(Long assignedHrOwnerId) {
        this.assignedHrOwnerId = assignedHrOwnerId;
    }

    public List<String> getNotificationRecipients() {
        return notificationRecipients;
    }

    public void setNotificationRecipients(List<String> notificationRecipients) {
        this.notificationRecipients = notificationRecipients;
    }
}
