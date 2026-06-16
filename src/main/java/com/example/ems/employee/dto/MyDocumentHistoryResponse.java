package com.example.ems.employee.dto;

import java.time.LocalDateTime;
import java.util.List;

public class MyDocumentHistoryResponse {

    private List<HistoryItem> content;
    private PaginationInfo pagination;

    public MyDocumentHistoryResponse() {}

    public MyDocumentHistoryResponse(List<HistoryItem> content, PaginationInfo pagination) {
        this.content = content;
        this.pagination = pagination;
    }

    public List<HistoryItem> getContent() {
        return content;
    }

    public void setContent(List<HistoryItem> content) {
        this.content = content;
    }

    public PaginationInfo getPagination() {
        return pagination;
    }

    public void setPagination(PaginationInfo pagination) {
        this.pagination = pagination;
    }

    public static class HistoryItem {
        private Long activityId;
        private String action;
        private String documentName;
        private String performedBy;
        private LocalDateTime performedAt;

        public HistoryItem() {}

        public HistoryItem(Long activityId, String action, String documentName, String performedBy, LocalDateTime performedAt) {
            this.activityId = activityId;
            this.action = action;
            this.documentName = documentName;
            this.performedBy = performedBy;
            this.performedAt = performedAt;
        }

        public Long getActivityId() { return activityId; }
        public void setActivityId(Long activityId) { this.activityId = activityId; }
        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
        public String getDocumentName() { return documentName; }
        public void setDocumentName(String documentName) { this.documentName = documentName; }
        public String getPerformedBy() { return performedBy; }
        public void setPerformedBy(String performedBy) { this.performedBy = performedBy; }
        public LocalDateTime getPerformedAt() { return performedAt; }
        public void setPerformedAt(LocalDateTime performedAt) { this.performedAt = performedAt; }
    }

    public static class PaginationInfo {
        private int page;
        private int size;
        private long totalRecords;
        private int totalPages;

        public PaginationInfo() {}

        public PaginationInfo(int page, int size, long totalRecords, int totalPages) {
            this.page = page;
            this.size = size;
            this.totalRecords = totalRecords;
            this.totalPages = totalPages;
        }

        public int getPage() { return page; }
        public void setPage(int page) { this.page = page; }
        public int getSize() { return size; }
        public void setSize(int size) { this.size = size; }
        public long getTotalRecords() { return totalRecords; }
        public void setTotalRecords(long totalRecords) { this.totalRecords = totalRecords; }
        public int getTotalPages() { return totalPages; }
        public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
    }
}
