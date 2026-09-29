package com.example.ems.offboarding.dto;

import com.example.ems.offboarding.enums.ClearanceAssignToType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Exit Interview Template response")
public class InterviewTemplateResponse {

    @Schema(description = "Interview Template ID", example = "1")
    private Long id;

    @Schema(description = "Organization ID", example = "9645")
    private Long organizationId;

    @Schema(description = "Parent Offboarding Template ID", example = "1")
    private Long templateId;

    @Schema(description = "Whether exit interview is enabled", example = "true")
    private Boolean enabled;

    @Schema(description = "Interviewer role", example = "HR")
    private ClearanceAssignToType conductedByType;

    @Schema(description = "Specific interviewer user ID if SPECIFIC_USER", example = "105")
    private Long conductedByUserId;

    @Schema(description = "Whether exit interview is mandatory", example = "true")
    private Boolean mandatory;

    @Schema(description = "Due days before Last Working Day", example = "3")
    private Integer dueBeforeLwdDays;

    @Schema(description = "Whether anonymous feedback submission is permitted", example = "false")
    private Boolean allowAnonymousFeedback;

    @Schema(description = "List of configured interview questions")
    private List<InterviewQuestionResponse> questions;

    @Schema(description = "Creation timestamp", example = "2026-09-15T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp", example = "2026-09-15T10:30:00")
    private LocalDateTime updatedAt;

    public InterviewTemplateResponse() {}

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

    public List<InterviewQuestionResponse> getQuestions() { return questions; }
    public void setQuestions(List<InterviewQuestionResponse> questions) { this.questions = questions; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
