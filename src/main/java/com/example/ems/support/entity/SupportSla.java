package com.example.ems.support.entity;

import com.example.ems.auth.entity.User;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLRestriction;
import java.time.LocalDateTime;

@Entity
@Table(name = "support_sla")
@SQLRestriction("deleted = false")
public class SupportSla {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SupportTicketPriority priority;

    @Column(name = "response_time_minutes", nullable = false)
    private int responseTimeMinutes;

    @Column(name = "resolution_time_minutes", nullable = false)
    private int resolutionTimeMinutes;

    @Column(name = "business_hours_only", nullable = false)
    private boolean businessHoursOnly = false;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(name = "escalation_after_minutes")
    private Integer escalationAfterMinutes;

    @Column(name = "auto_close_after_days")
    private Integer autoCloseAfterDays;

    @Column(name = "warning_before_minutes")
    private Integer warningBeforeMinutes;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault = false;

    @Column(nullable = false)
    private boolean deleted = false;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "created_by_id")
    private User createdBy;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "updated_by_id")
    private User updatedBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public SupportSla() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public SupportTicketPriority getPriority() { return priority; }
    public void setPriority(SupportTicketPriority priority) { this.priority = priority; }

    public int getResponseTimeMinutes() { return responseTimeMinutes; }
    public void setResponseTimeMinutes(int responseTimeMinutes) { this.responseTimeMinutes = responseTimeMinutes; }

    public int getResolutionTimeMinutes() { return resolutionTimeMinutes; }
    public void setResolutionTimeMinutes(int resolutionTimeMinutes) { this.resolutionTimeMinutes = resolutionTimeMinutes; }

    public boolean isBusinessHoursOnly() { return businessHoursOnly; }
    public void setBusinessHoursOnly(boolean businessHoursOnly) { this.businessHoursOnly = businessHoursOnly; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public Integer getEscalationAfterMinutes() { return escalationAfterMinutes; }
    public void setEscalationAfterMinutes(Integer escalationAfterMinutes) { this.escalationAfterMinutes = escalationAfterMinutes; }

    public Integer getAutoCloseAfterDays() { return autoCloseAfterDays; }
    public void setAutoCloseAfterDays(Integer autoCloseAfterDays) { this.autoCloseAfterDays = autoCloseAfterDays; }

    public Integer getWarningBeforeMinutes() { return warningBeforeMinutes; }
    public void setWarningBeforeMinutes(Integer warningBeforeMinutes) { this.warningBeforeMinutes = warningBeforeMinutes; }

    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean isDefault) { this.isDefault = isDefault; }

    public boolean isDeleted() { return deleted; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }

    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }

    public User getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(User updatedBy) { this.updatedBy = updatedBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
