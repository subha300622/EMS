package com.example.ems.organization.dto;

import java.math.BigDecimal;
import java.util.List;

public class PaymentDtos {

    public record CreateOrderRequest(
            Long invoiceId,
            String gateway
    ) {}

    public record VerifyPaymentRequest(
            String gatewayOrderId,
            String gatewayPaymentId,
            String gatewaySignature,
            String paymentMethod
    ) {}

    public record RefundPaymentRequest(
            BigDecimal amount,
            String reason
    ) {}

    public record PaymentOrderResponse(
            PaymentDto payment,
            InvoiceDto invoice,
            GatewayDto gateway
    ) {
        public record PaymentDto(
                Long id,
                String status,
                String gateway,
                String gatewayOrderId,
                String currency,
                BigDecimal amount,
                BigDecimal displayAmount,
                String receipt
        ) {}

        public record InvoiceDto(
                Long id,
                String invoiceNumber,
                String status
        ) {}

        public record GatewayDto(
                String publicKey,
                String checkoutSessionExpiresAt
        ) {}
    }

    public record VerifyPaymentResponse(
            PaymentVerifyDto payment,
            InvoiceVerifyDto invoice,
            SubscriptionVerifyDto subscription
    ) {
        public record PaymentVerifyDto(
                Long id,
                String gateway,
                String gatewayPaymentId,
                String status,
                String paymentMethod,
                String paidAt
        ) {}

        public record InvoiceVerifyDto(
                Long invoiceId,
                String invoiceNumber,
                String status
        ) {}

        public record SubscriptionVerifyDto(
                Long subscriptionId,
                String status,
                String activatedAt
        ) {}
    }

    public record PaymentDetailResponse(
            Long paymentId,
            String gateway,
            String gatewayOrderId,
            String gatewayPaymentId,
            String invoiceNumber,
            OrganizationSummary organization,
            AmountDto amount,
            String paymentMethod,
            String status,
            String paidAt
    ) {
        public record OrganizationSummary(
                Long id,
                String name
        ) {}

        public record AmountDto(
                String currency,
                BigDecimal value
        ) {}
    }

    public record PaymentHistoryItem(
            Long paymentId,
            String invoiceNumber,
            String organization,
            String gateway,
            BigDecimal amount,
            String currency,
            String status,
            String paidAt
    ) {}

    public record PaymentHistoryResponse(
            List<PaymentHistoryItem> content,
            PaginationDto pagination
    ) {
        public record PaginationDto(
                int page,
                int size,
                long totalElements,
                int totalPages,
                boolean hasNext,
                boolean hasPrevious
        ) {}
    }

    public record RefundResponse(
            Long refundId,
            Long paymentId,
            String gatewayRefundId,
            String invoiceNumber,
            BigDecimal refundAmount,
            String currency,
            String status,
            String requestedAt,
            String reason
    ) {}
}
