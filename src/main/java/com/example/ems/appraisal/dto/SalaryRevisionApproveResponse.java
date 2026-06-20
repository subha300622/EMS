package com.example.ems.appraisal.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SalaryRevisionApproveResponse {

    @Schema(example = "string")
    private String revisionId;
    @Schema(example = "ACTIVE")
    private String status;
    @Schema(example = "string")
    private String approvedAt;
    @Schema(example = "string")
    private String approvedBy;

    public SalaryRevisionApproveResponse() {}

    public SalaryRevisionApproveResponse(String revisionId, String status, String approvedAt, String approvedBy) {
        this.revisionId = revisionId;
        this.status = status;
        this.approvedAt = approvedAt;
        this.approvedBy = approvedBy;
    }

    public String getRevisionId() { return revisionId; }
    public void setRevisionId(String revisionId) { this.revisionId = revisionId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getApprovedAt() { return approvedAt; }
    public void setApprovedAt(String approvedAt) { this.approvedAt = approvedAt; }

    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }
}
