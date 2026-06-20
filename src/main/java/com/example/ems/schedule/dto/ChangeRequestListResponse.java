package com.example.ems.schedule.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public class ChangeRequestListResponse {

    private List<ChangeRequestItem> content;
    private PaginationInfo pagination;

    public static class ChangeRequestItem {
        @Schema(example = "1")
        private Long requestId;
        @Schema(example = "string")
        private String requestNumber;
        @Schema(example = "string")
        private String currentShift;
        @Schema(example = "string")
        private String requestedShift;
        @Schema(example = "string")
        private String requestedDate;
        @Schema(example = "ACTIVE")
        private String status;
        @Schema(example = "string")
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
        @Schema(example = "0")
        private Integer page;
        @Schema(example = "10")
        private Integer size;
        @Schema(example = "1")
        private Long totalElements;
        @Schema(example = "0")
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
