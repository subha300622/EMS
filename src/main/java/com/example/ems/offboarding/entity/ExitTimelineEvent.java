package com.example.ems.offboarding.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "exit_timeline_events")
public class ExitTimelineEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "offboarding_id", nullable = false)
    private Offboarding offboarding;

    @Column(nullable = false)
    private LocalDateTime eventDate = LocalDateTime.now();

    @Column(nullable = false)
    private String action;

    @Column(nullable = false)
    private String performedBy;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Offboarding getOffboarding() { return offboarding; }
    public void setOffboarding(Offboarding offboarding) { this.offboarding = offboarding; }

    public LocalDateTime getEventDate() { return eventDate; }
    public void setEventDate(LocalDateTime eventDate) { this.eventDate = eventDate; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getPerformedBy() { return performedBy; }
    public void setPerformedBy(String performedBy) { this.performedBy = performedBy; }
}
