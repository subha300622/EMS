package com.example.ems.offboarding.dto;

import com.example.ems.offboarding.enums.ClearanceAssignToType;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class InterviewTemplateRequest {

    private Boolean enabled;

    @NotNull(message = "Conducted by type is required")
    private ClearanceAssignToType conductedByType;

    private Long conductedByUserId;

    private Boolean mandatory;

    private Integer dueBeforeLwdDays;

    private Boolean allowAnonymousFeedback;

    private List<InterviewQuestionRequest> questions;

    public InterviewTemplateRequest() {}

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

    public List<InterviewQuestionRequest> getQuestions() { return questions; }
    public void setQuestions(List<InterviewQuestionRequest> questions) { this.questions = questions; }
}
