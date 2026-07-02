package com.example.ems.support.dto;

import java.util.List;

public class SlaPaginatedResponse {

    private List<SlaResponse> items;
    private PaginationDto pagination;

    public SlaPaginatedResponse() {}

    public SlaPaginatedResponse(List<SlaResponse> items, int page, int size, long totalElements, int totalPages, boolean hasNext, boolean hasPrevious) {
        this.items = items;
        this.pagination = new PaginationDto(page, size, totalElements, totalPages, hasNext, hasPrevious);
    }

    public List<SlaResponse> getItems() { return items; }
    public void setItems(List<SlaResponse> items) { this.items = items; }

    public PaginationDto getPagination() { return pagination; }
    public void setPagination(PaginationDto pagination) { this.pagination = pagination; }

    public static class PaginationDto {
        private int page;
        private int size;
        private long totalElements;
        private int totalPages;
        private boolean hasNext;
        private boolean hasPrevious;

        public PaginationDto(int page, int size, long totalElements, int totalPages, boolean hasNext, boolean hasPrevious) {
            this.page = page;
            this.size = size;
            this.totalElements = totalElements;
            this.totalPages = totalPages;
            this.hasNext = hasNext;
            this.hasPrevious = hasPrevious;
        }

        public int getPage() { return page; }
        public int getSize() { return size; }
        public long getTotalElements() { return totalElements; }
        public int getTotalPages() { return totalPages; }
        public boolean isHasNext() { return hasNext; }
        public boolean isHasPrevious() { return hasPrevious; }
    }
}
