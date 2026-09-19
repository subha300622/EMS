package com.example.ems.offboarding.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "F&F Settlement Payment Release Payload")
public class FnfPaymentRequest {

    @NotBlank(message = "Payment method is required")
    @Schema(description = "Payment mode: BANK_TRANSFER, CHEQUE, UPI", example = "BANK_TRANSFER", requiredMode = Schema.RequiredMode.REQUIRED)
    private String paymentMethod;

    @NotNull(message = "Payment date is required")
    @Schema(description = "Date payment was processed", example = "2026-10-20", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate paymentDate;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    @Schema(description = "Settlement amount released", example = "86766.67", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal amount;

    @NotBlank(message = "Transaction reference is required")
    @Schema(description = "Bank UTR or transaction ID", example = "TXN202610200001", requiredMode = Schema.RequiredMode.REQUIRED)
    private String transactionReference;

    @Pattern(regexp = "^[A-Za-z0-9_\\-\\.:]+$", message = "Idempotency key contains invalid characters")
    @Size(max = 100, message = "Idempotency key must not exceed 100 characters")
    @Schema(description = "Client request retry identity (canonical via Idempotency-Key header)", example = "7c9a12b3-4567")
    private String idempotencyKey;

    @Schema(description = "Payment notes / remarks", example = "F&F payment released")
    private String remarks;

    public FnfPaymentRequest() {}

    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public LocalDate getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDate paymentDate) { this.paymentDate = paymentDate; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getTransactionReference() { return transactionReference; }
    public void setTransactionReference(String transactionReference) { this.transactionReference = transactionReference; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
