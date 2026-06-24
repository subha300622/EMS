package com.example.ems.onboarding.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "onboarding_template_snapshots")
public class OnboardingTemplateSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "onboarding_id", nullable = false)
    private Onboarding onboarding;

    @Column(nullable = false)
    private Long templateId;

    @Column(nullable = false)
    private int version;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Onboarding getOnboarding() { return onboarding; }
    public void setOnboarding(Onboarding onboarding) { this.onboarding = onboarding; }

    public Long getTemplateId() { return templateId; }
    public void setTemplateId(Long templateId) { this.templateId = templateId; }

    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }
}
