package com.example.ems.increment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Import Appraisals Response")
public class ImportAppraisalsResponse {

    @Schema(description = "Increment Cycle ID", example = "1")
    private Long incrementCycleId;
    @Schema(description = "Appraisal Cycle ID", example = "2")
    private Long appraisalCycleId;
    @Schema(description = "Total number of evaluated appraisals", example = "50")
    private int totalEvaluated;
    @Schema(description = "Number of eligible employees", example = "42")
    private int eligibleCount;
    @Schema(description = "Number of ineligible employees", example = "8")
    private int ineligibleCount;
    @Schema(description = "Detailed list of eligibility evaluations")
    private List<EligibilityEvaluationResponse> evaluations;

    public Long getIncrementCycleId() { return incrementCycleId; }
    public void setIncrementCycleId(Long incrementCycleId) { this.incrementCycleId = incrementCycleId; }

    public Long getAppraisalCycleId() { return appraisalCycleId; }
    public void setAppraisalCycleId(Long appraisalCycleId) { this.appraisalCycleId = appraisalCycleId; }

    public int getTotalEvaluated() { return totalEvaluated; }
    public void setTotalEvaluated(int totalEvaluated) { this.totalEvaluated = totalEvaluated; }

    public int getEligibleCount() { return eligibleCount; }
    public void setEligibleCount(int eligibleCount) { this.eligibleCount = eligibleCount; }

    public int getIneligibleCount() { return ineligibleCount; }
    public void setIneligibleCount(int ineligibleCount) { this.ineligibleCount = ineligibleCount; }

    public List<EligibilityEvaluationResponse> getEvaluations() { return evaluations; }
    public void setEvaluations(List<EligibilityEvaluationResponse> evaluations) { this.evaluations = evaluations; }
}
