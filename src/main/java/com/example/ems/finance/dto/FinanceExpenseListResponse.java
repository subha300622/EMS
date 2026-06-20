package com.example.ems.finance.dto;

import java.util.List;

public class FinanceExpenseListResponse {
    private List<FinanceExpenseListItem> content;
    private long totalElements;
    private int totalPages;

    public FinanceExpenseListResponse() {}

    public FinanceExpenseListResponse(List<FinanceExpenseListItem> content, long totalElements, int totalPages) {
        this.content = content;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
    }

    public List<FinanceExpenseListItem> getContent() {
        return content;
    }

    public void setContent(List<FinanceExpenseListItem> content) {
        this.content = content;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
}
