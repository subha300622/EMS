package com.example.ems.attendance.dto.adjustment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload for approving or rejecting an attendance adjustment request")
public class AdjustmentApprovalRequest {

    @Size(max = 500, message = "remarks cannot exceed 500 characters")
    @Schema(description = "Approver remarks or rejection reason", example = "Adjustment approved after manager review")
    private String remarks;

    public AdjustmentApprovalRequest() {}

    public AdjustmentApprovalRequest(String remarks) {
        this.remarks = remarks;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
