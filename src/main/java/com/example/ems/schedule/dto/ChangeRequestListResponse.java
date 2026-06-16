package com.example.ems.schedule.dto;

import java.util.List;

public class ChangeRequestListResponse {

    private List<ChangeRequestItem> content;
    private PaginationInfo pagination;

    public static class ChangeRequestItem {
        private Long requestId;
        private String requestNumber;
        private String currentShift;
        private String requestedShift;
        private String requestedDate;
        private String status;
        private String requestedAt;

        // Getters and Setters
        public Long getRequestId() { return requestId; }
        public void setRequestId(Long requestId) { this.requestId = requestId; }

        public String getRequestNumber() { return requestNumber; }
        public void setRequestNumber(String requestNumber) { this.requestNumber = requestNumber; }

        public String getCurrentShift() { return currentShift; }
        public void setCurrentShift(String currentShift) { this.currentShift = currentShift; }

        public String getRequestedShift() { return requestedShift; }
        public void setRequestedShift(String requestedShift) { this.requestedShift = requestedShift; }

        public String getRequestedDate() { return requestedDate; }
        public void setRequestedDate(String requestedDate) { this.requestedDate = requestedDate; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getRequestedAt() { return requestedAt; }
        public void setRequestedAt(String requestedAt) { this.requestedAt = requestedAt; }
    }

    public static class PaginationInfo {
        private Integer page;
        private Integer size;
        private Long totalElements;
        private Integer totalPages;

        public PaginationInfo(Integer page, Integer size, Long totalElements, Integer totalPages) {
            this.page = page;
            this.size = size;
            this.totalElements = totalElements;
            this.totalPages = totalPages;
        }

        // Getters and Setters
        public Integer getPage() { return page; }
        public void setPage(Integer page) { this.page = page; }

        public Integer getSize() { return size; }
        public void setSize(Integer size) { this.size = size; }

        public Long getTotalElements() { return totalElements; }
        public void setTotalElements(Long totalElements) { this.totalElements = totalElements; }

        public Integer getTotalPages() { return totalPages; }
        public void setTotalPages(Integer totalPages) { this.totalPages = totalPages; }
    }

    // Getters and Setters
    public List<ChangeRequestItem> getContent() { return content; }
    public void setContent(List<ChangeRequestItem> content) { this.content = content; }

    public PaginationInfo getPagination() { return pagination; }
    public void setPagination(PaginationInfo pagination) { this.pagination = pagination; }
}
