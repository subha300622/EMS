package com.example.ems.support.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Support Ticket Comment Response")
public class SupportTicketCommentResponse {

    @Schema(description = "Comment ID", example = "1")
    private Long id;

    @Schema(description = "Comment text", example = "Investigating router configuration logs.")
    private String comment;

    @Schema(description = "Whether comment is internal to support team", example = "false")
    private Boolean isInternal;

    @Schema(description = "Author name/email", example = "support.agent@company.com")
    private String createdBy;

    @Schema(description = "Creation timestamp", example = "2026-09-25T11:00:00Z")
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
