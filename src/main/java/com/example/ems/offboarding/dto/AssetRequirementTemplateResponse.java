package com.example.ems.offboarding.dto;

import com.example.ems.offboarding.enums.ClearanceAssignToType;
import com.example.ems.offboarding.enums.OffboardingAssetType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Asset Requirement Template response")
public class AssetRequirementTemplateResponse {

    @Schema(description = "Asset Requirement ID", example = "1")
    private Long id;

    @Schema(description = "Organization ID", example = "9645")
    private Long organizationId;

    @Schema(description = "Parent Offboarding Template ID", example = "1")
    private Long templateId;

    @Schema(description = "Type of asset", example = "HARDWARE")
    private OffboardingAssetType assetType;

    @Schema(description = "Name/category of asset requirement", example = "Company Laptop")
    private String name;

    @Schema(description = "Detailed return instructions", example = "MacBook Pro M2 and charger")
    private String description;

    @Schema(description = "Whether physical return is required", example = "true")
    private Boolean returnRequired;

    @Schema(description = "Whether condition assessment is required", example = "true")
    private Boolean conditionCheckRequired;

    @Schema(description = "Whether serial number matching is required", example = "true")
    private Boolean serialNumberVerificationRequired;

    @Schema(description = "Due days before Last Working Day", example = "1")
    private Integer dueBeforeLwdDays;

    @Schema(description = "Verifier actor role", example = "IT_ADMIN")
    private ClearanceAssignToType assignedToType;

    @Schema(description = "Specific verifier user ID if SPECIFIC_USER", example = "202")
    private Long assignedUserId;

    @Schema(description = "Whether clearance is mandatory", example = "true")
    private Boolean mandatory;

    @Schema(description = "Whether clearance requires supervisor approval", example = "true")
    private Boolean approvalRequired;

    @Schema(description = "Sequence order index", example = "1")
    private Integer sequence;

    @Schema(description = "Active status flag", example = "true")
    private Boolean active;

    @Schema(description = "Creation timestamp", example = "2026-09-15T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp", example = "2026-09-15T10:30:00")
    private LocalDateTime updatedAt;

    public AssetRequirementTemplateResponse() {}

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
