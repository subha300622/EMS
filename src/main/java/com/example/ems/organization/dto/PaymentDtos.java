package com.example.ems.organization.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;

public class PaymentDtos {

    @Schema(description = "Payment order creation request")
    public record CreateOrderRequest(
            @Schema(description = "Invoice ID to pay", example = "1") Long invoiceId,
            @Schema(description = "Payment Gateway name", example = "RAZORPAY") String gateway
    ) {}

    @Schema(description = "Payment verification request")
    public record VerifyPaymentRequest(
            @Schema(description = "Gateway Order ID", example = "order_123") String gatewayOrderId,
            @Schema(description = "Gateway Payment ID", example = "pay_ABC") String gatewayPaymentId,
            @Schema(description = "Gateway Signature String", example = "sig_XYZ") String gatewaySignature,
            @Schema(description = "Payment Method used", example = "UPI") String paymentMethod
    ) {}

    @Schema(description = "Refund initiation request")
    public record RefundPaymentRequest(
            @Schema(description = "Refund Amount", example = "1000.00") BigDecimal amount,
            @Schema(description = "Refund Reason", example = "Plan downgraded") String reason
    ) {}

    @Schema(description = "Payment order created response")
    public record PaymentOrderResponse(
            @Schema(description = "Payment record summary") PaymentDto payment,
            @Schema(description = "Invoice record summary") InvoiceDto invoice,
            @Schema(description = "Gateway metadata") GatewayDto gateway
    ) {
        @Schema(description = "Payment DTO")
        public record PaymentDto(
                @Schema(description = "Payment ID", example = "1") Long id,
                @Schema(description = "Payment Status", example = "CREATED") String status,
                @Schema(description = "Payment Gateway", example = "RAZORPAY") String gateway,
                @Schema(description = "Gateway Order ID", example = "order_123") String gatewayOrderId,
                @Schema(description = "Currency", example = "INR") String currency,
                @Schema(description = "Raw Amount in subunits", example = "5700000") BigDecimal amount,
                @Schema(description = "Display Amount", example = "57000.00") BigDecimal displayAmount,
                @Schema(description = "Invoice Receipt Number", example = "EMS-2026-000001") String receipt
        ) {}

        @Schema(description = "Invoice DTO")
        public record InvoiceDto(
                @Schema(description = "Invoice ID", example = "1") Long id,
                @Schema(description = "Invoice Number", example = "EMS-2026-000001") String invoiceNumber,
                @Schema(description = "Invoice Status", example = "PENDING") String status
        ) {}

        @Schema(description = "Gateway DTO")
        public record GatewayDto(
                @Schema(description = "Public API Key", example = "rzp_test_123") String publicKey,
                @Schema(description = "Session Expiry Timestamp", example = "2026-07-01T11:30:10Z") String checkoutSessionExpiresAt
        ) {}
    }

    @Schema(description = "Payment verified response")
    public record VerifyPaymentResponse(
            @Schema(description = "Payment summary") PaymentVerifyDto payment,
            @Schema(description = "Invoice summary") InvoiceVerifyDto invoice,
            @Schema(description = "Subscription summary") SubscriptionVerifyDto subscription
    ) {
        @Schema(description = "Payment Verify DTO")
        public record PaymentVerifyDto(
                @Schema(description = "Payment ID", example = "1") Long id,
                @Schema(description = "Gateway Name", example = "RAZORPAY") String gateway,
                @Schema(description = "Gateway Payment ID", example = "pay_ABC") String gatewayPaymentId,
                @Schema(description = "Payment Status", example = "SUCCESS") String status,
                @Schema(description = "Payment Method", example = "UPI") String paymentMethod,
                @Schema(description = "Payment Timestamp", example = "2026-07-01T11:03:22Z") String paidAt
        ) {}

        @Schema(description = "Invoice Verify DTO")
        public record InvoiceVerifyDto(
                @Schema(description = "Invoice ID", example = "1") Long invoiceId,
                @Schema(description = "Invoice Number", example = "EMS-2026-000001") String invoiceNumber,
                @Schema(description = "Invoice Status", example = "PAID") String status
        ) {}

        @Schema(description = "Subscription Verify DTO")
        public record SubscriptionVerifyDto(
                @Schema(description = "Subscription ID", example = "1") Long subscriptionId,
                @Schema(description = "Subscription Status", example = "ACTIVE") String status,
                @Schema(description = "Activated Timestamp", example = "2026-07-01T11:03:23Z") String activatedAt
        ) {}
    }

    @Schema(description = "Detailed payment response")
    public record PaymentDetailResponse(
            @Schema(description = "Payment ID", example = "1") Long paymentId,
            @Schema(description = "Gateway Name", example = "RAZORPAY") String gateway,
            @Schema(description = "Gateway Order ID", example = "order_123") String gatewayOrderId,
            @Schema(description = "Gateway Payment ID", example = "pay_ABC") String gatewayPaymentId,
            @Schema(description = "Invoice Number", example = "EMS-2026-000001") String invoiceNumber,
            @Schema(description = "Organization Summary") OrganizationSummary organization,
            @Schema(description = "Amount DTO") AmountDto amount,
            @Schema(description = "Payment Method", example = "UPI") String paymentMethod,
            @Schema(description = "Status", example = "SUCCESS") String status,
            @Schema(description = "Payment Timestamp", example = "2026-07-01T11:03:22Z") String paidAt
    ) {
        @Schema(description = "Organization Summary")
        public record OrganizationSummary(
                @Schema(description = "Organization ID", example = "1") Long id,
                @Schema(description = "Organization Name", example = "Acme Corp") String name
        ) {}

        @Schema(description = "Amount DTO")
        public record AmountDto(
                @Schema(description = "Currency Code", example = "INR") String currency,
                @Schema(description = "Amount Value", example = "57000.00") BigDecimal value
        ) {}
    }

    @Schema(description = "Payment history item")
    public record PaymentHistoryItem(
            @Schema(description = "Payment ID", example = "1") Long paymentId,
            @Schema(description = "Invoice Number", example = "EMS-2026-000001") String invoiceNumber,
            @Schema(description = "Organization Name", example = "Acme Corp") String organization,
            @Schema(description = "Gateway", example = "RAZORPAY") String gateway,
            @Schema(description = "Amount", example = "57000.00") BigDecimal amount,
            @Schema(description = "Currency", example = "INR") String currency,
            @Schema(description = "Payment Status", example = "SUCCESS") String status,
            @Schema(description = "Paid Timestamp", example = "2026-07-01T11:03:22Z") String paidAt
    ) {}

    @Schema(description = "Payment history paginated response")
    public record PaymentHistoryResponse(
            @Schema(description = "Payment history records") List<PaymentHistoryItem> content,
            @Schema(description = "Pagination metadata") PaginationDto pagination
    ) {
        @Schema(description = "Pagination DTO")
        public record PaginationDto(
                @Schema(description = "Current Page (1-based or 0-based)", example = "1") int page,
                @Schema(description = "Page Size", example = "20") int size,
                @Schema(description = "Total Elements", example = "50") long totalElements,
                @Schema(description = "Total Pages", example = "3") int totalPages,
                @Schema(description = "Has Next Page", example = "true") boolean hasNext,
                @Schema(description = "Has Previous Page", example = "false") boolean hasPrevious
        ) {}
    }

    @Schema(description = "Refund initiation response")
    public record RefundResponse(
            @Schema(description = "Refund Record ID", example = "301") Long refundId,
            @Schema(description = "Payment ID", example = "1") Long paymentId,
            @Schema(description = "Gateway Refund ID", example = "rfnd_998") String gatewayRefundId,
            @Schema(description = "Invoice Number", example = "EMS-2026-000001") String invoiceNumber,
            @Schema(description = "Refund Amount", example = "10000.00") BigDecimal refundAmount,
            @Schema(description = "Currency", example = "INR") String currency,
            @Schema(description = "Refund Status", example = "PROCESSING") String status,
            @Schema(description = "Requested Timestamp", example = "2026-07-01T12:15:10Z") String requestedAt,
            @Schema(description = "Refund Reason", example = "Subscription downgraded") String reason
    ) {}
}
