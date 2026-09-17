package com.example.ems.offboarding.dto;

import com.example.ems.offboarding.enums.ClearanceAssignToType;
import com.example.ems.offboarding.enums.OffboardingAssetType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class AssetRequirementTemplateRequest {

    @NotNull(message = "Asset type is required")
    private OffboardingAssetType assetType;

    @NotBlank(message = "Name is required")
    private String name;

    private String description;

    private Boolean returnRequired;

    private Boolean conditionCheckRequired;

    private Boolean serialNumberVerificationRequired;

    private Integer dueBeforeLwdDays;

    @NotNull(message = "Assigned to type is required")
    private ClearanceAssignToType assignedToType;

    private Long assignedUserId;

    private Boolean mandatory;

    private Boolean approvalRequired;

    private Integer sequence;

    private Boolean active;

    public AssetRequirementTemplateRequest() {}

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
}
