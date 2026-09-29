package com.example.ems.offboarding.dto;

import com.example.ems.offboarding.enums.InterviewQuestionType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Exit Interview Question response")
public class InterviewQuestionResponse {

    @Schema(description = "Question ID", example = "1")
    private Long id;

    @Schema(description = "Organization ID", example = "9645")
    private Long organizationId;

    @Schema(description = "Parent Interview Template ID", example = "1")
    private Long interviewTemplateId;

    @Schema(description = "Question prompt text", example = "What is the primary reason for leaving?")
    private String question;

    @Schema(description = "Question input type (e.g. TEXT, MULTIPLE_CHOICE, RATING, YES_NO)", example = "TEXT")
    private InterviewQuestionType questionType;

    @Schema(description = "Whether an answer is mandatory", example = "true")
    private Boolean required;

    @Schema(description = "Choice options for MULTIPLE_CHOICE questions", example = "[\"Better compensation\", \"Career growth\", \"Relocation\"]")
    private List<String> options;

    @Schema(description = "Minimum allowed scale value for rating questions", example = "1")
    private Integer minValue;

    @Schema(description = "Maximum allowed scale value for rating questions", example = "5")
    private Integer maxValue;

    @Schema(description = "Question display sequence", example = "1")
    private Integer sequence;

    @Schema(description = "Active status flag", example = "true")
    private Boolean active;

    @Schema(description = "Creation timestamp", example = "2026-09-15T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp", example = "2026-09-15T10:30:00")
    private LocalDateTime updatedAt;

    public InterviewQuestionResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getOrganizationId() { return organizationId; }
    public void setOrganizationId(Long organizationId) { this.organizationId = organizationId; }

    public Long getInterviewTemplateId() { return interviewTemplateId; }
    public void setInterviewTemplateId(Long interviewTemplateId) { this.interviewTemplateId = interviewTemplateId; }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public InterviewQuestionType getQuestionType() { return questionType; }
    public void setQuestionType(InterviewQuestionType questionType) { this.questionType = questionType; }

    public Boolean getRequired() { return required; }
    public void setRequired(Boolean required) { this.required = required; }

    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) { this.options = options; }

    public Integer getMinValue() { return minValue; }
    public void setMinValue(Integer minValue) { this.minValue = minValue; }

    public Integer getMaxValue() { return maxValue; }
    public void setMaxValue(Integer maxValue) { this.maxValue = maxValue; }

    public Integer getSequence() { return sequence; }
    public void setSequence(Integer sequence) { this.sequence = sequence; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
