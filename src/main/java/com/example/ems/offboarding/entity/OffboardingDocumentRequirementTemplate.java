package com.example.ems.offboarding.entity;

import com.example.ems.offboarding.enums.ClearanceAssignToType;
import com.example.ems.offboarding.enums.DocumentActionType;
import com.example.ems.offboarding.enums.OffboardingDocumentType;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "offboarding_document_requirement_templates", indexes = {
    @Index(name = "idx_offboarding_doc_reqs_org_tpl", columnList = "organization_id, template_id")
})
public class OffboardingDocumentRequirementTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "template_id", nullable = false)
    private Long templateId;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false, length = 50)
    private OffboardingDocumentType documentType;

    @Column(name = "document_name", nullable = false)
    private String documentName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Boolean required = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private DocumentActionType action = DocumentActionType.GENERATE;

    @Enumerated(EnumType.STRING)
    @Column(name = "owner_type", nullable = false, length = 50)
    private ClearanceAssignToType ownerType;

    @Column(name = "owner_user_id")
    private Long ownerUserId;

    @Column(name = "due_before_lwd_days", nullable = false)
    private Integer dueBeforeLwdDays = 0;

    @Column(name = "employee_upload_required", nullable = false)
    private Boolean employeeUploadRequired = false;

    @Column(name = "approval_required", nullable = false)
    private Boolean approvalRequired = false;

    @Column(nullable = false)
    private Integer sequence = 1;

    @Column(nullable = false)
    private Boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public OffboardingDocumentRequirementTemplate() {}

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
