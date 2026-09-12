package com.example.ems.payroll.service;

import com.example.ems.payroll.dto.PaymentConfigRequest;
import com.example.ems.payroll.dto.PaymentConfigResponse;
import com.example.ems.payroll.entity.OrganizationPaymentConfig;
import com.example.ems.payroll.entity.PaymentEnvironment;
import com.example.ems.payroll.entity.PaymentProviderType;
import com.example.ems.payroll.repository.OrganizationPaymentConfigRepository;
import com.example.ems.security.context.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PaymentConfigurationServiceTest {

    @Mock
    private OrganizationPaymentConfigRepository configRepository;

    @InjectMocks
    private PaymentConfigurationService configService;

    private final Long orgId = 1L;

    @BeforeEach
    void setUp() {
        TenantContext.setCurrentTenant(orgId);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("Save Payment Config - Encrypts secrets and returns masked response")
    void testSaveOrUpdateConfig_Success() {
        PaymentConfigRequest request = new PaymentConfigRequest(
                PaymentProviderType.RAZORPAYX,
                PaymentEnvironment.TEST,
                "rzp_test_1234567890",
                "secret_key_abcdef",
                "2323230012345678",
                "whsec_test_secret"
        );

        when(configRepository.findByOrganizationId(orgId)).thenReturn(Optional.empty());
        when(configRepository.save(any(OrganizationPaymentConfig.class))).thenAnswer(inv -> {
            OrganizationPaymentConfig c = inv.getArgument(0);
            c.setId(5L);
            return c;
        });

        PaymentConfigResponse response = configService.saveOrUpdateConfig(request);

        assertNotNull(response);
        assertEquals(5L, response.getId());
        assertEquals(PaymentProviderType.RAZORPAYX, response.getProvider());
        assertEquals(PaymentEnvironment.TEST, response.getEnvironment());
        assertTrue(response.getMaskedApiKey().startsWith("****"));
        assertTrue(response.getMaskedAccountNumber().endsWith("5678"));
    }

    @Test
    @DisplayName("Get Payment Config - Returns masked response")
    void testGetConfig_Success() {
        OrganizationPaymentConfig config = new OrganizationPaymentConfig(
                orgId,
                PaymentProviderType.RAZORPAYX,
                PaymentEnvironment.TEST,
                "rzp_test_key",
                "rzp_test_secret",
                "1122334455",
                "whsec_123"
        );
        config.setId(10L);

        when(configRepository.findByOrganizationId(orgId)).thenReturn(Optional.of(config));

        PaymentConfigResponse response = configService.getConfig();

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertTrue(response.getMaskedAccountNumber().endsWith("4455"));
    }
}
