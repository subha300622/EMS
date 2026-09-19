package com.example.ems.offboarding.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

public class EmployeeTemplateAssignmentResponse {

    @Schema(example = "1")
    private Long id;

    @Schema(example = "25")
    private Long employeeId;

    @Schema(example = "John Doe")
    private String employeeName;

    @Schema(example = "EMP025")
    private String employeeCode;

    @Schema(example = "4")
    private Long templateId;

    @Schema(example = "Standard Resignation Template")
    private String templateName;

    @Schema(example = "RESIGNATION")
    private String exitType;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public EmployeeTemplateAssignmentResponse() {}

    public EmployeeTemplateAssignmentResponse(Long id, Long employeeId, String employeeName, String employeeCode,
                                              Long templateId, String templateName, String exitType,
                                              LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.employeeCode = employeeCode;
        this.templateId = templateId;
        this.templateName = templateName;
        this.exitType = exitType;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getExitType() {
        return exitType;
    }

    public void setExitType(String exitType) {
        this.exitType = exitType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
