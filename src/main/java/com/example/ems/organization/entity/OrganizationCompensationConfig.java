package com.example.ems.organization.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "organization_compensation_configs", indexes = {
    @Index(name = "idx_org_comp_cfg_org", columnList = "organization_id")
})
public class OrganizationCompensationConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false, unique = true)
    private Organization organization;

    @Column(name = "overtime_enabled", nullable = false)
    private boolean overtimeEnabled = true;

    @Column(name = "incentive_enabled", nullable = false)
    private boolean incentiveEnabled = true;

    @Column(name = "bonus_enabled", nullable = false)
    private boolean bonusEnabled = true;

    @Version
    private Long version;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public OrganizationCompensationConfig() {}

    public OrganizationCompensationConfig(Organization organization) {
        this.organization = organization;
        this.overtimeEnabled = true;
        this.incentiveEnabled = true;
        this.bonusEnabled = true;
    }

    public OrganizationCompensationConfig(Organization organization, boolean overtimeEnabled, boolean incentiveEnabled, boolean bonusEnabled) {
        this.organization = organization;
        this.overtimeEnabled = overtimeEnabled;
        this.incentiveEnabled = incentiveEnabled;
        this.bonusEnabled = bonusEnabled;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public boolean isOvertimeEnabled() {
        return overtimeEnabled;
    }

    public void setOvertimeEnabled(boolean overtimeEnabled) {
        this.overtimeEnabled = overtimeEnabled;
    }

    public boolean isIncentiveEnabled() {
        return incentiveEnabled;
    }

    public void setIncentiveEnabled(boolean incentiveEnabled) {
        this.incentiveEnabled = incentiveEnabled;
    }

    public boolean isBonusEnabled() {
        return bonusEnabled;
    }

    public void setBonusEnabled(boolean bonusEnabled) {
        this.bonusEnabled = bonusEnabled;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
