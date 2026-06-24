package com.example.ems.onboarding.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "onboarding_phases")
public class OnboardingPhase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "onboarding_id", nullable = false)
    private Onboarding onboarding;

    @Column(nullable = false)
    private String phaseName;

    @Column(nullable = false)
    private int completedTasks = 0;

    @Column(nullable = false)
    private int totalTasks = 0;

    @Column(nullable = false)
    private double weightProgress = 0.0;

    @Column(nullable = false)
    private String status = "NOT_STARTED"; // NOT_STARTED, IN_PROGRESS, COMPLETED

    @Version
    private Long optVersion;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Onboarding getOnboarding() { return onboarding; }
    public void setOnboarding(Onboarding onboarding) { this.onboarding = onboarding; }

    public String getPhaseName() { return phaseName; }
    public void setPhaseName(String phaseName) { this.phaseName = phaseName; }

    public int getCompletedTasks() { return completedTasks; }
    public void setCompletedTasks(int completedTasks) { this.completedTasks = completedTasks; }

    public int getTotalTasks() { return totalTasks; }
    public void setTotalTasks(int totalTasks) { this.totalTasks = totalTasks; }

    public double getWeightProgress() { return weightProgress; }
    public void setWeightProgress(double weightProgress) { this.weightProgress = weightProgress; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getOptVersion() { return optVersion; }
    public void setOptVersion(Long optVersion) { this.optVersion = optVersion; }
}
