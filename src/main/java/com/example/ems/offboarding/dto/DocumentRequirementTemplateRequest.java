package com.example.ems.offboarding.dto;

import com.example.ems.offboarding.enums.ClearanceAssignToType;
import com.example.ems.offboarding.enums.DocumentActionType;
import com.example.ems.offboarding.enums.OffboardingDocumentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class DocumentRequirementTemplateRequest {

    @NotNull(message = "Document type is required")
    private OffboardingDocumentType documentType;

    @NotBlank(message = "Document name is required")
    private String documentName;

    private String description;

    private Boolean required;

    private DocumentActionType action;

    @NotNull(message = "Owner type is required")
    private ClearanceAssignToType ownerType;

    private Long ownerUserId;

    private Integer dueBeforeLwdDays;

    private Boolean employeeUploadRequired;

    private Boolean approvalRequired;

    private Integer sequence;

    private Boolean active;

    public DocumentRequirementTemplateRequest() {}

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
}
