package com.example.ems.payroll.service;

import com.example.ems.common.exception.ResourceNotFoundException;
import com.example.ems.payroll.dto.PaymentConfigRequest;
import com.example.ems.payroll.dto.PaymentConfigResponse;
import com.example.ems.payroll.entity.OrganizationPaymentConfig;
import com.example.ems.payroll.repository.OrganizationPaymentConfigRepository;
import com.example.ems.security.context.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PaymentConfigurationService {

    private final OrganizationPaymentConfigRepository configRepository;

    public PaymentConfigurationService(OrganizationPaymentConfigRepository configRepository) {
        this.configRepository = configRepository;
    }

    public PaymentConfigResponse saveOrUpdateConfig(PaymentConfigRequest request) {
        Long organizationId = TenantContext.requireOrganizationId();

        OrganizationPaymentConfig config = configRepository.findByOrganizationId(organizationId)
                .orElseGet(() -> {
                    OrganizationPaymentConfig newConfig = new OrganizationPaymentConfig();
                    newConfig.setOrganizationId(organizationId);
                    return newConfig;
                });

        config.setProvider(request.getProvider());
        config.setEnvironment(request.getEnvironment());
        config.setApiKey(request.getApiKey());
        config.setApiSecret(request.getApiSecret());
        config.setAccountNumber(request.getAccountNumber());
        if (request.getWebhookSecret() != null) {
            config.setWebhookSecret(request.getWebhookSecret());
        }
        config.setActive(true);

        config = configRepository.save(config);
        return PaymentConfigResponse.fromEntity(config);
    }

    @Transactional(readOnly = true)
    public PaymentConfigResponse getConfig() {
        Long organizationId = TenantContext.requireOrganizationId();
        OrganizationPaymentConfig config = configRepository.findByOrganizationId(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment configuration not found for organization."));
        return PaymentConfigResponse.fromEntity(config);
    }

    public PaymentConfigResponse saveConfiguration(PaymentConfigRequest request) {
        return saveOrUpdateConfig(request);
    }

    @Transactional(readOnly = true)
    public PaymentConfigResponse getConfiguration() {
        return getConfig();
    }

    @Transactional(readOnly = true)
    public OrganizationPaymentConfig getInternalConfig(Long organizationId) {
        return configRepository.findByOrganizationId(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment configuration not found for organization id: " + organizationId));
    }
}
