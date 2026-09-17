package com.example.ems.offboarding.dto;

import com.example.ems.offboarding.enums.ClearanceAssignToType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Knowledge Transfer (KT) Template response")
public class KtTemplateResponse {

    @Schema(description = "KT Template ID", example = "1")
    private Long id;

    @Schema(description = "Organization ID", example = "9645")
    private Long organizationId;

    @Schema(description = "Parent Offboarding Template ID", example = "1")
    private Long templateId;

    @Schema(description = "Whether KT plan is mandatory", example = "true")
    private Boolean required;

    @Schema(description = "KT template title", example = "Engineering Knowledge Transfer Plan")
    private String title;

    @Schema(description = "KT requirements description", example = "Transfer ownership of codebase, documentation, and credentials")
    private String description;

    @Schema(description = "Receiver role assigned to verify KT", example = "REPORTING_MANAGER")
    private ClearanceAssignToType assignToType;

    @Schema(description = "Specific recipient user ID if SPECIFIC_USER", example = "102")
    private Long assignedUserId;

    @Schema(description = "Standard checklist of employee responsibilities", example = "[\"Document architecture\", \"Handover runbooks\"]")
    private List<String> employeeResponsibilities;

    @Schema(description = "Whether handover document attachment is required", example = "true")
    private Boolean handoverDocumentRequired;

    @Schema(description = "Whether handover signoff requires manager approval", example = "true")
    private Boolean handoverApprovalRequired;

    @Schema(description = "Due days before Last Working Day", example = "5")
    private Integer dueBeforeLwdDays;

    @Schema(description = "Priority level", example = "HIGH")
    private String priority;

    @Schema(description = "Whether completion is mandatory", example = "true")
    private Boolean mandatory;

    @Schema(description = "Creation timestamp", example = "2026-09-15T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp", example = "2026-09-15T10:30:00")
    private LocalDateTime updatedAt;

    public KtTemplateResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getOrganizationId() { return organizationId; }
    public void setOrganizationId(Long organizationId) { this.organizationId = organizationId; }

    public Long getTemplateId() { return templateId; }
    public void setTemplateId(Long templateId) { this.templateId = templateId; }

    public Boolean getRequired() { return required; }
    public void setRequired(Boolean required) { this.required = required; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public ClearanceAssignToType getAssignToType() { return assignToType; }
    public void setAssignToType(ClearanceAssignToType assignToType) { this.assignToType = assignToType; }

    public Long getAssignedUserId() { return assignedUserId; }
    public void setAssignedUserId(Long assignedUserId) { this.assignedUserId = assignedUserId; }

    public List<String> getEmployeeResponsibilities() { return employeeResponsibilities; }
    public void setEmployeeResponsibilities(List<String> employeeResponsibilities) { this.employeeResponsibilities = employeeResponsibilities; }

    public Boolean getHandoverDocumentRequired() { return handoverDocumentRequired; }
    public void setHandoverDocumentRequired(Boolean handoverDocumentRequired) { this.handoverDocumentRequired = handoverDocumentRequired; }

    public Boolean getHandoverApprovalRequired() { return handoverApprovalRequired; }
    public void setHandoverApprovalRequired(Boolean handoverApprovalRequired) { this.handoverApprovalRequired = handoverApprovalRequired; }

    public Integer getDueBeforeLwdDays() { return dueBeforeLwdDays; }
    public void setDueBeforeLwdDays(Integer dueBeforeLwdDays) { this.dueBeforeLwdDays = dueBeforeLwdDays; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public Boolean getMandatory() { return mandatory; }
    public void setMandatory(Boolean mandatory) { this.mandatory = mandatory; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
