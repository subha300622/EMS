package com.example.ems.reports.revenue.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "Revenue refund detail record response")
public class RevenueRefundResponse {
    @Schema(description = "Refund Record ID", example = "REF-001")
    private String refundId;

    @Schema(description = "Organization ID", example = "10")
    private Long organizationId;

    @Schema(description = "Organization Name", example = "Acme Corp")
    private String organizationName;

    @Schema(description = "Payment Database ID", example = "1")
    private Long paymentId;

    @Schema(description = "Refund Amount", example = "50.00")
    private BigDecimal refundAmount;

    @Schema(description = "Refund Reason", example = "Duplicate charge")
    private String refundReason;

    @Schema(description = "Refund Date", example = "2026-07-03")
    private String refundDate;

    @Schema(description = "Payment Gateway Name", example = "RAZORPAY")
    private String gateway;

    public RevenueRefundResponse() {}

    public RevenueRefundResponse(String refundId, Long organizationId, String organizationName, Long paymentId, BigDecimal refundAmount, String refundReason, String refundDate, String gateway) {
        this.refundId = refundId;
        this.organizationId = organizationId;
        this.organizationName = organizationName;
        this.paymentId = paymentId;
        this.refundAmount = refundAmount;
        this.refundReason = refundReason;
        this.refundDate = refundDate;
        this.gateway = gateway;
    }

    public String getRefundId() { return refundId; }
    public void setRefundId(String refundId) { this.refundId = refundId; }

    public Long getOrganizationId() { return organizationId; }
    public void setOrganizationId(Long organizationId) { this.organizationId = organizationId; }

    public String getOrganizationName() { return organizationName; }
    public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }

    public Long getPaymentId() { return paymentId; }
    public void setPaymentId(Long paymentId) { this.paymentId = paymentId; }

    public BigDecimal getRefundAmount() { return refundAmount; }
    public void setRefundAmount(BigDecimal refundAmount) { this.refundAmount = refundAmount; }

    public String getRefundReason() { return refundReason; }
    public void setRefundReason(String refundReason) { this.refundReason = refundReason; }

    public String getRefundDate() { return refundDate; }
    public void setRefundDate(String refundDate) { this.refundDate = refundDate; }

    public String getGateway() { return gateway; }
    public void setGateway(String gateway) { this.gateway = gateway; }
}
