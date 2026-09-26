package com.example.ems.offboarding.dto;

import com.example.ems.offboarding.enums.ClearanceAssignToType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Clearance Task Template response")
public class ClearanceTaskTemplateResponse {

    @Schema(description = "Task Template ID", example = "1")
    private Long id;

    @Schema(description = "Organization ID", example = "9645")
    private Long organizationId;

    @Schema(description = "Parent Offboarding Template ID", example = "1")
    private Long templateId;

    @Schema(description = "Name of the clearance task", example = "IT Access Revocation & Laptop Return")
    private String taskName;

    @Schema(description = "Detailed task instructions", example = "Revoke AWS/GitHub access and collect laptop")
    private String description;

    @Schema(description = "Role or actor assigned to clear this task", example = "DEPARTMENT_HEAD")
    private ClearanceAssignToType assignToType;

    @Schema(description = "Specific user ID if assignToType is SPECIFIC_USER", example = "101")
    private Long assignedUserId;

    @Schema(description = "Number of days before Last Working Day (LWD) this task must be cleared", example = "2")
    private Integer dueBeforeLwdDays;

    @Schema(description = "Task priority level", example = "HIGH")
    private String priority;

    @Schema(description = "Whether task completion is mandatory for final clearance", example = "true")
    private Boolean mandatory;

    @Schema(description = "Whether task completion requires approval from a supervisor", example = "true")
    private Boolean approvalRequired;

    @Schema(description = "Execution sequence order index", example = "1")
    private Integer sequence;

    @Schema(description = "Active status flag", example = "true")
    private Boolean active;

    @Schema(description = "Creation timestamp", example = "2026-09-15T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp", example = "2026-09-15T10:30:00")
    private LocalDateTime updatedAt;

    public ClearanceTaskTemplateResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getOrganizationId() { return organizationId; }
    public void setOrganizationId(Long organizationId) { this.organizationId = organizationId; }

    public Long getTemplateId() { return templateId; }
    public void setTemplateId(Long templateId) { this.templateId = templateId; }

    public String getTaskName() { return taskName; }
    public void setTaskName(String taskName) { this.taskName = taskName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public ClearanceAssignToType getAssignToType() { return assignToType; }
    public void setAssignToType(ClearanceAssignToType assignToType) { this.assignToType = assignToType; }

    public Long getAssignedUserId() { return assignedUserId; }
    public void setAssignedUserId(Long assignedUserId) { this.assignedUserId = assignedUserId; }

    public Integer getDueBeforeLwdDays() { return dueBeforeLwdDays; }
    public void setDueBeforeLwdDays(Integer dueBeforeLwdDays) { this.dueBeforeLwdDays = dueBeforeLwdDays; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

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
