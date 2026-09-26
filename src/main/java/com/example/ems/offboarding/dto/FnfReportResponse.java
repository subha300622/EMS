package com.example.ems.offboarding.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;

@Schema(description = "Aggregated F&F Report Response")
public class FnfReportResponse {

    @Schema(description = "Total number of settlements matching query", example = "42")
    private long totalRecords;

    @Schema(description = "Total gross calculated settlement value across reports", example = "3650000.00")
    private BigDecimal totalAmount;

    @Schema(description = "Total disbursed settlement payments", example = "2400000.00")
    private BigDecimal totalPaidAmount;

    @Schema(description = "Total pending settlement amount awaiting payment", example = "1250000.00")
    private BigDecimal totalPendingAmount;

    @Schema(description = "List of individual settlement summary records")
    private List<FnfReportSummaryDto> settlements;

    public FnfReportResponse() {}

    public FnfReportResponse(long totalRecords, BigDecimal totalAmount, BigDecimal totalPaidAmount, BigDecimal totalPendingAmount, List<FnfReportSummaryDto> settlements) {
        this.totalRecords = totalRecords;
        this.totalAmount = totalAmount;
        this.totalPaidAmount = totalPaidAmount;
        this.totalPendingAmount = totalPendingAmount;
        this.settlements = settlements;
    }

    public long getTotalRecords() { return totalRecords; }
    public void setTotalRecords(long totalRecords) { this.totalRecords = totalRecords; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public BigDecimal getTotalPaidAmount() { return totalPaidAmount; }
    public void setTotalPaidAmount(BigDecimal totalPaidAmount) { this.totalPaidAmount = totalPaidAmount; }

    public BigDecimal getTotalPendingAmount() { return totalPendingAmount; }
    public void setTotalPendingAmount(BigDecimal totalPendingAmount) { this.totalPendingAmount = totalPendingAmount; }

    public List<FnfReportSummaryDto> getSettlements() { return settlements; }
    public void setSettlements(List<FnfReportSummaryDto> settlements) { this.settlements = settlements; }
}
