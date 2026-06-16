package com.example.ems.performance.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "my_goal_milestones")
public class MyGoalMilestone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "goal_id", nullable = false)
    private MyGoal goal;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String status; // PENDING, COMPLETED

    @Column(name = "created_at", nullable = false)
    private java.time.LocalDateTime createdAt = java.time.LocalDateTime.now();

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public MyGoal getGoal() { return goal; }
    public void setGoal(MyGoal goal) { this.goal = goal; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public java.time.LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(java.time.LocalDateTime createdAt) { this.createdAt = createdAt; }
}
