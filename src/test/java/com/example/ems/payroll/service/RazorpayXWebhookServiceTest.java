package com.example.ems.payroll.service;

import com.example.ems.common.exception.AccessDeniedException;
import com.example.ems.payroll.entity.OrganizationPaymentConfig;
import com.example.ems.payroll.entity.PaymentEnvironment;
import com.example.ems.payroll.entity.PaymentProviderType;
import com.example.ems.payroll.entity.PayrollPayment;
import com.example.ems.payroll.entity.PayrollPaymentStatus;
import com.example.ems.payroll.entity.PayrollRun;
import com.example.ems.payroll.entity.PayrollRunStatus;
import com.example.ems.payroll.entity.RazorpayXWebhookEvent;
import com.example.ems.payroll.payment.PaymentProvider;
import com.example.ems.payroll.payment.PaymentProviderFactory;
import com.example.ems.payroll.repository.OrganizationPaymentConfigRepository;
import com.example.ems.payroll.repository.PayrollPaymentRepository;
import com.example.ems.payroll.repository.PayrollRunRepository;
import com.example.ems.payroll.repository.RazorpayXWebhookEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RazorpayXWebhookServiceTest {

    @Mock
    private RazorpayXWebhookEventRepository webhookEventRepository;

    @Mock
    private PayrollPaymentRepository payrollPaymentRepository;

    @Mock
    private PayrollRunRepository payrollRunRepository;

    @Mock
    private OrganizationPaymentConfigRepository configRepository;

    @Mock
    private PaymentProvider paymentProvider;

    private RazorpayXWebhookService webhookService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Long orgId = 1L;
    private PayrollPayment payment;
    private OrganizationPaymentConfig config;

    @BeforeEach
    void setUp() {
        lenient().when(paymentProvider.getProviderType()).thenReturn(PaymentProviderType.RAZORPAYX);
        PaymentProviderFactory factory = new PaymentProviderFactory(List.of(paymentProvider));

        webhookService = new RazorpayXWebhookService(
                webhookEventRepository,
                payrollPaymentRepository,
                payrollRunRepository,
                configRepository,
                factory,
                objectMapper
        );

        payment = new PayrollPayment();
        payment.setId(50L);
        payment.setOrganizationId(orgId);
        payment.setPayrollRunId(10L);
        payment.setEmployeeId(100L);
        payment.setPayoutId("pout_12345");
        payment.setStatus(PayrollPaymentStatus.PROCESSING);
        payment.setAmount(BigDecimal.valueOf(50000));

        config = new OrganizationPaymentConfig(
                orgId, PaymentProviderType.RAZORPAYX, PaymentEnvironment.TEST, "k", "s", "acc", "whsec_test"
        );
    }

    @Test
    @DisplayName("Webhook - payout.processed reconciles payment to PAID with UTR")
    void testWebhook_PayoutProcessed_Success() {
        String rawPayload = """
                {
                  "entity": "event",
                  "event": "payout.processed",
                  "event_id": "evt_001",
                  "payload": {
                    "payout": {
                      "entity": {
                        "id": "pout_12345",
                        "status": "processed",
                        "utr": "UTR_SUCCESS_789"
                      }
                    }
                  }
                }
                """;

        when(webhookEventRepository.existsByEventId("evt_001")).thenReturn(false);
        when(payrollPaymentRepository.findByPayoutId("pout_12345")).thenReturn(Optional.of(payment));
        when(configRepository.findByOrganizationId(orgId)).thenReturn(Optional.of(config));
        when(paymentProvider.verifyWebhookSignature(rawPayload, "sig_valid", "whsec_test")).thenReturn(true);
        when(payrollPaymentRepository.findByPayrollRunIdAndOrganizationId(10L, orgId)).thenReturn(List.of(payment));

        PayrollRun run = new PayrollRun();
        run.setId(10L);
        run.setOrganizationId(orgId);
        run.setStatus(PayrollRunStatus.PAYMENT_PROCESSING);
        when(payrollRunRepository.findByIdAndOrganizationId(10L, orgId)).thenReturn(Optional.of(run));

        webhookService.processWebhook(rawPayload, "sig_valid");

        assertEquals(PayrollPaymentStatus.PAID, payment.getStatus());
        assertEquals("UTR_SUCCESS_789", payment.getUtr());
        assertEquals(PayrollRunStatus.PAID, run.getStatus());

        verify(payrollPaymentRepository).save(payment);
        verify(payrollRunRepository).save(run);
        verify(webhookEventRepository).save(argThat(RazorpayXWebhookEvent::getProcessed));
    }

    @Test
    @DisplayName("Webhook - Invalid signature throws AccessDeniedException")
    void testWebhook_InvalidSignature_ThrowsAccessDenied() {
        String rawPayload = """
                {
                  "event": "payout.processed",
                  "event_id": "evt_002",
                  "payload": {
                    "payout": {
                      "entity": {
                        "id": "pout_12345"
                      }
                    }
                  }
                }
                """;

        when(webhookEventRepository.existsByEventId("evt_002")).thenReturn(false);
        when(payrollPaymentRepository.findByPayoutId("pout_12345")).thenReturn(Optional.of(payment));
        when(configRepository.findByOrganizationId(orgId)).thenReturn(Optional.of(config));
        when(paymentProvider.verifyWebhookSignature(rawPayload, "bad_sig", "whsec_test")).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> webhookService.processWebhook(rawPayload, "bad_sig"));
    }

    @Test
    @DisplayName("Webhook - Duplicate event is idempotently ignored")
    void testWebhook_DuplicateEvent_Skipped() {
        String rawPayload = """
                {
                  "event": "payout.processed",
                  "event_id": "evt_duplicate",
                  "payload": {
                    "payout": {
                      "entity": {
                        "id": "pout_12345"
                      }
                    }
                  }
                }
                """;

        when(webhookEventRepository.existsByEventId("evt_duplicate")).thenReturn(true);

        webhookService.processWebhook(rawPayload, "sig");

        verify(payrollPaymentRepository, never()).save(any());
        verify(webhookEventRepository, never()).save(any());
    }
}

