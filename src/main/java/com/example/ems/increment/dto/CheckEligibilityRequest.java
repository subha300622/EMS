package com.example.ems.increment.dto;

import jakarta.validation.constraints.NotNull;

public class CheckEligibilityRequest {

    @NotNull(message = "Appraisal ID is required")
    private Long appraisalId;

    private Double proposedPercentage;

    private Boolean disciplinaryClear = true;

    public Long getAppraisalId() { return appraisalId; }
    public void setAppraisalId(Long appraisalId) { this.appraisalId = appraisalId; }

    public Double getProposedPercentage() { return proposedPercentage; }
    public void setProposedPercentage(Double proposedPercentage) { this.proposedPercentage = proposedPercentage; }

    public Boolean getDisciplinaryClear() { return disciplinaryClear; }
    public void setDisciplinaryClear(Boolean disciplinaryClear) { this.disciplinaryClear = disciplinaryClear; }
}
