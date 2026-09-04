package com.example.ems.payroll.payment;

import com.example.ems.employee.entity.Employee;
import com.example.ems.payroll.dto.EmployeePaymentAccountRequest;
import com.example.ems.payroll.entity.OrganizationPaymentConfig;
import com.example.ems.payroll.entity.PaymentProviderType;
import com.example.ems.payroll.entity.PayrollPaymentStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.UUID;

@Component
public class RazorpayXPaymentProvider implements PaymentProvider {

    private static final Logger log = LoggerFactory.getLogger(RazorpayXPaymentProvider.class);

    @Override
    public PaymentProviderType getProviderType() {
        return PaymentProviderType.RAZORPAYX;
    }

    @Override
    public PaymentAccountResult createContactAndFundAccount(
            OrganizationPaymentConfig config,
            Employee employee,
            EmployeePaymentAccountRequest request) {

        try {
            // Simulated or Sandbox creation for Test environment / offline dev
            String contactId = "cont_" + UUID.nameUUIDFromBytes((employee.getId() + "_" + employee.getEmail()).getBytes(StandardCharsets.UTF_8)).toString().replace("-", "").substring(0, 14);
            String fundAccountId = "fa_" + UUID.nameUUIDFromBytes((contactId + "_" + request.getAccountNumber()).getBytes(StandardCharsets.UTF_8)).toString().replace("-", "").substring(0, 14);

            return PaymentAccountResult.success(contactId, fundAccountId);
        } catch (Exception e) {
            log.error("Failed to create RazorpayX contact/fund account for employee {}", employee.getId(), e);
            return PaymentAccountResult.failure(e.getMessage());
        }
    }

    @Override
    public PayoutResult executePayout(
            OrganizationPaymentConfig config,
            PayoutRequest request) {

        try {
            // Simulated payout dispatch for test / sandbox environment
            String payoutId = "pout_" + UUID.nameUUIDFromBytes(request.getIdempotencyKey().getBytes(StandardCharsets.UTF_8)).toString().replace("-", "").substring(0, 14);
            String utr = "UTR" + System.currentTimeMillis();

            // Payout transitions to PROCESSING / PAID
            return PayoutResult.success(payoutId, PayrollPaymentStatus.PROCESSING, utr);
        } catch (Exception e) {
            log.error("RazorpayX Payout execution failed for idempotencyKey {}", request.getIdempotencyKey(), e);
            return PayoutResult.failure(e.getMessage());
        }
    }

    @Override
    public PayoutStatusResult fetchPayoutStatus(
            OrganizationPaymentConfig config,
            String payoutId) {

        return new PayoutStatusResult(payoutId, PayrollPaymentStatus.PAID, "UTR-" + payoutId, null);
    }

    @Override
    public boolean verifyWebhookSignature(
            String rawBody,
            String signature,
            String webhookSecret) {

        if (rawBody == null || signature == null || webhookSecret == null || webhookSecret.isBlank()) {
            return false;
        }

        try {
            Mac sha256Hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(webhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256Hmac.init(secretKey);
            byte[] hash = sha256Hmac.doFinal(rawBody.getBytes(StandardCharsets.UTF_8));
            String expectedSignature = HexFormat.of().formatHex(hash);

            return MessageDigest.isEqual(
                    expectedSignature.getBytes(StandardCharsets.UTF_8),
                    signature.getBytes(StandardCharsets.UTF_8)
            );
        } catch (Exception e) {
            log.error("Webhook signature verification error", e);
            return false;
        }
    }
}
