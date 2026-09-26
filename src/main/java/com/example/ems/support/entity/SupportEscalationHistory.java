package com.example.ems.support.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "support_escalation_history", uniqueConstraints = {
    @UniqueConstraint(name = "uk_ticket_escalation_level", columnNames = {"ticket_id", "level"})
})
public class SupportEscalationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private MySupportTicket ticket;

    @Column(nullable = false)
    private Integer level;

    @Column(nullable = false, length = 1000)
    private String reason;

    @Column(name = "triggered_at", nullable = false)
    private LocalDateTime triggeredAt = LocalDateTime.now();

    @Column(nullable = false)
    private String action;

    @Column(nullable = false)
    private String status = "COMPLETED";

    public SupportEscalationHistory() {}

    public SupportEscalationHistory(MySupportTicket ticket, Integer level, String reason, String action, String status) {
        this.ticket = ticket;
        this.level = level;
        this.reason = reason;
        this.action = action;
        this.status = status;
        this.triggeredAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public MySupportTicket getTicket() { return ticket; }
    public void setTicket(MySupportTicket ticket) { this.ticket = ticket; }

    public Integer getLevel() { return level; }
    public void setLevel(Integer level) { this.level = level; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public LocalDateTime getTriggeredAt() { return triggeredAt; }
    public void setTriggeredAt(LocalDateTime triggeredAt) { this.triggeredAt = triggeredAt; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
