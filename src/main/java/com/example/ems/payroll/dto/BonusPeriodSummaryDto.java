package com.example.ems.payroll.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class BonusPeriodSummaryDto {

    private BigDecimal amount = BigDecimal.ZERO;
    private List<Long> bonusIds = new ArrayList<>();
    private String remarks;

    public BonusPeriodSummaryDto() {}

    public BonusPeriodSummaryDto(BigDecimal amount, List<Long> bonusIds, String remarks) {
        this.amount = amount != null ? amount : BigDecimal.ZERO;
        this.bonusIds = bonusIds != null ? bonusIds : new ArrayList<>();
        this.remarks = remarks;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public List<Long> getBonusIds() {
        return bonusIds;
    }

    public void setBonusIds(List<Long> bonusIds) {
        this.bonusIds = bonusIds;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
