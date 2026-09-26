package com.example.ems.offboarding.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Individual Blocking Item Preventing Settlement Clearance or Disbursement")
public class FnfBlockingItemDto {

    @Schema(description = "Bloker category: CLEARANCE, ASSET, EXPENSE, BANK_ACCOUNT, APPROVAL", example = "CLEARANCE")
    private String type; // CLEARANCE, ASSET, EXPENSE, BANK_ACCOUNT, APPROVAL

    @Schema(description = "Identifier reference of the blocking entity", example = "CLR-IT-5001")
    private String referenceId;

    @Schema(description = "Department responsible for resolution", example = "Information Technology")
    private String department;

    @Schema(description = "Detailed reason why action is blocked", example = "Company laptop MacBook Pro serial #C02X... has not been returned.")
    private String reason;

    @Schema(description = "Severity level: BLOCKING or WARNING", example = "BLOCKING")
    private String severity; // BLOCKING, WARNING

    public FnfBlockingItemDto() {}

    public FnfBlockingItemDto(String type, String referenceId, String department, String reason, String severity) {
        this.type = type;
        this.referenceId = referenceId;
        this.department = department;
        this.reason = reason;
        this.severity = severity;
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getReferenceId() { return referenceId; }
    public void setReferenceId(String referenceId) { this.referenceId = referenceId; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getDescription() { return reason; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
}

