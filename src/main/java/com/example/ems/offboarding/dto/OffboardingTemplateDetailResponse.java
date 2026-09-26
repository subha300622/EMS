package com.example.ems.offboarding.dto;

import com.example.ems.offboarding.enums.OffboardingTemplateStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Detailed Offboarding Template response including tasks, asset requirements, document requirements, KT, and exit interview configurations")
public class OffboardingTemplateDetailResponse {

    @Schema(description = "Unique Template ID", example = "1")
    private Long id;

    @Schema(description = "Organization ID", example = "9645")
    private Long organizationId;

    @Schema(description = "Template name", example = "Standard Employee Exit")
    private String name;

    @Schema(description = "Template description", example = "Standard exit and clearance workflow")
    private String description;

    @Schema(description = "Template lifecycle status", example = "ACTIVE")
    private OffboardingTemplateStatus status;

    @Schema(description = "Applicable department IDs", example = "[\"ENGINEERING\", \"PRODUCT\"]")
    private List<String> departmentIds;

    @Schema(description = "Applicable employment types", example = "[\"FULL_TIME\", \"CONTRACT\"]")
    private List<String> employmentTypes;

    @Schema(description = "Applicable employee types", example = "[\"REGULAR\", \"PROBATION\"]")
    private List<String> employeeTypes;

    @Schema(description = "Whether notice period is required", example = "true")
    private Boolean noticePeriodEnabled;

    @Schema(description = "Default notice period in days", example = "30")
    private Integer noticePeriodDefaultDays;

    @Schema(description = "Whether early release is permitted", example = "true")
    private Boolean allowEarlyRelease;

    @Schema(description = "Whether notice period buyout is permitted", example = "false")
    private Boolean allowNoticePeriodBuyout;

    @Schema(description = "List of configured clearance tasks")
    private List<ClearanceTaskTemplateResponse> clearanceTasks;

    @Schema(description = "List of configured asset requirements")
    private List<AssetRequirementTemplateResponse> assetRequirements;

    @Schema(description = "List of configured document requirements")
    private List<DocumentRequirementTemplateResponse> documentRequirements;

    @Schema(description = "Knowledge transfer configuration")
    private KtTemplateResponse ktTemplate;

    @Schema(description = "Exit interview configuration")
    private InterviewTemplateResponse interviewTemplate;

    @Schema(description = "Creation timestamp", example = "2026-09-15T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp", example = "2026-09-15T10:30:00")
    private LocalDateTime updatedAt;

    public OffboardingTemplateDetailResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getOrganizationId() { return organizationId; }
    public void setOrganizationId(Long organizationId) { this.organizationId = organizationId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public OffboardingTemplateStatus getStatus() { return status; }
    public void setStatus(OffboardingTemplateStatus status) { this.status = status; }

    public List<String> getDepartmentIds() { return departmentIds; }
    public void setDepartmentIds(List<String> departmentIds) { this.departmentIds = departmentIds; }

    public List<String> getEmploymentTypes() { return employmentTypes; }
    public void setEmploymentTypes(List<String> employmentTypes) { this.employmentTypes = employmentTypes; }

    public List<String> getEmployeeTypes() { return employeeTypes; }
    public void setEmployeeTypes(List<String> employeeTypes) { this.employeeTypes = employeeTypes; }

    public Boolean getNoticePeriodEnabled() { return noticePeriodEnabled; }
    public void setNoticePeriodEnabled(Boolean noticePeriodEnabled) { this.noticePeriodEnabled = noticePeriodEnabled; }

    public Integer getNoticePeriodDefaultDays() { return noticePeriodDefaultDays; }
    public void setNoticePeriodDefaultDays(Integer noticePeriodDefaultDays) { this.noticePeriodDefaultDays = noticePeriodDefaultDays; }

    public Boolean getAllowEarlyRelease() { return allowEarlyRelease; }
    public void setAllowEarlyRelease(Boolean allowEarlyRelease) { this.allowEarlyRelease = allowEarlyRelease; }

    public Boolean getAllowNoticePeriodBuyout() { return allowNoticePeriodBuyout; }
    public void setAllowNoticePeriodBuyout(Boolean allowNoticePeriodBuyout) { this.allowNoticePeriodBuyout = allowNoticePeriodBuyout; }

    public List<ClearanceTaskTemplateResponse> getClearanceTasks() { return clearanceTasks; }
    public void setClearanceTasks(List<ClearanceTaskTemplateResponse> clearanceTasks) { this.clearanceTasks = clearanceTasks; }

    public List<AssetRequirementTemplateResponse> getAssetRequirements() { return assetRequirements; }
    public void setAssetRequirements(List<AssetRequirementTemplateResponse> assetRequirements) { this.assetRequirements = assetRequirements; }

    public List<DocumentRequirementTemplateResponse> getDocumentRequirements() { return documentRequirements; }
    public void setDocumentRequirements(List<DocumentRequirementTemplateResponse> documentRequirements) { this.documentRequirements = documentRequirements; }

    public KtTemplateResponse getKtTemplate() { return ktTemplate; }
    public void setKtTemplate(KtTemplateResponse ktTemplate) { this.ktTemplate = ktTemplate; }

    public InterviewTemplateResponse getInterviewTemplate() { return interviewTemplate; }
    public void setInterviewTemplate(InterviewTemplateResponse interviewTemplate) { this.interviewTemplate = interviewTemplate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
