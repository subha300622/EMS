package com.example.ems.finance.dto;

import java.time.LocalDateTime;

public class SettlementTimelineItem {
    private String status;
    private String updatedBy;
    private LocalDateTime updatedAt;
    private String remarks;

    public SettlementTimelineItem() {}

    public SettlementTimelineItem(String status, String updatedBy, LocalDateTime updatedAt, String remarks) {
        this.status = status;
        this.updatedBy = updatedBy;
        this.updatedAt = updatedAt;
        this.remarks = remarks;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
