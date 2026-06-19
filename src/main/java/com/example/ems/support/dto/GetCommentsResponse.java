package com.example.ems.support.dto;

import java.util.List;

public class GetCommentsResponse {

    private Long ticketId;
    private String ticketNumber;
    private List<CommentDto> comments;

    public GetCommentsResponse() {}

    public GetCommentsResponse(Long ticketId, String ticketNumber, List<CommentDto> comments) {
        this.ticketId = ticketId;
        this.ticketNumber = ticketNumber;
        this.comments = comments;
    }

    public Long getTicketId() { return ticketId; }
    public void setTicketId(Long ticketId) { this.ticketId = ticketId; }

    public String getTicketNumber() { return ticketNumber; }
    public void setTicketNumber(String ticketNumber) { this.ticketNumber = ticketNumber; }

    public List<CommentDto> getComments() { return comments; }
    public void setComments(List<CommentDto> comments) { this.comments = comments; }

    public static class CommentDto {
        private Long id;
        private String commentText;
        private String createdBy;
        private String createdAt;

        public CommentDto() {}

        public CommentDto(Long id, String commentText, String createdBy, String createdAt) {
            this.id = id;
            this.commentText = commentText;
            this.createdBy = createdBy;
            this.createdAt = createdAt;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getCommentText() { return commentText; }
        public void setCommentText(String commentText) { this.commentText = commentText; }

        public String getCreatedBy() { return createdBy; }
        public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    }
}
