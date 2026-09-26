package com.example.ems.offboarding.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "Detailed breakdown of F&F Deductions")
public class FnfDeductionsBreakdown {

    @Schema(description = "Deduction for unserved mandatory notice period days", example = "0.00")
    private BigDecimal noticeRecovery = BigDecimal.ZERO;

    @Schema(description = "Deduction for damaged, unreturned, or missing company hardware/assets", example = "0.00")
    private BigDecimal assetDamage = BigDecimal.ZERO;

    @Schema(description = "Recovery for outstanding company loans, salary advances, or travel advances", example = "0.00")
    private BigDecimal loanRecovery = BigDecimal.ZERO;

    @Schema(description = "Withholding income tax / TDS deduction on settlement earnings", example = "17233.33")
    private BigDecimal tax = BigDecimal.ZERO;

    @Schema(description = "Other miscellaneous deductions or fine adjustments", example = "0.00")
    private BigDecimal other = BigDecimal.ZERO;

    @Schema(description = "Total aggregate deductions", example = "17233.33")
    private BigDecimal total = BigDecimal.ZERO;

    public FnfDeductionsBreakdown() {}

    public BigDecimal getNoticeRecovery() { return noticeRecovery; }
    public void setNoticeRecovery(BigDecimal noticeRecovery) { this.noticeRecovery = noticeRecovery; }

    public BigDecimal getAssetDamage() { return assetDamage; }
    public void setAssetDamage(BigDecimal assetDamage) { this.assetDamage = assetDamage; }

    public BigDecimal getLoanRecovery() { return loanRecovery; }
    public void setLoanRecovery(BigDecimal loanRecovery) { this.loanRecovery = loanRecovery; }

    public BigDecimal getTax() { return tax; }
    public void setTax(BigDecimal tax) { this.tax = tax; }

    public BigDecimal getOther() { return other; }
    public void setOther(BigDecimal other) { this.other = other; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
}

