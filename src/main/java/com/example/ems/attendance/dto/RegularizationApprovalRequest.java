package com.example.ems.attendance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload for approving or rejecting an attendance regularization request")
public class RegularizationApprovalRequest {

    @Size(max = 500, message = "remarks cannot exceed 500 characters")
    @Schema(description = "Approver remarks or rejection reason", example = "Correction verified with building security log")
    private String remarks;

    public RegularizationApprovalRequest() {}

    public RegularizationApprovalRequest(String remarks) {
        this.remarks = remarks;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
