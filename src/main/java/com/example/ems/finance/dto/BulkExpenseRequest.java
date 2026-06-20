package com.example.ems.finance.dto;

import java.util.List;

public class BulkExpenseRequest {
    private List<Long> expenseIds;
    private String remarks;

    public BulkExpenseRequest() {}

    public BulkExpenseRequest(List<Long> expenseIds, String remarks) {
        this.expenseIds = expenseIds;
        this.remarks = remarks;
    }

    public List<Long> getExpenseIds() {
        return expenseIds;
    }

    public void setExpenseIds(List<Long> expenseIds) {
        this.expenseIds = expenseIds;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
