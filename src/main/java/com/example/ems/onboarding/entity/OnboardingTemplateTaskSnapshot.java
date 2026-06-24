package com.example.ems.onboarding.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "onboarding_template_task_snapshots")
public class OnboardingTemplateTaskSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "snapshot_id", nullable = false)
    private OnboardingTemplateSnapshot snapshot;

    @Column(nullable = false)
    private String title;

    private String description;

    @Column(nullable = false)
    private String phase;

    @Column(nullable = false)
    private String owner;

    private String estimatedTime;

    @Column(nullable = false)
    private int dueDaysAfterJoining = 0;

    @Column(nullable = false)
    private String priority = "MEDIUM";

    @Column(nullable = false)
    private int slaHours = 0;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public OnboardingTemplateSnapshot getSnapshot() { return snapshot; }
    public void setSnapshot(OnboardingTemplateSnapshot snapshot) { this.snapshot = snapshot; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPhase() { return phase; }
    public void setPhase(String phase) { this.phase = phase; }

    public String getOwner() { return owner; }
    public void setOwner(String owner) { this.owner = owner; }

    public String getEstimatedTime() { return estimatedTime; }
    public void setEstimatedTime(String estimatedTime) { this.estimatedTime = estimatedTime; }

    public int getDueDaysAfterJoining() { return dueDaysAfterJoining; }
    public void setDueDaysAfterJoining(int dueDaysAfterJoining) { this.dueDaysAfterJoining = dueDaysAfterJoining; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public int getSlaHours() { return slaHours; }
    public void setSlaHours(int slaHours) { this.slaHours = slaHours; }
}
