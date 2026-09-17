package com.example.ems.offboarding.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public class AssignTemplateToRequestDto {

    @NotNull(message = "Template ID is required")
    @Schema(description = "ID of the offboarding template to assign to this exit request", example = "4")
    private Long templateId;

    public AssignTemplateToRequestDto() {}

    public AssignTemplateToRequestDto(Long templateId) {
        this.templateId = templateId;
    }

    public Long getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
    }
}
