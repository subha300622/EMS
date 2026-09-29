package com.example.ems.offboarding.dto;

import com.example.ems.offboarding.enums.OffboardingTemplateStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Offboarding Template summary response")
public class OffboardingTemplateResponse {

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

    @Schema(description = "Number of clearance tasks configured", example = "5")
    private int clearanceTaskCount;

    @Schema(description = "Number of asset requirements configured", example = "3")
    private int assetRequirementCount;

    @Schema(description = "Number of document requirements configured", example = "2")
    private int documentRequirementCount;

    @Schema(description = "Whether knowledge transfer is configured", example = "true")
    private boolean ktConfigured;

    @Schema(description = "Whether exit interview is configured", example = "true")
    private boolean interviewConfigured;

    @Schema(description = "Creation timestamp", example = "2026-09-15T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp", example = "2026-09-15T10:30:00")
    private LocalDateTime updatedAt;

    public OffboardingTemplateResponse() {}

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

    public int getClearanceTaskCount() { return clearanceTaskCount; }
    public void setClearanceTaskCount(int clearanceTaskCount) { this.clearanceTaskCount = clearanceTaskCount; }

    public int getAssetRequirementCount() { return assetRequirementCount; }
    public void setAssetRequirementCount(int assetRequirementCount) { this.assetRequirementCount = assetRequirementCount; }

    public int getDocumentRequirementCount() { return documentRequirementCount; }
    public void setDocumentRequirementCount(int documentRequirementCount) { this.documentRequirementCount = documentRequirementCount; }

    public boolean isKtConfigured() { return ktConfigured; }
    public void setKtConfigured(boolean ktConfigured) { this.ktConfigured = ktConfigured; }

    public boolean isInterviewConfigured() { return interviewConfigured; }
    public void setInterviewConfigured(boolean interviewConfigured) { this.interviewConfigured = interviewConfigured; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
