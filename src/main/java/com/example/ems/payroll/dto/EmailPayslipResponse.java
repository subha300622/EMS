package com.example.ems.payroll.dto;

import java.time.LocalDateTime;

public class EmailPayslipResponse {

    private String message;
    private Long payslipId;
    private LocalDateTime sentAt;

    public EmailPayslipResponse() {}

    public EmailPayslipResponse(String message, Long payslipId, LocalDateTime sentAt) {
        this.message = message;
        this.payslipId = payslipId;
        this.sentAt = sentAt;
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Long getPayslipId() { return payslipId; }
    public void setPayslipId(Long payslipId) { this.payslipId = payslipId; }

    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }
}
