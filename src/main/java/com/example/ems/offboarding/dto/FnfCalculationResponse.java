package com.example.ems.offboarding.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "Calculated F&F Settlement Statement Response")
public class FnfCalculationResponse {

    @Schema(description = "F&F settlement record ID", example = "8001")
    private Long fnfId;

    @Schema(description = "Exit record ID", example = "5001")
    private Long exitId;

    @Schema(description = "Itemized earnings breakdown")
    private FnfEarningsBreakdown earnings;

    @Schema(description = "Itemized deductions breakdown")
    private FnfDeductionsBreakdown deductions;

    @Schema(description = "Final calculated net settlement payable to employee", example = "86766.67")
    private BigDecimal netSettlement;

    @Schema(description = "F&F approval/processing status", example = "FINANCE_APPROVAL_PENDING")
    private String status;

    public FnfCalculationResponse() {}

    public FnfCalculationResponse(Long fnfId, Long exitId, FnfEarningsBreakdown earnings, FnfDeductionsBreakdown deductions, BigDecimal netSettlement, String status) {
        this.fnfId = fnfId;
        this.exitId = exitId;
        this.earnings = earnings;
        this.deductions = deductions;
        this.netSettlement = netSettlement;
        this.status = status;
    }

    public Long getFnfId() { return fnfId; }
    public void setFnfId(Long fnfId) { this.fnfId = fnfId; }

    public Long getExitId() { return exitId; }
    public void setExitId(Long exitId) { this.exitId = exitId; }

    public FnfEarningsBreakdown getEarnings() { return earnings; }
    public void setEarnings(FnfEarningsBreakdown earnings) { this.earnings = earnings; }

    public FnfDeductionsBreakdown getDeductions() { return deductions; }
    public void setDeductions(FnfDeductionsBreakdown deductions) { this.deductions = deductions; }

    public BigDecimal getNetSettlement() { return netSettlement; }
    public void setNetSettlement(BigDecimal netSettlement) { this.netSettlement = netSettlement; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
