package com.example.ems.appraisal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateEmployeeAppraisalRequestDto {

    @NotNull(message = "reasonId is required")
    private Long reasonId;

    @NotBlank(message = "justification is required")
    private String justification;

    public Long getReasonId() { return reasonId; }
    public void setReasonId(Long reasonId) { this.reasonId = reasonId; }

    public String getJustification() { return justification; }
    public void setJustification(String justification) { this.justification = justification; }
}
