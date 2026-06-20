package com.example.ems.expense.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public class MyExpenseListResponse {
    private List<MyExpenseItem> content;
    private PaginationInfo pagination;

    public MyExpenseListResponse() {}

    public MyExpenseListResponse(List<MyExpenseItem> content, PaginationInfo pagination) {
        this.content = content;
        this.pagination = pagination;
    }

    public List<MyExpenseItem> getContent() {
        return content;
    }

    public void setContent(List<MyExpenseItem> content) {
        this.content = content;
    }

    public PaginationInfo getPagination() {
        return pagination;
    }

    public void setPagination(PaginationInfo pagination) {
        this.pagination = pagination;
    }

    public static class PaginationInfo {
        @Schema(example = "0")
        private int page;
        @Schema(example = "10")
        private int size;
        @Schema(example = "1")
        private long totalElements;
        @Schema(example = "0")
        private int totalPages;
        @Schema(example = "true")
        private boolean hasNext;
        @Schema(example = "true")
        private boolean hasPrevious;

        public PaginationInfo() {}

        public PaginationInfo(int page, int size, long totalElements, int totalPages, boolean hasNext, boolean hasPrevious) {
            this.page = page;
            this.size = size;
            this.totalElements = totalElements;
            this.totalPages = totalPages;
            this.hasNext = hasNext;
            this.hasPrevious = hasPrevious;
        }

        public int getPage() { return page; }
        public void setPage(int page) { this.page = page; }

        public int getSize() { return size; }
        public void setSize(int size) { this.size = size; }

        public long getTotalElements() { return totalElements; }
        public void setTotalElements(long totalElements) { this.totalElements = totalElements; }

        public int getTotalPages() { return totalPages; }
        public void setTotalPages(int totalPages) { this.totalPages = totalPages; }

        public boolean isHasNext() { return hasNext; }
        public void setHasNext(boolean hasNext) { this.hasNext = hasNext; }

        public boolean isHasPrevious() { return hasPrevious; }
        public void setHasPrevious(boolean hasPrevious) { this.hasPrevious = hasPrevious; }
    }
}
