package com.example.ems.offboarding.dto;

import com.example.ems.offboarding.enums.InterviewQuestionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class InterviewQuestionRequest {

    @NotBlank(message = "Question is required")
    private String question;

    @NotNull(message = "Question type is required")
    private InterviewQuestionType questionType;

    private Boolean required;

    private List<String> options;

    private Integer minValue;

    private Integer maxValue;

    private Integer sequence;

    private Boolean active;

    public InterviewQuestionRequest() {}

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
}
