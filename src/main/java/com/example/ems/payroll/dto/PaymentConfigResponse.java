package com.example.ems.payroll.dto;

import com.example.ems.payroll.entity.OrganizationPaymentConfig;
import com.example.ems.payroll.entity.PaymentEnvironment;
import com.example.ems.payroll.entity.PaymentProviderType;

import java.time.LocalDateTime;

public class PaymentConfigResponse {

    private Long id;
    private Long organizationId;
    private PaymentProviderType provider;
    private PaymentEnvironment environment;
    private String maskedApiKey;
    private String maskedAccountNumber;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public PaymentConfigResponse() {}

    public static PaymentConfigResponse fromEntity(OrganizationPaymentConfig config) {
        if (config == null) return null;
        PaymentConfigResponse res = new PaymentConfigResponse();
        res.setId(config.getId());
        res.setOrganizationId(config.getOrganizationId());
        res.setProvider(config.getProvider());
        res.setEnvironment(config.getEnvironment());
        res.setMaskedApiKey(maskSecret(config.getApiKey()));
        res.setMaskedAccountNumber(maskSecret(config.getAccountNumber()));
        res.setActive(config.getActive());
        res.setCreatedAt(config.getCreatedAt());
        res.setUpdatedAt(config.getUpdatedAt());
        return res;
    }

    private static String maskSecret(String secret) {
        if (secret == null || secret.isEmpty()) return "";
        if (secret.length() <= 4) return "****";
        return "****" + secret.substring(secret.length() - 4);
    }

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

    public String getMaskedApiKey() {
        return maskedApiKey;
    }

    public void setMaskedApiKey(String maskedApiKey) {
        this.maskedApiKey = maskedApiKey;
    }

    public String getMaskedAccountNumber() {
        return maskedAccountNumber;
    }

    public void setMaskedAccountNumber(String maskedAccountNumber) {
        this.maskedAccountNumber = maskedAccountNumber;
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
