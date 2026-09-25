package com.example.ems.support.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "support_sla_org_rules")
public class SupportSlaRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sla_config_id", nullable = false)
    private SupportSlaConfig slaConfig;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SupportTicketPriority priority;

    @Column(name = "sla_hours", nullable = false)
    private Integer slaHours;

    public SupportSlaRule() {}

    public SupportSlaRule(SupportTicketPriority priority, Integer slaHours) {
        this.priority = priority;
        this.slaHours = slaHours;
    }

    public SupportSlaRule(SupportSlaConfig slaConfig, SupportTicketPriority priority, Integer slaHours) {
        this.slaConfig = slaConfig;
        this.priority = priority;
        this.slaHours = slaHours;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public SupportSlaConfig getSlaConfig() { return slaConfig; }
    public void setSlaConfig(SupportSlaConfig slaConfig) { this.slaConfig = slaConfig; }

    public SupportTicketPriority getPriority() { return priority; }
    public void setPriority(SupportTicketPriority priority) { this.priority = priority; }

    public Integer getSlaHours() { return slaHours; }
    public void setSlaHours(Integer slaHours) { this.slaHours = slaHours; }
}
