package com.example.ems.payroll.dto;

import com.example.ems.payroll.entity.PaymentEnvironment;
import com.example.ems.payroll.entity.PaymentProviderType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class PaymentConfigRequest {

    @NotNull(message = "provider is required")
    private PaymentProviderType provider = PaymentProviderType.RAZORPAYX;

    @NotNull(message = "environment is required")
    private PaymentEnvironment environment = PaymentEnvironment.TEST;

    @NotBlank(message = "apiKey is required")
    private String apiKey;

    @NotBlank(message = "apiSecret is required")
    private String apiSecret;

    @NotBlank(message = "accountNumber is required")
    private String accountNumber;

    private String webhookSecret;

    public PaymentConfigRequest() {}

    public PaymentConfigRequest(PaymentProviderType provider, PaymentEnvironment environment,
                                String apiKey, String apiSecret, String accountNumber, String webhookSecret) {
        this.provider = provider != null ? provider : PaymentProviderType.RAZORPAYX;
        this.environment = environment != null ? environment : PaymentEnvironment.TEST;
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.accountNumber = accountNumber;
        this.webhookSecret = webhookSecret;
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
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getApiSecret() {
        return apiSecret;
    }

    public void setApiSecret(String apiSecret) {
        this.apiSecret = apiSecret;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getWebhookSecret() {
        return webhookSecret;
    }

    public void setWebhookSecret(String webhookSecret) {
        this.webhookSecret = webhookSecret;
    }
}
