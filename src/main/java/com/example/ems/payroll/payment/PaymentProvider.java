package com.example.ems.payroll.payment;

import com.example.ems.employee.entity.Employee;
import com.example.ems.payroll.dto.EmployeePaymentAccountRequest;
import com.example.ems.payroll.entity.OrganizationPaymentConfig;
import com.example.ems.payroll.entity.PaymentProviderType;

public interface PaymentProvider {

    PaymentProviderType getProviderType();

    PaymentAccountResult createContactAndFundAccount(
            OrganizationPaymentConfig config,
            Employee employee,
            EmployeePaymentAccountRequest request
    );

    PayoutResult executePayout(
            OrganizationPaymentConfig config,
            PayoutRequest request
    );

    PayoutStatusResult fetchPayoutStatus(
            OrganizationPaymentConfig config,
            String payoutId
    );

    boolean verifyWebhookSignature(
            String rawBody,
            String signature,
            String webhookSecret
    );
}
