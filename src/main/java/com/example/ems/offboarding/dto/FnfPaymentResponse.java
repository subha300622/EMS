package com.example.ems.offboarding.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "F&F Payment Confirmation Response")
public class FnfPaymentResponse {

    @Schema(description = "F&F settlement record ID", example = "8001")
    private Long fnfId;

    @Schema(description = "Exit record ID", example = "5001")
    private Long exitId;

    @Schema(description = "Beneficiary employee ID", example = "1025")
    private Long employeeId;

    @Schema(description = "Beneficiary employee full name", example = "Sarah Jenkins")
    private String employeeName;

    @Schema(description = "Updated settlement payment status", example = "PAYMENT_RELEASED")
    private String status;

    @Schema(description = "Disbursement channel/method: BANK_TRANSFER, CHEQUE, UPI", example = "BANK_TRANSFER")
    private String paymentMethod;

    @Schema(description = "Accounting payment release date", example = "2026-10-20")
    private LocalDate paymentDate;

    @Schema(description = "Total net amount disbursed", example = "86766.67")
    private BigDecimal amount;

    @Schema(description = "Banking transaction reference / UTR number", example = "TXN-202610200001")
    private String transactionReference;

    @Schema(description = "Idempotency key associated with release", example = "pay-fnf-8001-run1")
    private String idempotencyKey;

    @Schema(description = "Payment remarks or transaction note", example = "F&F net settlement disbursed to employee salary account.")
    private String remarks;

    @Schema(description = "Exact timestamp when disbursement occurred", example = "2026-10-20T14:35:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime paidAt;

    @Schema(description = "Name of the finance executive or admin who disbursed payment", example = "Robert Chen")
    private String paidByName;

    public FnfPaymentResponse() {}

    public Long getFnfId() { return fnfId; }
    public void setFnfId(Long fnfId) { this.fnfId = fnfId; }

    public Long getExitId() { return exitId; }
    public void setExitId(Long exitId) { this.exitId = exitId; }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public LocalDate getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDate paymentDate) { this.paymentDate = paymentDate; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getTransactionReference() { return transactionReference; }
    public void setTransactionReference(String transactionReference) { this.transactionReference = transactionReference; }

    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public LocalDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }

    public String getPaidByName() { return paidByName; }
    public void setPaidByName(String paidByName) { this.paidByName = paidByName; }
}
