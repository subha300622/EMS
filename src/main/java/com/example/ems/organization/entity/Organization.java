package com.example.ems.organization.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "organizations")
public class Organization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "organization_code", unique = true, nullable = false)
    private String organizationCode;

    @Column(nullable = false)
    private String name;

    private String email;
    private String phone;
    private String website;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "deleted_by")
    private String deletedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Version
    private Long version = 0L;

    @OneToOne(mappedBy = "organization", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private OrganizationAddress address;

    @OneToOne(mappedBy = "organization", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private OrganizationSettings settings;

    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Subscription> subscriptions = new ArrayList<>();

    public Organization() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrganizationCode() {
        return organizationCode;
    }

    public void setOrganizationCode(String organizationCode) {
        this.organizationCode = organizationCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Instant deletedAt) {
        this.deletedAt = deletedAt;
    }

    public String getDeletedBy() {
        return deletedBy;
    }

    public void setDeletedBy(String deletedBy) {
        this.deletedBy = deletedBy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public OrganizationAddress getAddress() {
        return address;
    }

    public void setAddress(OrganizationAddress address) {
        this.address = address;
        if (address != null) {
            address.setOrganization(this);
        }
    }

    public OrganizationSettings getSettings() {
        return settings;
    }

    public void setSettings(OrganizationSettings settings) {
        this.settings = settings;
        if (settings != null) {
            settings.setOrganization(this);
        }
    }

    public List<Subscription> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(List<Subscription> subscriptions) {
        this.subscriptions = subscriptions;
    }

    public void addSubscription(Subscription subscription) {
        this.subscriptions.add(subscription);
        subscription.setOrganization(this);
    }

    public Subscription getActiveSubscription() {
        if (subscriptions == null || subscriptions.isEmpty()) return null;
        return subscriptions.stream()
                .filter(s -> s.getStatus() == SubscriptionStatus.ACTIVE)
                .findFirst()
                .orElse(subscriptions.get(subscriptions.size() - 1)); // Fallback to latest
    }
}
