package com.example.ems.support.entity;

import com.example.ems.organization.entity.Organization;
import jakarta.persistence.*;

@Entity
@Table(name = "support_escalation_rules", uniqueConstraints = {
    @UniqueConstraint(name = "uk_org_escalation_level", columnNames = {"organization_id", "level"})
})
public class SupportEscalationRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(nullable = false)
    private Integer level;

    @Column(name = "trigger_after_minutes", nullable = false)
    private Integer triggerAfterMinutes;

    @Column(nullable = false)
    private String action; // NOTIFY_MANAGER, NOTIFY_SUPPORT_HEAD, REASSIGN

    public SupportEscalationRule() {}

    public SupportEscalationRule(Organization organization, Integer level, Integer triggerAfterMinutes, String action) {
        this.organization = organization;
        this.level = level;
        this.triggerAfterMinutes = triggerAfterMinutes;
        this.action = action;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Organization getOrganization() { return organization; }
    public void setOrganization(Organization organization) { this.organization = organization; }

    public Integer getLevel() { return level; }
    public void setLevel(Integer level) { this.level = level; }

    public Integer getTriggerAfterMinutes() { return triggerAfterMinutes; }
    public void setTriggerAfterMinutes(Integer triggerAfterMinutes) { this.triggerAfterMinutes = triggerAfterMinutes; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
}
