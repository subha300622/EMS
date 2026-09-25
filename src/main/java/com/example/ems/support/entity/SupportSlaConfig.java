package com.example.ems.support.entity;

import com.example.ems.organization.entity.Organization;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "support_sla_org_configs")
public class SupportSlaConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false, unique = true)
    private Organization organization;

    @Column(nullable = false)
    private boolean enabled = true;

    @OneToMany(mappedBy = "slaConfig", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<SupportSlaRule> rules = new ArrayList<>();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public SupportSlaConfig() {}

    public SupportSlaConfig(Organization organization, boolean enabled) {
        this.organization = organization;
        this.enabled = enabled;
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Organization getOrganization() { return organization; }
    public void setOrganization(Organization organization) { this.organization = organization; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public List<SupportSlaRule> getRules() { return rules; }
    public void setRules(List<SupportSlaRule> rules) {
        this.rules.clear();
        if (rules != null) {
            for (SupportSlaRule r : rules) {
                r.setSlaConfig(this);
                this.rules.add(r);
            }
        }
    }

    public void addRule(SupportSlaRule rule) {
        rule.setSlaConfig(this);
        this.rules.add(rule);
    }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
