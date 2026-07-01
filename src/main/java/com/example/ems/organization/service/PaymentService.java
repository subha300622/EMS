package com.example.ems.organization.service;

import com.example.ems.organization.dto.PaymentDtos.*;
import com.example.ems.organization.entity.Payment;
import com.example.ems.organization.entity.Subscription;
import com.example.ems.organization.entity.SubscriptionHistory;
import com.example.ems.organization.entity.SubscriptionInvoice;
import com.example.ems.organization.repository.PaymentRepository;
import com.example.ems.organization.repository.SubscriptionHistoryRepository;
import com.example.ems.organization.repository.SubscriptionInvoiceRepository;
import com.example.ems.organization.repository.SubscriptionRepository;
import com.example.ems.organization.entity.InvoiceStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.context.ApplicationEventPublisher;
import com.example.ems.organization.event.PaymentSucceededEvent;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private SubscriptionInvoiceRepository invoiceRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private SubscriptionHistoryRepository historyRepository;

    @Autowired
    private RazorpayService razorpayService;

    @Autowired
    private com.example.ems.organization.config.RazorpayProperties razorpayProperties;

    public PaymentOrderResponse createPaymentOrder(CreateOrderRequest request, String performedBy) {
        SubscriptionInvoice invoice = invoiceRepository.findById(request.invoiceId())
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + request.invoiceId()));

        if (InvoiceStatus.PAID == invoice.getStatus()) {
            throw new IllegalArgumentException("Invoice is already paid");
        }

        String gatewayOrderId;
        try {
            gatewayOrderId = razorpayService.createRazorpayOrder(invoice.getInvoiceNumber(), invoice.getAmount(), invoice.getCurrency());
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to initiate Razorpay order: " + e.getMessage(), e);
        }

        Payment payment = new Payment();
        payment.setInvoice(invoice);
        payment.setGateway(request.gateway() != null ? request.gateway().toUpperCase() : "RAZORPAY");
        payment.setGatewayOrderId(gatewayOrderId);
        payment.setAmount(invoice.getAmount());
        payment.setCurrency(invoice.getCurrency());
        payment.setStatus("CREATED");
        payment.setReceipt(invoice.getInvoiceNumber());
        payment.setCreatedAt(Instant.now());

        payment = paymentRepository.save(payment);

        // Razorpay expects amount in paise (multiply by 100)
        BigDecimal paiseAmount = invoice.getAmount().multiply(new BigDecimal(100));

        PaymentOrderResponse.PaymentDto paymentDto = new PaymentOrderResponse.PaymentDto(
                payment.getId(),
                payment.getStatus(),
                payment.getGateway(),
                payment.getGatewayOrderId(),
                payment.getCurrency(),
                paiseAmount,
                payment.getAmount(),
                payment.getReceipt()
        );

        PaymentOrderResponse.InvoiceDto invoiceDto = new PaymentOrderResponse.InvoiceDto(
                invoice.getId(),
                invoice.getInvoiceNumber(),
                invoice.getStatus() != null ? invoice.getStatus().name() : null
        );

        PaymentOrderResponse.GatewayDto gatewayDto = new PaymentOrderResponse.GatewayDto(
                razorpayProperties.getKeyId() != null ? razorpayProperties.getKeyId() : "rzp_live_StUZupmMw4H4yc",
                Instant.now().plusSeconds(1800).toString() // Expires in 30 minutes
        );

        return new PaymentOrderResponse(paymentDto, invoiceDto, gatewayDto);
    }

    public VerifyPaymentResponse verifyPayment(VerifyPaymentRequest request, String performedBy) {
        // Level 1 Idempotency: Already processed gatewayPaymentId
        if (request.gatewayPaymentId() != null && paymentRepository.existsByGatewayPaymentId(request.gatewayPaymentId())) {
            log.info("Level 1 Idempotency: Gateway payment ID {} already processed.", request.gatewayPaymentId());
            Payment payment = paymentRepository.findByGatewayOrderId(request.gatewayOrderId())
                    .orElseThrow(() -> new IllegalArgumentException("Payment order not found: " + request.gatewayOrderId()));
            return mapToVerifyResponse(payment);
        }

        Payment payment = paymentRepository.findByGatewayOrderId(request.gatewayOrderId())
                .orElseThrow(() -> new IllegalArgumentException("Payment order not found: " + request.gatewayOrderId()));

        if (!"CREATED".equalsIgnoreCase(payment.getStatus())) {
            if ("SUCCESS".equalsIgnoreCase(payment.getStatus())) {
                return mapToVerifyResponse(payment);
            }
            throw new IllegalArgumentException("Payment is already processed with status: " + payment.getStatus());
        }

        SubscriptionInvoice invoice = payment.getInvoice();
        Subscription subscription = invoice.getSubscription();

        // Update Payment status to SUCCESS
        payment.setGatewayPaymentId(request.gatewayPaymentId());
        payment.setPaymentMethod(request.paymentMethod() != null ? request.paymentMethod().toUpperCase() : "UPI");
        payment.setStatus("SUCCESS");
        payment.setPaidAt(Instant.now());
        payment = paymentRepository.save(payment);

        // Decouple State Updates: Publish PaymentSucceededEvent
        PaymentSucceededEvent succeededEvent = new PaymentSucceededEvent(
                payment.getId(),
                invoice.getId(),
                subscription.getId(),
                payment.getGatewayPaymentId(),
                UUID.randomUUID().toString()
        );
        eventPublisher.publishEvent(succeededEvent);

        return mapToVerifyResponse(payment);
    }

    private VerifyPaymentResponse mapToVerifyResponse(Payment payment) {
        SubscriptionInvoice invoice = payment.getInvoice();
        Subscription subscription = invoice.getSubscription();

        VerifyPaymentResponse.PaymentVerifyDto paymentVerifyDto = new VerifyPaymentResponse.PaymentVerifyDto(
                payment.getId(),
                payment.getGateway(),
                payment.getGatewayPaymentId(),
                payment.getStatus(),
                payment.getPaymentMethod(),
                payment.getPaidAt() != null ? payment.getPaidAt().toString() : Instant.now().toString()
        );

        VerifyPaymentResponse.InvoiceVerifyDto invoiceVerifyDto = new VerifyPaymentResponse.InvoiceVerifyDto(
                invoice.getId(),
                invoice.getInvoiceNumber(),
                "PAID"
        );

        VerifyPaymentResponse.SubscriptionVerifyDto subscriptionVerifyDto = new VerifyPaymentResponse.SubscriptionVerifyDto(
                subscription.getId(),
                "ACTIVE",
                payment.getPaidAt() != null ? payment.getPaidAt().toString() : Instant.now().toString()
        );

        return new VerifyPaymentResponse(paymentVerifyDto, invoiceVerifyDto, subscriptionVerifyDto);
    }

    public PaymentDetailResponse getPaymentDetails(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment transaction not found: " + paymentId));

        SubscriptionInvoice invoice = payment.getInvoice();
        Subscription subscription = invoice.getSubscription();

        PaymentDetailResponse.OrganizationSummary orgSummary = new PaymentDetailResponse.OrganizationSummary(
                subscription.getOrganization() != null ? subscription.getOrganization().getId() : null,
                "Acme Enterprise"
        );

        PaymentDetailResponse.AmountDto amountDto = new PaymentDetailResponse.AmountDto(
                payment.getCurrency(),
                payment.getAmount()
        );

        return new PaymentDetailResponse(
                payment.getId(),
                payment.getGateway(),
                payment.getGatewayOrderId(),
                payment.getGatewayPaymentId(),
                invoice.getInvoiceNumber(),
                orgSummary,
                amountDto,
                payment.getPaymentMethod(),
                payment.getStatus(),
                payment.getPaidAt() != null ? payment.getPaidAt().toString() : null
        );
    }

    public PaymentHistoryResponse getPaymentHistory(int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Payment> paymentPage = paymentRepository.findAll(pageable);

        List<PaymentHistoryItem> items = new ArrayList<>();
        for (Payment payment : paymentPage.getContent()) {
            SubscriptionInvoice invoice = payment.getInvoice();
            items.add(new PaymentHistoryItem(
                    payment.getId(),
                    invoice.getInvoiceNumber(),
                    "Acme Enterprise",
                    payment.getGateway(),
                    payment.getAmount(),
                    payment.getCurrency(),
                    payment.getStatus(),
                    payment.getPaidAt() != null ? payment.getPaidAt().toString() : null
            ));
        }

        PaymentHistoryResponse.PaginationDto paginationDto = new PaymentHistoryResponse.PaginationDto(
                paymentPage.getNumber() + 1,
                paymentPage.getSize(),
                paymentPage.getTotalElements(),
                paymentPage.getTotalPages(),
                paymentPage.hasNext(),
                paymentPage.hasPrevious()
        );

        return new PaymentHistoryResponse(items, paginationDto);
    }

    public RefundResponse refundPayment(Long paymentId, RefundPaymentRequest request, String performedBy) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment transaction not found: " + paymentId));

        if (!"SUCCESS".equalsIgnoreCase(payment.getStatus())) {
            throw new IllegalArgumentException("Only successful payments can be refunded");
        }

        BigDecimal refundAmt = request.amount() != null ? request.amount() : payment.getAmount();

        payment.setGatewayRefundId("rfnd_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12));
        payment.setStatus("REFUNDED");
        payment.setRefundAmount(refundAmt);
        payment.setRefundReason(request.reason() != null ? request.reason() : "Subscription downgraded");
        payment.setRefundedAt(Instant.now());
        paymentRepository.save(payment);

        return new RefundResponse(
                301L,
                payment.getId(),
                payment.getGatewayRefundId(),
                payment.getInvoice().getInvoiceNumber(),
                refundAmt,
                payment.getCurrency(),
                "PROCESSING",
                Instant.now().toString(),
                payment.getRefundReason()
        );
    }

    public void processWebhook(String payload, String signature) {
        if (!razorpayService.verifySignature(payload, signature)) {
            throw new IllegalArgumentException("Invalid webhook signature");
        }

        try {
            org.json.JSONObject json = new org.json.JSONObject(payload);
            String event = json.optString("event");

            org.json.JSONObject payloadObj = json.optJSONObject("payload");
            if (payloadObj == null) return;

            if ("payment.captured".equals(event)) {
                org.json.JSONObject paymentEntity = payloadObj.optJSONObject("payment").optJSONObject("entity");
                String orderId = paymentEntity.optString("order_id");
                String paymentId = paymentEntity.optString("id");
                String method = paymentEntity.optString("method");

                VerifyPaymentRequest verifyRequest = new VerifyPaymentRequest(orderId, paymentId, signature, method);
                verifyPayment(verifyRequest, "webhook-system");
            } else if ("payment.failed".equals(event)) {
                org.json.JSONObject paymentEntity = payloadObj.optJSONObject("payment").optJSONObject("entity");
                String orderId = paymentEntity.optString("order_id");
                String paymentId = paymentEntity.optString("id");

                Payment payment = paymentRepository.findByGatewayOrderId(orderId).orElse(null);
                if (payment != null) {
                    payment.setStatus("FAILED");
                    payment.setGatewayPaymentId(paymentId);
                    paymentRepository.save(payment);
                }
            } else if ("refund.processed".equals(event)) {
                org.json.JSONObject refundEntity = payloadObj.optJSONObject("refund").optJSONObject("entity");
                String paymentId = refundEntity.optString("payment_id");
                String refundId = refundEntity.optString("id");
                int amountInPaise = refundEntity.optInt("amount");
                BigDecimal amount = new BigDecimal(amountInPaise).divide(new BigDecimal(100));

                log.info("Refund processed via webhook: refundId={}, paymentId={}, amount={}", refundId, paymentId, amount);
            }
        } catch (Exception e) {
            log.error("Error processing Razorpay webhook: {}", e.getMessage(), e);
            throw new IllegalArgumentException("Error processing webhook payload: " + e.getMessage());
        }
    }
}
