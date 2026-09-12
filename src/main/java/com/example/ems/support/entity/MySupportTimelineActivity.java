package com.example.ems.support.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "my_support_timeline_activities")
public class MySupportTimelineActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private MySupportTicket ticket;

    @Column(nullable = false)
    private String event;

    @Enumerated(EnumType.STRING)
    private SupportTimelineAction action;

    @Column(name = "old_value")
    private String oldValue;

    @Column(name = "new_value")
    private String newValue;

    @Column(nullable = false)
    private String performedBy;

    private LocalDateTime timestamp = LocalDateTime.now();

    public MySupportTimelineActivity() {}

    public MySupportTimelineActivity(Long id, MySupportTicket ticket, String event, String performedBy, LocalDateTime timestamp) {
        this.id = id;
        this.ticket = ticket;
        this.event = event;
        this.performedBy = performedBy;
        this.timestamp = timestamp;
    }

    public MySupportTimelineActivity(Long id, MySupportTicket ticket, String event, SupportTimelineAction action, String oldValue, String newValue, String performedBy, LocalDateTime timestamp) {
        this.id = id;
        this.ticket = ticket;
        this.event = event;
        this.action = action;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.performedBy = performedBy;
        this.timestamp = timestamp;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public MySupportTicket getTicket() { return ticket; }
    public void setTicket(MySupportTicket ticket) { this.ticket = ticket; }

    public String getEvent() { return event; }
    public void setEvent(String event) { this.event = event; }

    public SupportTimelineAction getAction() { return action; }
    public void setAction(SupportTimelineAction action) { this.action = action; }

    public String getOldValue() { return oldValue; }
    public void setOldValue(String oldValue) { this.oldValue = oldValue; }

    public String getNewValue() { return newValue; }
    public void setNewValue(String newValue) { this.newValue = newValue; }

    public String getPerformedBy() { return performedBy; }
    public void setPerformedBy(String performedBy) { this.performedBy = performedBy; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
