package com.example.ems.reports.revenue.validator;

import com.example.ems.organization.entity.Payment;
import com.example.ems.organization.entity.SubscriptionInvoice;
import com.example.ems.reports.revenue.exception.RevenueReportException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class RevenueIntegrityValidator {

    public void validatePaymentInvoiceIntegrity(Payment payment, SubscriptionInvoice invoice) {
        if (payment == null || invoice == null) {
            throw new RevenueReportException("Payment and invoice entities cannot be null for integrity verification");
        }

        if (payment.getCurrency() != null && invoice.getCurrency() != null &&
            !payment.getCurrency().equalsIgnoreCase(invoice.getCurrency())) {
            throw new RevenueReportException(String.format(
                "Currency mismatch: Payment currency [%s] does not match Invoice currency [%s]",
                payment.getCurrency(), invoice.getCurrency()
            ));
        }

        if (payment.getAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new RevenueReportException("Payment amount cannot be negative");
        }
    }

    public void validateRefundIntegrity(Payment payment, BigDecimal refundAmount) {
        if (payment == null) {
            throw new RevenueReportException("Payment entity cannot be null for refund verification");
        }

        if (refundAmount == null) {
            throw new RevenueReportException("Refund amount cannot be null");
        }

        if (refundAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new RevenueReportException("Refund amount cannot be negative");
        }

        if (refundAmount.compareTo(payment.getAmount()) > 0) {
            throw new RevenueReportException(String.format(
                "Refund amount [%s] cannot exceed the successful payment amount [%s]",
                refundAmount, payment.getAmount()
            ));
        }
    }
}
