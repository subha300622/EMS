package com.example.ems.increment.dto;

import com.example.ems.increment.entity.IncrementRecommendationStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Submit Approval Response")
public class SubmitApprovalResponse {

    @Schema(description = "Recommendation ID", example = "1")
    private Long id;
    @Schema(description = "Increment recommendation status", example = "SUBMITTED")
    private IncrementRecommendationStatus status;
    @Schema(description = "Approval request ID", example = "15")
    private Long approvalRequestId;

    public SubmitApprovalResponse() {}

    public SubmitApprovalResponse(Long id, IncrementRecommendationStatus status, Long approvalRequestId) {
        this.id = id;
        this.status = status;
        this.approvalRequestId = approvalRequestId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getRecommendationId() { return id; }
    public void setRecommendationId(Long recommendationId) { this.id = recommendationId; }

    public IncrementRecommendationStatus getStatus() { return status; }
    public void setStatus(IncrementRecommendationStatus status) { this.status = status; }

    public Long getApprovalRequestId() { return approvalRequestId; }
    public void setApprovalRequestId(Long approvalRequestId) { this.approvalRequestId = approvalRequestId; }
}
