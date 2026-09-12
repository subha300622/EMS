package com.example.ems.payroll.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ReimbursementPeriodSummaryDto {

    private BigDecimal amount = BigDecimal.ZERO;
    private List<Long> expenseIds = new ArrayList<>();
    private List<String> expenseNumbers = new ArrayList<>();

    public ReimbursementPeriodSummaryDto() {}

    public ReimbursementPeriodSummaryDto(BigDecimal amount, List<Long> expenseIds, List<String> expenseNumbers) {
        this.amount = amount != null ? amount : BigDecimal.ZERO;
        this.expenseIds = expenseIds != null ? expenseIds : new ArrayList<>();
        this.expenseNumbers = expenseNumbers != null ? expenseNumbers : new ArrayList<>();
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public List<Long> getExpenseIds() {
        return expenseIds;
    }

    public void setExpenseIds(List<Long> expenseIds) {
        this.expenseIds = expenseIds;
    }

    public List<String> getExpenseNumbers() {
        return expenseNumbers;
    }

    public void setExpenseNumbers(List<String> expenseNumbers) {
        this.expenseNumbers = expenseNumbers;
    }
}
