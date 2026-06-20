package com.example.ems.finance.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "finance_onboarding_history")
public class FinanceOnboardingHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "onboarding_id", nullable = false)
    private Long onboardingId;

    @Column(nullable = false)
    private String action; // CREATED, VERIFIED_BANK, VERIFIED_PAN, VERIFIED_UAN, APPROVED, REJECTED, SENT_BACK, SALARY_ASSIGNED, PAYROLL_ACTIVATED

    private String performedBy = "SYSTEM";
    private String notes;
    private LocalDateTime timestamp = LocalDateTime.now();

    public FinanceOnboardingHistory() {}

    public FinanceOnboardingHistory(Long onboardingId, String action, String performedBy, String notes) {
        this.onboardingId = onboardingId;
        this.action = action;
        this.performedBy = performedBy != null ? performedBy : "SYSTEM";
        this.notes = notes;
        this.timestamp = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOnboardingId() {
        return onboardingId;
    }

    public void setOnboardingId(Long onboardingId) {
        this.onboardingId = onboardingId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getPerformedBy() {
        return performedBy;
    }

    public void setPerformedBy(String performedBy) {
        this.performedBy = performedBy;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
