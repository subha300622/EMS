package com.example.ems.offboarding.dto;

import com.example.ems.offboarding.enums.ClearanceAssignToType;
import com.example.ems.offboarding.enums.DocumentActionType;
import com.example.ems.offboarding.enums.OffboardingDocumentType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Document Requirement Template response")
public class DocumentRequirementTemplateResponse {

    @Schema(description = "Document Requirement ID", example = "1")
    private Long id;

    @Schema(description = "Organization ID", example = "9645")
    private Long organizationId;

    @Schema(description = "Parent Offboarding Template ID", example = "1")
    private Long templateId;

    @Schema(description = "Document category", example = "NDA")
    private OffboardingDocumentType documentType;

    @Schema(description = "Document name", example = "Non-Disclosure & Exit Agreement")
    private String documentName;

    @Schema(description = "Description or instructions", example = "Sign and submit before last working day")
    private String description;

    @Schema(description = "Whether this document is required", example = "true")
    private Boolean required;

    @Schema(description = "Action required on document", example = "SIGN")
    private DocumentActionType action;

    @Schema(description = "Owner role responsible for collecting/signing", example = "HR")
    private ClearanceAssignToType ownerType;

    @Schema(description = "Specific owner user ID if SPECIFIC_USER", example = "105")
    private Long ownerUserId;

    @Schema(description = "Due days before Last Working Day", example = "3")
    private Integer dueBeforeLwdDays;

    @Schema(description = "Whether employee must upload document copy", example = "true")
    private Boolean employeeUploadRequired;

    @Schema(description = "Whether HR approval is required", example = "true")
    private Boolean approvalRequired;

    @Schema(description = "Sequence order index", example = "1")
    private Integer sequence;

    @Schema(description = "Active status flag", example = "true")
    private Boolean active;

    @Schema(description = "Creation timestamp", example = "2026-09-15T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp", example = "2026-09-15T10:30:00")
    private LocalDateTime updatedAt;

    public DocumentRequirementTemplateResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getOrganizationId() { return organizationId; }
    public void setOrganizationId(Long organizationId) { this.organizationId = organizationId; }

    public Long getTemplateId() { return templateId; }
    public void setTemplateId(Long templateId) { this.templateId = templateId; }

    public OffboardingDocumentType getDocumentType() { return documentType; }
    public void setDocumentType(OffboardingDocumentType documentType) { this.documentType = documentType; }

    public String getDocumentName() { return documentName; }
    public void setDocumentName(String documentName) { this.documentName = documentName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Boolean getRequired() { return required; }
    public void setRequired(Boolean required) { this.required = required; }

    public DocumentActionType getAction() { return action; }
    public void setAction(DocumentActionType action) { this.action = action; }

    public ClearanceAssignToType getOwnerType() { return ownerType; }
    public void setOwnerType(ClearanceAssignToType ownerType) { this.ownerType = ownerType; }

    public Long getOwnerUserId() { return ownerUserId; }
    public void setOwnerUserId(Long ownerUserId) { this.ownerUserId = ownerUserId; }

    public Integer getDueBeforeLwdDays() { return dueBeforeLwdDays; }
    public void setDueBeforeLwdDays(Integer dueBeforeLwdDays) { this.dueBeforeLwdDays = dueBeforeLwdDays; }

    public Boolean getEmployeeUploadRequired() { return employeeUploadRequired; }
    public void setEmployeeUploadRequired(Boolean employeeUploadRequired) { this.employeeUploadRequired = employeeUploadRequired; }

    public Boolean getApprovalRequired() { return approvalRequired; }
    public void setApprovalRequired(Boolean approvalRequired) { this.approvalRequired = approvalRequired; }

    public Integer getSequence() { return sequence; }
    public void setSequence(Integer sequence) { this.sequence = sequence; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
