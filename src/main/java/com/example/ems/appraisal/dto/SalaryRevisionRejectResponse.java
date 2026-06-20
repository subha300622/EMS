package com.example.ems.appraisal.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SalaryRevisionRejectResponse {

    @Schema(example = "string")
    private String revisionId;
    @Schema(example = "ACTIVE")
    private String status;
    @Schema(example = "string")
    private String rejectedAt;
    @Schema(example = "Personal business")
    private String reason;

    public SalaryRevisionRejectResponse() {}

    public SalaryRevisionRejectResponse(String revisionId, String status, String rejectedAt, String reason) {
        this.revisionId = revisionId;
        this.status = status;
        this.rejectedAt = rejectedAt;
        this.reason = reason;
    }

    public String getRevisionId() { return revisionId; }
    public void setRevisionId(String revisionId) { this.revisionId = revisionId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRejectedAt() { return rejectedAt; }
    public void setRejectedAt(String rejectedAt) { this.rejectedAt = rejectedAt; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
