package com.example.ems.organization.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDate;
import java.time.Instant;
import java.util.Map;
import java.util.HashMap;

@Entity
@Table(name = "subscriptions")
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    @JsonIgnore
    private Organization organization;

    @Column(name = "plan_code", nullable = false)
    private String planCode;

    @Column(name = "plan_name", nullable = false)
    private String planName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionStatus status;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Column(name = "auto_renew", nullable = false)
    private boolean autoRenew = false;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "billing_info")
    private Map<String, Object> billingInfo = new HashMap<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "limits_info")
    private Map<String, Object> limitsInfo = new HashMap<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "features_info")
    private Map<String, Object> featuresInfo = new HashMap<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payment_info")
    private Map<String, Object> paymentInfo = new HashMap<>();

    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public Subscription() {}

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

    public String getPlanCode() {
        return planCode;
    }

    public void setPlanCode(String planCode) {
        this.planCode = planCode;
    }

    public String getPlanName() {
        return planName;
    }

    public void setPlanName(String planName) {
        this.planName = planName;
    }

    public SubscriptionStatus getStatus() {
        return status;
    }

    public void setStatus(SubscriptionStatus status) {
        this.status = status;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public boolean isAutoRenew() {
        return autoRenew;
    }

    public void setAutoRenew(boolean autoRenew) {
        this.autoRenew = autoRenew;
    }

    public Map<String, Object> getBillingInfo() {
        return billingInfo;
    }

    public void setBillingInfo(Map<String, Object> billingInfo) {
        this.billingInfo = billingInfo;
    }

    public Map<String, Object> getLimitsInfo() {
        return limitsInfo;
    }

    public void setLimitsInfo(Map<String, Object> limitsInfo) {
        this.limitsInfo = limitsInfo;
    }

    public Map<String, Object> getFeaturesInfo() {
        return featuresInfo;
    }

    public void setFeaturesInfo(Map<String, Object> featuresInfo) {
        this.featuresInfo = featuresInfo;
    }

    public Map<String, Object> getPaymentInfo() {
        return paymentInfo;
    }

    public void setPaymentInfo(Map<String, Object> paymentInfo) {
        this.paymentInfo = paymentInfo;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
