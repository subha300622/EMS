package com.example.ems.support.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "my_support_comments")
public class MySupportComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private MySupportTicket ticket;

    @Column(nullable = false, length = 1000)
    private String commentText;

    private String createdBy;

    @Column(name = "is_internal", nullable = false)
    private boolean isInternal = false;

    private LocalDateTime createdAt = LocalDateTime.now();

    public MySupportComment() {}

    public MySupportComment(Long id, MySupportTicket ticket, String commentText, String createdBy) {
        this.id = id;
        this.ticket = ticket;
        this.commentText = commentText;
        this.createdBy = createdBy;
        this.isInternal = false;
    }

    public MySupportComment(Long id, MySupportTicket ticket, String commentText, String createdBy, boolean isInternal) {
        this.id = id;
        this.ticket = ticket;
        this.commentText = commentText;
        this.createdBy = createdBy;
        this.isInternal = isInternal;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public MySupportTicket getTicket() { return ticket; }
    public void setTicket(MySupportTicket ticket) { this.ticket = ticket; }

    public String getCommentText() { return commentText; }
    public void setCommentText(String commentText) { this.commentText = commentText; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public boolean isInternal() { return isInternal; }
    public void setInternal(boolean internal) { isInternal = internal; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
