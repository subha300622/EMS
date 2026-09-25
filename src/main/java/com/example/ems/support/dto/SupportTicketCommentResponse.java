package com.example.ems.support.dto;

public class SupportTicketCommentResponse {

    private Long id;
    private String comment;
    private Boolean isInternal;
    private String createdBy;
    private String createdAt;

    public SupportTicketCommentResponse() {}

    public SupportTicketCommentResponse(Long id, String comment, Boolean isInternal, String createdBy, String createdAt) {
        this.id = id;
        this.comment = comment;
        this.isInternal = isInternal;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public Boolean getIsInternal() { return isInternal; }
    public void setIsInternal(Boolean internal) { isInternal = internal; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
