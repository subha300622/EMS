package com.example.ems.finance.dto;

import java.time.LocalDateTime;

public class SettlementStatusResponse {
    private Long settlementId;
    private String status;
    private boolean readyForDisbursement;
    private String approvedBy;
    private LocalDateTime approvedDate;

    public SettlementStatusResponse() {}

    public SettlementStatusResponse(Long settlementId, String status, boolean readyForDisbursement, String approvedBy, LocalDateTime approvedDate) {
        this.settlementId = settlementId;
        this.status = status;
        this.readyForDisbursement = readyForDisbursement;
        this.approvedBy = approvedBy;
        this.approvedDate = approvedDate;
    }

    public Long getSettlementId() { return settlementId; }
    public void setSettlementId(Long settlementId) { this.settlementId = settlementId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isReadyForDisbursement() { return readyForDisbursement; }
    public void setReadyForDisbursement(boolean readyForDisbursement) { this.readyForDisbursement = readyForDisbursement; }

    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }

    public LocalDateTime getApprovedDate() { return approvedDate; }
    public void setApprovedDate(LocalDateTime approvedDate) { this.approvedDate = approvedDate; }
}
