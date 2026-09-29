package com.example.ems.payroll.payment;

import com.example.ems.employee.entity.Employee;
import com.example.ems.payroll.dto.EmployeePaymentAccountRequest;
import com.example.ems.payroll.entity.OrganizationPaymentConfig;
import com.example.ems.payroll.entity.PaymentProviderType;
import com.example.ems.payroll.entity.PayrollPaymentStatus;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Component
public class MockPaymentProvider implements PaymentProvider {

    @Override
    public PaymentProviderType getProviderType() {
        return PaymentProviderType.MOCK;
    }

    @Override
    public PaymentAccountResult createContactAndFundAccount(
            OrganizationPaymentConfig config,
            Employee employee,
            EmployeePaymentAccountRequest request) {

        String contactId = "mock_cont_" + UUID.nameUUIDFromBytes((employee.getId() + "_" + employee.getEmail()).getBytes(StandardCharsets.UTF_8)).toString().replace("-", "").substring(0, 10);
        String fundAccountId = "mock_fa_" + UUID.nameUUIDFromBytes((contactId + "_" + request.getAccountNumber()).getBytes(StandardCharsets.UTF_8)).toString().replace("-", "").substring(0, 10);

        return PaymentAccountResult.success(contactId, fundAccountId);
    }

    @Override
    public PayoutResult executePayout(
            OrganizationPaymentConfig config,
            PayoutRequest request) {

        if (request.getNarration() != null && request.getNarration().contains("SIMULATE_FAILURE")) {
            return PayoutResult.failure("Simulated payout failure: Insufficient mock account balance or invalid fund account.");
        }

        String payoutId = "mock_pout_" + UUID.nameUUIDFromBytes(request.getIdempotencyKey().getBytes(StandardCharsets.UTF_8)).toString().replace("-", "").substring(0, 10);
        String utr = "MOCK_UTR_" + System.currentTimeMillis();

        return PayoutResult.success(payoutId, PayrollPaymentStatus.PROCESSING, utr);
    }

    @Override
    public PayoutStatusResult fetchPayoutStatus(
            OrganizationPaymentConfig config,
            String payoutId) {

        return new PayoutStatusResult(payoutId, PayrollPaymentStatus.PAID, "MOCK_UTR_" + payoutId, null);
    }

    @Override
    public boolean verifyWebhookSignature(
            String rawBody,
            String signature,
            String webhookSecret) {

        if ("mock_signature".equalsIgnoreCase(signature) || "test_signature".equalsIgnoreCase(signature)) {
            return true;
        }
        if (webhookSecret == null || webhookSecret.isBlank()) {
            return true;
        }
        return true;
    }
}
