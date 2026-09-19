package com.example.ems.onboarding.entity;

import com.example.ems.auth.entity.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "onboarding_approvals")
public class OnboardingApproval {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "onboarding_id", nullable = false)
    private Onboarding onboarding;

    @Column(nullable = false)
    private int level = 1;

    @ManyToOne(optional = false)
    @JoinColumn(name = "approver_id", nullable = false)
    private User approver;

    @Column(nullable = false)
    private String status = "PENDING"; // PENDING, APPROVED, REJECTED

    private String remarks;
    private LocalDateTime approvedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Onboarding getOnboarding() { return onboarding; }
    public void setOnboarding(Onboarding onboarding) { this.onboarding = onboarding; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public User getApprover() { return approver; }
    public void setApprover(User approver) { this.approver = approver; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }
}
