package com.example.ems.approval.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Generic Approval Action Payload")
public class ApprovalGenericActionRequest {

    @Schema(description = "Action to perform: APPROVE, REJECT, HOLD", example = "APPROVE", requiredMode = Schema.RequiredMode.REQUIRED)
    private String action;

    @Schema(description = "Remarks or comments for the action", example = "Exit request approved")
    private String remarks;

    // Optional alias comment field for interoperability
    private String comment;

    public ApprovalGenericActionRequest() {}

    public ApprovalGenericActionRequest(String action, String remarks) {
        this.action = action;
        this.remarks = remarks;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getRemarks() {
        if (remarks != null && !remarks.trim().isEmpty()) {
            return remarks;
        }
        return comment;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getComment() {
        return getRemarks();
    }

    public void setComment(String comment) {
        this.comment = comment;
        if (this.remarks == null) {
            this.remarks = comment;
        }
    }
}
