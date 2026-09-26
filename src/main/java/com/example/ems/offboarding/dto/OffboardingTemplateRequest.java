package com.example.ems.offboarding.dto;

import com.example.ems.offboarding.enums.OffboardingTemplateStatus;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

public class OffboardingTemplateRequest {

    @NotBlank(message = "Template name is required")
    private String name;

    private String description;

    private OffboardingTemplateStatus status;

    private List<String> departmentIds;

    private List<String> employmentTypes;

    private List<String> employeeTypes;

    private Boolean noticePeriodEnabled;

    private Integer noticePeriodDefaultDays;

    private Boolean allowEarlyRelease;

    private Boolean allowNoticePeriodBuyout;

    public OffboardingTemplateRequest() {}

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
}
