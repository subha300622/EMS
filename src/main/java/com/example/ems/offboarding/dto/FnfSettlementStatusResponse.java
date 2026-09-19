package com.example.ems.offboarding.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Full & Final Settlement Status and Next Permissible Actions")
public class FnfSettlementStatusResponse {

    @Schema(description = "F&F settlement record ID", example = "8001")
    private Long fnfId;

    @Schema(description = "Associated employee exit ID", example = "5001")
    private Long exitId;

    @Schema(description = "Current settlement lifecycle status", example = "FINANCE_APPROVAL_PENDING")
    private String status;

    @Schema(description = "Computed net settlement amount", example = "86766.67")
    private BigDecimal netSettlement;

    @Schema(description = "Submission timestamp for approvals", example = "2026-09-18T10:00:00")
    private LocalDateTime submittedAt;

    @Schema(description = "User ID who submitted settlement", example = "1002")
    private Long submittedById;

    @Schema(description = "Finalization timestamp", example = "2026-09-18T15:00:00")
    private LocalDateTime finalizedAt;

    @Schema(description = "User ID who finalized settlement", example = "1001")
    private Long finalizedById;

    @Schema(description = "Cryptographic snapshot iteration version", example = "1")
    private Integer snapshotVersion;

    @Schema(description = "SHA-256 integrity hash of calculation snapshot", example = "9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08")
    private String snapshotHash;

    @Schema(description = "Permissible workflow actions for caller", example = "[\"APPROVE\", \"REJECT\", \"CANCEL\"]")
    private List<String> allowedActions;

    public FnfSettlementStatusResponse() {}

    public Long getFnfId() { return fnfId; }
    public void setFnfId(Long fnfId) { this.fnfId = fnfId; }

    public Long getExitId() { return exitId; }
    public void setExitId(Long exitId) { this.exitId = exitId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public BigDecimal getNetSettlement() { return netSettlement; }
    public void setNetSettlement(BigDecimal netSettlement) { this.netSettlement = netSettlement; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    public Long getSubmittedById() { return submittedById; }
    public void setSubmittedById(Long submittedById) { this.submittedById = submittedById; }

    public LocalDateTime getFinalizedAt() { return finalizedAt; }
    public void setFinalizedAt(LocalDateTime finalizedAt) { this.finalizedAt = finalizedAt; }

    public Long getFinalizedById() { return finalizedById; }
    public void setFinalizedById(Long finalizedById) { this.finalizedById = finalizedById; }

    public Integer getSnapshotVersion() { return snapshotVersion; }
    public void setSnapshotVersion(Integer snapshotVersion) { this.snapshotVersion = snapshotVersion; }

    public String getSnapshotHash() { return snapshotHash; }
    public void setSnapshotHash(String snapshotHash) { this.snapshotHash = snapshotHash; }

    public List<String> getAllowedActions() { return allowedActions; }
    public void setAllowedActions(List<String> allowedActions) { this.allowedActions = allowedActions; }
}
