package com.example.ems.offboarding.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "offboarding_tasks")
public class OffboardingTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "offboarding_id", nullable = false)
    private Offboarding offboarding;

    @Column(nullable = false)
    private String title;

    private String description;

    @Column(nullable = false)
    private String status = "PENDING"; // PENDING, COMPLETED

    private LocalDate dueDate;

    private LocalDateTime completedAt;

    private String assignedTo = "EMPLOYEE"; // EMPLOYEE, IT, HR, FINANCE, etc.

    private Boolean actionRequired = false;

    private String allowedActions; // Comma-separated allowed actions

    private Long assetId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Offboarding getOffboarding() { return offboarding; }
    public void setOffboarding(Offboarding offboarding) { this.offboarding = offboarding; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public String getAssignedTo() { return assignedTo; }
    public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }

    public Boolean getActionRequired() { return actionRequired; }
    public void setActionRequired(Boolean actionRequired) { this.actionRequired = actionRequired; }

    public String getAllowedActions() { return allowedActions; }
    public void setAllowedActions(String allowedActions) { this.allowedActions = allowedActions; }

    public Long getAssetId() { return assetId; }
    public void setAssetId(Long assetId) { this.assetId = assetId; }
}
