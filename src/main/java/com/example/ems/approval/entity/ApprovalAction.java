package com.example.ems.approval.entity;

import com.example.ems.employee.entity.Employee;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "approval_actions")
public class ApprovalAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approval_task_id", nullable = false)
    private ApprovalTask approvalTask;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id", nullable = false)
    private Employee actor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApprovalStatus action;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public ApprovalAction() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ApprovalTask getApprovalTask() { return approvalTask; }
    public void setApprovalTask(ApprovalTask approvalTask) { this.approvalTask = approvalTask; }

    public Employee getActor() { return actor; }
    public void setActor(Employee actor) { this.actor = actor; }

    public ApprovalStatus getAction() { return action; }
    public void setAction(ApprovalStatus action) { this.action = action; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
