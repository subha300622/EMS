package com.example.ems.payroll.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class MySalaryRevisionsResponse {

    private CurrentCTCInfo currentCTC;
    private List<RevisionItem> history;

    public MySalaryRevisionsResponse() {}

    public MySalaryRevisionsResponse(CurrentCTCInfo currentCTC, List<RevisionItem> history) {
        this.currentCTC = currentCTC;
        this.history = history;
    }

    public CurrentCTCInfo getCurrentCTC() { return currentCTC; }
    public void setCurrentCTC(CurrentCTCInfo currentCTC) { this.currentCTC = currentCTC; }

    public List<RevisionItem> getHistory() { return history; }
    public void setHistory(List<RevisionItem> history) { this.history = history; }

    public static class CurrentCTCInfo {
        @Schema(example = "5000.00")
        private BigDecimal amount;
        @Schema(example = "string")
        private String currency = "INR";

        public CurrentCTCInfo() {}

        public CurrentCTCInfo(BigDecimal amount, String currency) {
            this.amount = amount;
            this.currency = currency;
        }

        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }

        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
    }

    public static class RevisionItem {
        @Schema(example = "1")
        private Long revisionId;
        @Schema(example = "2026-06-19")
        private LocalDate effectiveDate;
        @Schema(example = "100.00")
        private BigDecimal previousCTC;
        @Schema(example = "100.00")
        private BigDecimal revisedCTC;
        @Schema(example = "100.00")
        private BigDecimal incrementPercentage;
        @Schema(example = "Personal business")
        private String revisionReason;

        public RevisionItem() {}

        public RevisionItem(Long revisionId, LocalDate effectiveDate, BigDecimal previousCTC, BigDecimal revisedCTC, BigDecimal incrementPercentage, String revisionReason) {
            this.revisionId = revisionId;
            this.effectiveDate = effectiveDate;
            this.previousCTC = previousCTC;
            this.revisedCTC = revisedCTC;
            this.incrementPercentage = incrementPercentage;
            this.revisionReason = revisionReason;
        }

        public Long getRevisionId() { return revisionId; }
        public void setRevisionId(Long revisionId) { this.revisionId = revisionId; }

        public LocalDate getEffectiveDate() { return effectiveDate; }
        public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }

        public BigDecimal getPreviousCTC() { return previousCTC; }
        public void setPreviousCTC(BigDecimal previousCTC) { this.previousCTC = previousCTC; }

        public BigDecimal getRevisedCTC() { return revisedCTC; }
        public void setRevisedCTC(BigDecimal revisedCTC) { this.revisedCTC = revisedCTC; }

        public BigDecimal getIncrementPercentage() { return incrementPercentage; }
        public void setIncrementPercentage(BigDecimal incrementPercentage) { this.incrementPercentage = incrementPercentage; }

        public String getRevisionReason() { return revisionReason; }
        public void setRevisionReason(String revisionReason) { this.revisionReason = revisionReason; }
    }
}
