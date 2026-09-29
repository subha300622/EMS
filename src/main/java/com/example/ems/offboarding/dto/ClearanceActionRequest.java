package com.example.ems.offboarding.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;

@Schema(description = "Payload to execute action on a department clearance")
public class ClearanceActionRequest {

    @NotBlank(message = "Action is required (CLEAR, HOLD, REJECT)")
    @Schema(description = "Action: CLEAR, HOLD, REJECT", example = "CLEAR", requiredMode = Schema.RequiredMode.REQUIRED)
    private String action;

    @Schema(description = "Remarks or comments for the clearance action", example = "Laptop returned and all system access revoked.")
    private String remarks;

    @Schema(description = "Department-specific clearance verification details or checklist", example = "{\"laptopReturned\": true, \"systemAccessRevoked\": true}")
    private Map<String, Object> clearanceData;

    public ClearanceActionRequest() {}

    public ClearanceActionRequest(String action, String remarks, Map<String, Object> clearanceData) {
        this.action = action;
        this.remarks = remarks;
        this.clearanceData = clearanceData;
    }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public Map<String, Object> getClearanceData() { return clearanceData; }
    public void setClearanceData(Map<String, Object> clearanceData) { this.clearanceData = clearanceData; }
}
