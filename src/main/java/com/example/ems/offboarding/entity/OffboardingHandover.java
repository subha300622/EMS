package com.example.ems.offboarding.entity;

import com.example.ems.employee.entity.Employee;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "offboarding_handovers")
public class OffboardingHandover {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "offboarding_id", nullable = false)
    private Offboarding offboarding;

    @Column(nullable = false)
    private String taskName;

    @ManyToOne(optional = false)
    @JoinColumn(name = "recipient_id", nullable = false)
    private Employee recipientEmployee;

    @Column(nullable = false)
    private String status = "PENDING"; // PENDING, COMPLETED

    private LocalDateTime completedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Offboarding getOffboarding() { return offboarding; }
    public void setOffboarding(Offboarding offboarding) { this.offboarding = offboarding; }

    public String getTaskName() { return taskName; }
    public void setTaskName(String taskName) { this.taskName = taskName; }

    public Employee getRecipientEmployee() { return recipientEmployee; }
    public void setRecipientEmployee(Employee recipientEmployee) { this.recipientEmployee = recipientEmployee; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
}
