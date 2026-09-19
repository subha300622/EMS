package com.example.ems.increment.dto;

import jakarta.validation.constraints.NotNull;

public class ImportAppraisalsRequest {

    @NotNull(message = "Appraisal Cycle ID is required")
    private Long appraisalCycleId;

    public Long getAppraisalCycleId() { return appraisalCycleId; }
    public void setAppraisalCycleId(Long appraisalCycleId) { this.appraisalCycleId = appraisalCycleId; }
}
