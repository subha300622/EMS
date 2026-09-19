package com.example.ems.offboarding.entity;

import com.example.ems.offboarding.enums.ClearanceAssignToType;
import com.example.ems.offboarding.enums.OffboardingAssetType;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "offboarding_asset_requirement_templates", indexes = {
    @Index(name = "idx_offboarding_asset_reqs_org_tpl", columnList = "organization_id, template_id")
})
public class OffboardingAssetRequirementTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "template_id", nullable = false)
    private Long templateId;

    @Enumerated(EnumType.STRING)
    @Column(name = "asset_type", nullable = false, length = 50)
    private OffboardingAssetType assetType;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "return_required", nullable = false)
    private Boolean returnRequired = true;

    @Column(name = "condition_check_required", nullable = false)
    private Boolean conditionCheckRequired = true;

    @Column(name = "serial_number_verification_required", nullable = false)
    private Boolean serialNumberVerificationRequired = true;

    @Column(name = "due_before_lwd_days", nullable = false)
    private Integer dueBeforeLwdDays = 1;

    @Enumerated(EnumType.STRING)
    @Column(name = "assigned_to_type", nullable = false, length = 50)
    private ClearanceAssignToType assignedToType;

    @Column(name = "assigned_user_id")
    private Long assignedUserId;

    @Column(nullable = false)
    private Boolean mandatory = true;

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

    public OffboardingAssetRequirementTemplate() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getOrganizationId() { return organizationId; }
    public void setOrganizationId(Long organizationId) { this.organizationId = organizationId; }

    public Long getTemplateId() { return templateId; }
    public void setTemplateId(Long templateId) { this.templateId = templateId; }

    public OffboardingAssetType getAssetType() { return assetType; }
    public void setAssetType(OffboardingAssetType assetType) { this.assetType = assetType; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Boolean getReturnRequired() { return returnRequired; }
    public void setReturnRequired(Boolean returnRequired) { this.returnRequired = returnRequired; }

    public Boolean getConditionCheckRequired() { return conditionCheckRequired; }
    public void setConditionCheckRequired(Boolean conditionCheckRequired) { this.conditionCheckRequired = conditionCheckRequired; }

    public Boolean getSerialNumberVerificationRequired() { return serialNumberVerificationRequired; }
    public void setSerialNumberVerificationRequired(Boolean serialNumberVerificationRequired) { this.serialNumberVerificationRequired = serialNumberVerificationRequired; }

    public Integer getDueBeforeLwdDays() { return dueBeforeLwdDays; }
    public void setDueBeforeLwdDays(Integer dueBeforeLwdDays) { this.dueBeforeLwdDays = dueBeforeLwdDays; }

    public ClearanceAssignToType getAssignedToType() { return assignedToType; }
    public void setAssignedToType(ClearanceAssignToType assignedToType) { this.assignedToType = assignedToType; }

    public Long getAssignedUserId() { return assignedUserId; }
    public void setAssignedUserId(Long assignedUserId) { this.assignedUserId = assignedUserId; }

    public Boolean getMandatory() { return mandatory; }
    public void setMandatory(Boolean mandatory) { this.mandatory = mandatory; }

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
