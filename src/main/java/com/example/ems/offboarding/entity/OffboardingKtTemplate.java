package com.example.ems.offboarding.entity;

import com.example.ems.offboarding.enums.ClearanceAssignToType;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "offboarding_kt_templates", uniqueConstraints = {
    @UniqueConstraint(name = "uq_offboarding_kt_template_template_id", columnNames = {"template_id"})
}, indexes = {
    @Index(name = "idx_offboarding_kt_templates_org", columnList = "organization_id, template_id")
})
public class OffboardingKtTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "template_id", nullable = false)
    private Long templateId;

    @Column(nullable = false)
    private Boolean required = true;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "assign_to_type", nullable = false, length = 50)
    private ClearanceAssignToType assignToType;

    @Column(name = "assigned_user_id")
    private Long assignedUserId;

    @Column(name = "employee_responsibilities_json", columnDefinition = "TEXT")
    private String employeeResponsibilitiesJson;

    @Column(name = "handover_document_required", nullable = false)
    private Boolean handoverDocumentRequired = true;

    @Column(name = "handover_approval_required", nullable = false)
    private Boolean handoverApprovalRequired = true;

    @Column(name = "due_before_lwd_days", nullable = false)
    private Integer dueBeforeLwdDays = 5;

    @Column(length = 50)
    private String priority = "HIGH";

    @Column(nullable = false)
    private Boolean mandatory = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public OffboardingKtTemplate() {}

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

    public String getEmployeeResponsibilitiesJson() { return employeeResponsibilitiesJson; }
    public void setEmployeeResponsibilitiesJson(String employeeResponsibilitiesJson) { this.employeeResponsibilitiesJson = employeeResponsibilitiesJson; }

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
