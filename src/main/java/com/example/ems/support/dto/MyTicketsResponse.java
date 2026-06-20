package com.example.ems.support.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public class MyTicketsResponse {
    private List<TicketListItem> content;
    private PaginationDto pagination;

    public MyTicketsResponse() {}

    public MyTicketsResponse(List<TicketListItem> content, PaginationDto pagination) {
        this.content = content;
        this.pagination = pagination;
    }

    public List<TicketListItem> getContent() { return content; }
    public void setContent(List<TicketListItem> content) { this.content = content; }

    public PaginationDto getPagination() { return pagination; }
    public void setPagination(PaginationDto pagination) { this.pagination = pagination; }

    public static class TicketListItem {
        @Schema(example = "1")
        private Long ticketId;
        @Schema(example = "string")
        private String ticketNumber;
        @Schema(example = "Request for Leave")
        private String subject;
        @Schema(example = "string")
        private String category;
        @Schema(example = "string")
        private String priority;
        @Schema(example = "ACTIVE")
        private String status;
        @Schema(example = "string")
        private String assignedTeam;
        @Schema(example = "string")
        private String createdAt;
        @Schema(example = "string")
        private String lastUpdatedAt;

        public TicketListItem() {}

        public TicketListItem(Long ticketId, String ticketNumber, String subject, String category, String priority, String status, String assignedTeam, String createdAt, String lastUpdatedAt) {
            this.ticketId = ticketId;
            this.ticketNumber = ticketNumber;
            this.subject = subject;
            this.category = category;
            this.priority = priority;
            this.status = status;
            this.assignedTeam = assignedTeam;
            this.createdAt = createdAt;
            this.lastUpdatedAt = lastUpdatedAt;
        }

        public Long getTicketId() { return ticketId; }
        public void setTicketId(Long ticketId) { this.ticketId = ticketId; }

        public String getTicketNumber() { return ticketNumber; }
        public void setTicketNumber(String ticketNumber) { this.ticketNumber = ticketNumber; }

        public String getSubject() { return subject; }
        public void setSubject(String subject) { this.subject = subject; }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public String getPriority() { return priority; }
        public void setPriority(String priority) { this.priority = priority; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getAssignedTeam() { return assignedTeam; }
        public void setAssignedTeam(String assignedTeam) { this.assignedTeam = assignedTeam; }

        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

        public String getLastUpdatedAt() { return lastUpdatedAt; }
        public void setLastUpdatedAt(String lastUpdatedAt) { this.lastUpdatedAt = lastUpdatedAt; }
    }

    public static class PaginationDto {
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

        public PaginationDto() {}

        public PaginationDto(int page, int size, long totalElements, int totalPages, boolean hasNext) {
            this.page = page;
            this.size = size;
            this.totalElements = totalElements;
            this.totalPages = totalPages;
            this.hasNext = hasNext;
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
    }
}
