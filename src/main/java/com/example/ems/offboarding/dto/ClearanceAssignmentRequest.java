package com.example.ems.offboarding.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Payload to assign or re-assign a clearance task")
public class ClearanceAssignmentRequest {

    @NotNull(message = "assignedToUserId is required")
    @Schema(description = "Employee ID assigned to verify and complete this clearance", example = "125", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long assignedToUserId;

    @Schema(description = "Clearance instruction / reason why it is required", example = "Verify laptop return and revoke all application access.")
    private String clearanceReason;

    @Schema(description = "Optional additional remarks", example = "Complete before F&F calculation.")
    private String remarks;

    public ClearanceAssignmentRequest() {}

    public ClearanceAssignmentRequest(Long assignedToUserId, String clearanceReason, String remarks) {
        this.assignedToUserId = assignedToUserId;
        this.clearanceReason = clearanceReason;
        this.remarks = remarks;
    }

    public Long getAssignedToUserId() { return assignedToUserId; }
    public void setAssignedToUserId(Long assignedToUserId) { this.assignedToUserId = assignedToUserId; }

    public String getClearanceReason() { return clearanceReason; }
    public void setClearanceReason(String clearanceReason) { this.clearanceReason = clearanceReason; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
