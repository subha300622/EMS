package com.example.ems.offboarding.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class InitiateExitResponse {

    @Schema(description = "Offboarding ID", example = "101")
    private Long offboardingId;

    @Schema(description = "Employee ID", example = "25")
    private Long employeeId;

    @Schema(description = "Employee Full Name", example = "John Doe")
    private String employeeName;

    @Schema(description = "Employee Code", example = "EMP025")
    private String employeeCode;

    @Schema(description = "Exit Type", example = "RESIGNATION")
    private String exitType;

    @Schema(description = "Offboarding Status", example = "PENDING")
    private String status;

    @Schema(description = "Current stage in workflow", example = "MANAGER_APPROVAL")
    private String currentStage;

    @Schema(description = "Applied Offboarding Template ID", example = "4")
    private Long templateId;

    @Schema(description = "Applied Offboarding Template Name", example = "Engineering Standard Offboarding")
    private String templateName;

    @Schema(description = "Last working date", example = "2026-10-10")
    private LocalDate lastWorkingDate;

    @Schema(description = "Resignation date", example = "2026-09-15")
    private LocalDate resignationDate;

    @Schema(description = "Reason Category", example = "CAREER_GROWTH")
    private String reasonCategory;

    @Schema(description = "Assigned HR Owner ID", example = "12")
    private Long assignedHrOwnerId;

    @Schema(description = "Creation Timestamp")
    private LocalDateTime createdAt;

    public InitiateExitResponse() {}

    public InitiateExitResponse(Long offboardingId, Long employeeId, String employeeName, String employeeCode,
                                String exitType, String status, String currentStage, Long templateId,
                                String templateName, LocalDate lastWorkingDate, LocalDate resignationDate,
                                String reasonCategory, Long assignedHrOwnerId, LocalDateTime createdAt) {
        this.offboardingId = offboardingId;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.employeeCode = employeeCode;
        this.exitType = exitType;
        this.status = status;
        this.currentStage = currentStage;
        this.templateId = templateId;
        this.templateName = templateName;
        this.lastWorkingDate = lastWorkingDate;
        this.resignationDate = resignationDate;
        this.reasonCategory = reasonCategory;
        this.assignedHrOwnerId = assignedHrOwnerId;
        this.createdAt = createdAt;
    }

    public Long getOffboardingId() {
        return offboardingId;
    }

    public void setOffboardingId(Long offboardingId) {
        this.offboardingId = offboardingId;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    public String getExitType() {
        return exitType;
    }

    public void setExitType(String exitType) {
        this.exitType = exitType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCurrentStage() {
        return currentStage;
    }

    public void setCurrentStage(String currentStage) {
        this.currentStage = currentStage;
    }

    public Long getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
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

    public String getReasonCategory() {
        return reasonCategory;
    }

    public void setReasonCategory(String reasonCategory) {
        this.reasonCategory = reasonCategory;
    }

    public Long getAssignedHrOwnerId() {
        return assignedHrOwnerId;
    }

    public void setAssignedHrOwnerId(Long assignedHrOwnerId) {
        this.assignedHrOwnerId = assignedHrOwnerId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
