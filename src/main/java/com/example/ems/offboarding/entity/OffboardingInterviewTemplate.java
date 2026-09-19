package com.example.ems.offboarding.entity;

import com.example.ems.offboarding.enums.ClearanceAssignToType;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "offboarding_interview_templates", uniqueConstraints = {
    @UniqueConstraint(name = "uq_offboarding_interview_template_template_id", columnNames = {"template_id"})
}, indexes = {
    @Index(name = "idx_offboarding_interview_templates_org", columnList = "organization_id, template_id")
})
public class OffboardingInterviewTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "template_id", nullable = false)
    private Long templateId;

    @Column(nullable = false)
    private Boolean enabled = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "conducted_by_type", nullable = false, length = 50)
    private ClearanceAssignToType conductedByType;

    @Column(name = "conducted_by_user_id")
    private Long conductedByUserId;

    @Column(nullable = false)
    private Boolean mandatory = true;

    @Column(name = "due_before_lwd_days", nullable = false)
    private Integer dueBeforeLwdDays = 2;

    @Column(name = "allow_anonymous_feedback", nullable = false)
    private Boolean allowAnonymousFeedback = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public OffboardingInterviewTemplate() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getOrganizationId() { return organizationId; }
    public void setOrganizationId(Long organizationId) { this.organizationId = organizationId; }

    public Long getTemplateId() { return templateId; }
    public void setTemplateId(Long templateId) { this.templateId = templateId; }

    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }

    public ClearanceAssignToType getConductedByType() { return conductedByType; }
    public void setConductedByType(ClearanceAssignToType conductedByType) { this.conductedByType = conductedByType; }

    public Long getConductedByUserId() { return conductedByUserId; }
    public void setConductedByUserId(Long conductedByUserId) { this.conductedByUserId = conductedByUserId; }

    public Boolean getMandatory() { return mandatory; }
    public void setMandatory(Boolean mandatory) { this.mandatory = mandatory; }

    public Integer getDueBeforeLwdDays() { return dueBeforeLwdDays; }
    public void setDueBeforeLwdDays(Integer dueBeforeLwdDays) { this.dueBeforeLwdDays = dueBeforeLwdDays; }

    public Boolean getAllowAnonymousFeedback() { return allowAnonymousFeedback; }
    public void setAllowAnonymousFeedback(Boolean allowAnonymousFeedback) { this.allowAnonymousFeedback = allowAnonymousFeedback; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
