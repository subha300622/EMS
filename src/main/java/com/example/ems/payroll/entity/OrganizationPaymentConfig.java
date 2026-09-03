package com.example.ems.payroll.entity;

import com.example.ems.payroll.util.AesEncryptionUtil;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "organization_payment_configs",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_payment_config_org", columnNames = {"organization_id"})
    }
)
public class OrganizationPaymentConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "organization_id", nullable = false, unique = true)
    private Long organizationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 30)
    private PaymentProviderType provider = PaymentProviderType.RAZORPAYX;

    @Enumerated(EnumType.STRING)
    @Column(name = "environment", nullable = false, length = 30)
    private PaymentEnvironment environment = PaymentEnvironment.TEST;

    @Column(name = "api_key")
    private String apiKey;

    @Column(name = "api_secret")
    private String apiSecret;

    @Column(name = "account_number")
    private String accountNumber;

    @Column(name = "webhook_secret")
    private String webhookSecret;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public OrganizationPaymentConfig() {}

    public OrganizationPaymentConfig(Long organizationId, PaymentProviderType provider,
                                     PaymentEnvironment environment, String apiKey,
                                     String apiSecret, String accountNumber, String webhookSecret) {
        this.organizationId = organizationId;
        this.provider = provider != null ? provider : PaymentProviderType.RAZORPAYX;
        this.environment = environment != null ? environment : PaymentEnvironment.TEST;
        setApiKey(apiKey);
        setApiSecret(apiSecret);
        setAccountNumber(accountNumber);
        setWebhookSecret(webhookSecret);
        this.active = true;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.active == null) this.active = true;
        if (this.provider == null) this.provider = PaymentProviderType.RAZORPAYX;
        if (this.environment == null) this.environment = PaymentEnvironment.TEST;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters with transparent encryption/decryption

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public PaymentProviderType getProvider() {
        return provider;
    }

    public void setProvider(PaymentProviderType provider) {
        this.provider = provider;
    }

    public PaymentEnvironment getEnvironment() {
        return environment;
    }

    public void setEnvironment(PaymentEnvironment environment) {
        this.environment = environment;
    }

    public String getApiKey() {
        return AesEncryptionUtil.decrypt(apiKey);
    }

    public void setApiKey(String apiKey) {
        this.apiKey = AesEncryptionUtil.encrypt(apiKey);
    }

    public String getApiSecret() {
        return AesEncryptionUtil.decrypt(apiSecret);
    }

    public void setApiSecret(String apiSecret) {
        this.apiSecret = AesEncryptionUtil.encrypt(apiSecret);
    }

    public String getAccountNumber() {
        return AesEncryptionUtil.decrypt(accountNumber);
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = AesEncryptionUtil.encrypt(accountNumber);
    }

    public String getWebhookSecret() {
        return AesEncryptionUtil.decrypt(webhookSecret);
    }

    public void setWebhookSecret(String webhookSecret) {
        this.webhookSecret = AesEncryptionUtil.encrypt(webhookSecret);
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
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
