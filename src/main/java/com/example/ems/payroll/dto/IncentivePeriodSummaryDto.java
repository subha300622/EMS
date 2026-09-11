package com.example.ems.payroll.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class IncentivePeriodSummaryDto {

    private BigDecimal amount = BigDecimal.ZERO;
    private List<Long> incentiveIds = new ArrayList<>();
    private String remarks;

    public IncentivePeriodSummaryDto() {}

    public IncentivePeriodSummaryDto(BigDecimal amount, List<Long> incentiveIds, String remarks) {
        this.amount = amount != null ? amount : BigDecimal.ZERO;
        this.incentiveIds = incentiveIds != null ? incentiveIds : new ArrayList<>();
        this.remarks = remarks;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public List<Long> getIncentiveIds() {
        return incentiveIds;
    }

    public void setIncentiveIds(List<Long> incentiveIds) {
        this.incentiveIds = incentiveIds;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
