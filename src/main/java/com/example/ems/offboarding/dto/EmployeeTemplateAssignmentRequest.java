package com.example.ems.offboarding.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public class EmployeeTemplateAssignmentRequest {

    @NotNull(message = "Template ID is required")
    @Schema(description = "ID of the offboarding template to assign", example = "4")
    private Long templateId;

    @Schema(description = "Exit type for this assignment: RESIGNATION, TERMINATION, RETIREMENT, CONTRACT_END, or ALL", example = "RESIGNATION")
    private String exitType = "ALL";

    public EmployeeTemplateAssignmentRequest() {}

    public EmployeeTemplateAssignmentRequest(Long templateId, String exitType) {
        this.templateId = templateId;
        this.exitType = exitType;
    }

    public Long getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
    }

    public String getExitType() {
        return exitType;
    }

    public void setExitType(String exitType) {
        this.exitType = exitType;
    }
}
