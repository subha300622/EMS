package com.example.ems.offboarding.entity;

import com.example.ems.offboarding.enums.OffboardingTemplateStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "offboarding_templates", indexes = {
    @Index(name = "idx_offboarding_templates_org_status", columnList = "organization_id, status")
})
public class OffboardingTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private OffboardingTemplateStatus status = OffboardingTemplateStatus.ACTIVE;

    @Column(name = "department_ids_json", columnDefinition = "TEXT")
    private String departmentIdsJson;

    @Column(name = "employment_types_json", columnDefinition = "TEXT")
    private String employmentTypesJson;

    @Column(name = "employee_types_json", columnDefinition = "TEXT")
    private String employeeTypesJson;

    @Column(name = "notice_period_enabled", nullable = false)
    private Boolean noticePeriodEnabled = true;

    @Column(name = "notice_period_default_days", nullable = false)
    private Integer noticePeriodDefaultDays = 30;

    @Column(name = "allow_early_release", nullable = false)
    private Boolean allowEarlyRelease = true;

    @Column(name = "allow_notice_period_buyout", nullable = false)
    private Boolean allowNoticePeriodBuyout = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public OffboardingTemplate() {}

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

    public String getDepartmentIdsJson() { return departmentIdsJson; }
    public void setDepartmentIdsJson(String departmentIdsJson) { this.departmentIdsJson = departmentIdsJson; }

    public String getEmploymentTypesJson() { return employmentTypesJson; }
    public void setEmploymentTypesJson(String employmentTypesJson) { this.employmentTypesJson = employmentTypesJson; }

    public String getEmployeeTypesJson() { return employeeTypesJson; }
    public void setEmployeeTypesJson(String employeeTypesJson) { this.employeeTypesJson = employeeTypesJson; }

    public Boolean getNoticePeriodEnabled() { return noticePeriodEnabled; }
    public void setNoticePeriodEnabled(Boolean noticePeriodEnabled) { this.noticePeriodEnabled = noticePeriodEnabled; }

    public Integer getNoticePeriodDefaultDays() { return noticePeriodDefaultDays; }
    public void setNoticePeriodDefaultDays(Integer noticePeriodDefaultDays) { this.noticePeriodDefaultDays = noticePeriodDefaultDays; }

    public Boolean getAllowEarlyRelease() { return allowEarlyRelease; }
    public void setAllowEarlyRelease(Boolean allowEarlyRelease) { this.allowEarlyRelease = allowEarlyRelease; }

    public Boolean getAllowNoticePeriodBuyout() { return allowNoticePeriodBuyout; }
    public void setAllowNoticePeriodBuyout(Boolean allowNoticePeriodBuyout) { this.allowNoticePeriodBuyout = allowNoticePeriodBuyout; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
