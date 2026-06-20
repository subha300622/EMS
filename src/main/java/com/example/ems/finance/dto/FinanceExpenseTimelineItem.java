package com.example.ems.finance.dto;

import java.time.LocalDateTime;

public class FinanceExpenseTimelineItem {
    private String status;
    private String updatedBy;
    private LocalDateTime updatedAt;

    public FinanceExpenseTimelineItem() {}

    public FinanceExpenseTimelineItem(String status, String updatedBy, LocalDateTime updatedAt) {
        this.status = status;
        this.updatedBy = updatedBy;
        this.updatedAt = updatedAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
