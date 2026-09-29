package com.example.ems.offboarding.dto;

import com.example.ems.offboarding.enums.ClearanceAssignToType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class KtTemplateRequest {

    private Boolean required;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotNull(message = "Assign to type is required")
    private ClearanceAssignToType assignToType;

    private Long assignedUserId;

    private List<String> employeeResponsibilities;

    private Boolean handoverDocumentRequired;

    private Boolean handoverApprovalRequired;

    private Integer dueBeforeLwdDays;

    private String priority;

    private Boolean mandatory;

    public KtTemplateRequest() {}

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
}
