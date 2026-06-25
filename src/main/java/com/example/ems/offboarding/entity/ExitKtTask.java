package com.example.ems.offboarding.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "exit_kt_tasks")
public class ExitKtTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "kt_plan_id", nullable = false)
    @JsonIgnore
    private ExitKtPlan ktPlan;

    @Column(nullable = false)
    private String taskName;

    @Column(nullable = false)
    private String status = "PENDING"; // PENDING, COMPLETED, etc.

    private LocalDate dueDate;

    private LocalDateTime completedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ExitKtPlan getKtPlan() { return ktPlan; }
    public void setKtPlan(ExitKtPlan ktPlan) { this.ktPlan = ktPlan; }

    public String getTaskName() { return taskName; }
    public void setTaskName(String taskName) { this.taskName = taskName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
}
