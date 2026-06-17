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

    private LocalDateTime createdAt = LocalDateTime.now();

    public MySupportComment() {}

    public MySupportComment(Long id, MySupportTicket ticket, String commentText, String createdBy) {
        this.id = id;
        this.ticket = ticket;
        this.commentText = commentText;
        this.createdBy = createdBy;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public MySupportTicket getTicket() { return ticket; }
    public void setTicket(MySupportTicket ticket) { this.ticket = ticket; }

    public String getCommentText() { return commentText; }
    public void setCommentText(String commentText) { this.commentText = commentText; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
