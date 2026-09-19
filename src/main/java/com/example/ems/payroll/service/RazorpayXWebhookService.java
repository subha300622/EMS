package com.example.ems.payroll.service;

import com.example.ems.common.exception.AccessDeniedException;
import com.example.ems.common.exception.BadRequestException;
import com.example.ems.payroll.entity.*;
import com.example.ems.payroll.payment.PaymentProvider;
import com.example.ems.payroll.payment.PaymentProviderFactory;
import com.example.ems.payroll.repository.OrganizationPaymentConfigRepository;
import com.example.ems.payroll.repository.PayrollPaymentRepository;
import com.example.ems.payroll.repository.PayrollRunRepository;
import com.example.ems.payroll.repository.RazorpayXWebhookEventRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class RazorpayXWebhookService {

    private static final Logger log = LoggerFactory.getLogger(RazorpayXWebhookService.class);

    private final RazorpayXWebhookEventRepository webhookEventRepository;
    private final PayrollPaymentRepository payrollPaymentRepository;
    private final PayrollRunRepository payrollRunRepository;
    private final OrganizationPaymentConfigRepository configRepository;
    private final PaymentProviderFactory paymentProviderFactory;
    private final ObjectMapper objectMapper;

    public RazorpayXWebhookService(RazorpayXWebhookEventRepository webhookEventRepository,
                                  PayrollPaymentRepository payrollPaymentRepository,
                                  PayrollRunRepository payrollRunRepository,
                                  OrganizationPaymentConfigRepository configRepository,
                                  PaymentProviderFactory paymentProviderFactory,
                                  ObjectMapper objectMapper) {
        this.webhookEventRepository = webhookEventRepository;
        this.payrollPaymentRepository = payrollPaymentRepository;
        this.payrollRunRepository = payrollRunRepository;
        this.configRepository = configRepository;
        this.paymentProviderFactory = paymentProviderFactory;
        this.objectMapper = objectMapper;
    }

    public void processWebhook(String rawBody, String signature) {
        if (rawBody == null || rawBody.isBlank()) {
            throw new BadRequestException("Empty webhook payload.");
        }

        JsonNode root;
        try {
            root = objectMapper.readTree(rawBody);
        } catch (Exception e) {
            throw new BadRequestException("Invalid JSON payload in webhook.");
        }

        String eventType = root.path("event").asText("");
        String eventId = root.path("event_id").asText(root.path("id").asText(""));
        JsonNode payoutNode = root.path("payload").path("payout").path("entity");
        String payoutId = payoutNode.path("id").asText("");

        // Deduplication: If event was already processed, ignore and return cleanly
        if (eventId != null && !eventId.isBlank() && webhookEventRepository.existsByEventId(eventId)) {
            log.info("Webhook event {} already processed. Skipping duplicate.", eventId);
            return;
        }

        // Find associated payment
        PayrollPayment payment = null;
        if (!payoutId.isBlank()) {
            payment = payrollPaymentRepository.findByPayoutId(payoutId).orElse(null);
        }

        Long orgId = payment != null ? payment.getOrganizationId() : null;

        // Verify HMAC-SHA256 signature if config and webhookSecret exist
        if (orgId != null) {
            OrganizationPaymentConfig config = configRepository.findByOrganizationId(orgId).orElse(null);
            if (config != null && config.getWebhookSecret() != null && !config.getWebhookSecret().isBlank()) {
                PaymentProvider provider = paymentProviderFactory.getProvider(config.getProvider());
                boolean valid = provider.verifyWebhookSignature(rawBody, signature, config.getWebhookSecret());
                if (!valid) {
                    log.error("Invalid webhook signature for organization {}", orgId);
                    throw new AccessDeniedException("Invalid webhook signature.");
                }
            }
        }

        // Save event record
        RazorpayXWebhookEvent eventRecord = new RazorpayXWebhookEvent(
                orgId,
                (eventId != null && !eventId.isBlank()) ? eventId : ("EVT_" + System.currentTimeMillis()),
                eventType,
                payoutId,
                rawBody,
                signature
        );

        if (payment != null) {
            String utr = payoutNode.path("utr").asText(null);
            String failureReason = payoutNode.path("failure_reason").asText(null);

            switch (eventType) {
                case "payout.processed" -> {
                    payment.setStatus(PayrollPaymentStatus.PAID);
                    if (utr != null) payment.setUtr(utr);
                    payment.setFailureReason(null);
                }
                case "payout.failed" -> {
                    payment.setStatus(PayrollPaymentStatus.FAILED);
                    if (failureReason != null) payment.setFailureReason(failureReason);
                }
                case "payout.reversed" -> {
                    payment.setStatus(PayrollPaymentStatus.REVERSED);
                    if (failureReason != null) payment.setFailureReason(failureReason);
                }
                default -> log.debug("Unhandled webhook event type: {}", eventType);
            }

            payrollPaymentRepository.save(payment);

            // Check if all payments in the run are now PAID
            List<PayrollPayment> allPayments = payrollPaymentRepository
                    .findByPayrollRunIdAndOrganizationId(payment.getPayrollRunId(), payment.getOrganizationId());
            boolean allPaid = !allPayments.isEmpty() && allPayments.stream().allMatch(p -> p.getStatus() == PayrollPaymentStatus.PAID);

            if (allPaid) {
                payrollRunRepository.findByIdAndOrganizationId(payment.getPayrollRunId(), payment.getOrganizationId())
                        .ifPresent(run -> {
                            run.setStatus(PayrollRunStatus.PAID);
                            payrollRunRepository.save(run);
                        });
            }
        }

        eventRecord.setProcessed(true);
        webhookEventRepository.save(eventRecord);
    }
}
