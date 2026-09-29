package com.example.ems.payroll.service;

import com.example.ems.common.exception.BadRequestException;
import com.example.ems.common.exception.ResourceNotFoundException;
import com.example.ems.payroll.dto.PaymentExecutionResponse;
import com.example.ems.payroll.dto.PayrollPaymentResponse;
import com.example.ems.payroll.entity.*;
import com.example.ems.payroll.payment.PaymentProvider;
import com.example.ems.payroll.payment.PaymentProviderFactory;
import com.example.ems.payroll.payment.PayoutRequest;
import com.example.ems.payroll.payment.PayoutResult;
import com.example.ems.payroll.repository.*;
import com.example.ems.security.context.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class PayrollPaymentService {

    private final PayrollRunRepository payrollRunRepository;
    private final PayrollEmployeeRepository payrollEmployeeRepository;
    private final PayrollPaymentRepository payrollPaymentRepository;
    private final EmployeePaymentAccountRepository employeePaymentAccountRepository;
    private final OrganizationPaymentConfigRepository configRepository;
    private final PaymentProviderFactory paymentProviderFactory;

    public PayrollPaymentService(PayrollRunRepository payrollRunRepository,
                                 PayrollEmployeeRepository payrollEmployeeRepository,
                                 PayrollPaymentRepository payrollPaymentRepository,
                                 EmployeePaymentAccountRepository employeePaymentAccountRepository,
                                 OrganizationPaymentConfigRepository configRepository,
                                 PaymentProviderFactory paymentProviderFactory) {
        this.payrollRunRepository = payrollRunRepository;
        this.payrollEmployeeRepository = payrollEmployeeRepository;
        this.payrollPaymentRepository = payrollPaymentRepository;
        this.employeePaymentAccountRepository = employeePaymentAccountRepository;
        this.configRepository = configRepository;
        this.paymentProviderFactory = paymentProviderFactory;
    }

    public PaymentExecutionResponse executePayrollPayments(Long runId, String paymentMode) {
        return executePayrollPayments(runId, paymentMode, null);
    }

    public PaymentExecutionResponse executePayrollPayments(Long runId, String paymentMode, String simulation) {
        Long organizationId = TenantContext.requireOrganizationId();

        PayrollRun run = payrollRunRepository.findByIdAndOrganizationId(runId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Payroll run not found with id: " + runId));

        if (run.getStatus() != PayrollRunStatus.FINALIZED &&
            run.getStatus() != PayrollRunStatus.PAYMENT_PROCESSING &&
            run.getStatus() != PayrollRunStatus.PAID) {
            throw new BadRequestException("Payroll run must be FINALIZED before executing payments. Current status: " + run.getStatus());
        }

        OrganizationPaymentConfig config = configRepository.findByOrganizationId(organizationId)
                .orElseGet(() -> {
                    // Fallback to default MOCK configuration if none explicitly created
                    OrganizationPaymentConfig defConfig = new OrganizationPaymentConfig();
                    defConfig.setOrganizationId(organizationId);
                    defConfig.setProvider(PaymentProviderType.MOCK);
                    defConfig.setEnvironment(PaymentEnvironment.TEST);
                    defConfig.setAccountNumber("2323230000000000");
                    defConfig.setActive(true);
                    return configRepository.save(defConfig);
                });

        if (!Boolean.TRUE.equals(config.getActive())) {
            throw new BadRequestException("Organization payment configuration is disabled.");
        }

        PaymentProvider provider = paymentProviderFactory.getProvider(config.getProvider());
        run.setStatus(PayrollRunStatus.PAYMENT_PROCESSING);

        List<PayrollEmployee> employees = payrollEmployeeRepository
                .findByPayrollRunIdAndOrganizationIdOrderByIdAsc(runId, organizationId);

        List<PayrollPaymentResponse> paymentResponses = new ArrayList<>();
        int successfulDispatches = 0;
        int failedDispatches = 0;

        String mode = (paymentMode != null && !paymentMode.isBlank()) ? paymentMode : "NEFT";

        for (PayrollEmployee pe : employees) {
            if (pe.getStatus() != PayrollEmployeeStatus.CALCULATED) {
                continue;
            }

            // Deterministic Idempotency Key
            String idempotencyKey = "PAYROLL-" + run.getId() + "-EMP-" + pe.getEmployeeId();

            PayrollPayment payment = payrollPaymentRepository.findByIdempotencyKey(idempotencyKey)
                    .orElseGet(() -> new PayrollPayment(
                            organizationId,
                            run.getId(),
                            pe.getId(),
                            pe.getEmployeeId(),
                            idempotencyKey,
                            pe.getNetAmount(),
                            pe.getCurrency(),
                            config.getProvider(),
                            mode
                    ));

            if (payment.getStatus() == PayrollPaymentStatus.PAID) {
                paymentResponses.add(PayrollPaymentResponse.fromEntity(payment));
                successfulDispatches++;
                continue;
            }

            EmployeePaymentAccount account = employeePaymentAccountRepository
                    .findByEmployeeIdAndOrganizationId(pe.getEmployeeId(), organizationId)
                    .orElse(null);

            if (account == null || account.getFundAccountId() == null) {
                payment.setStatus(PayrollPaymentStatus.FAILED);
                payment.setFailureReason("Missing registered payment/bank account or fund account ID.");
                payment = payrollPaymentRepository.save(payment);
                paymentResponses.add(PayrollPaymentResponse.fromEntity(payment));
                failedDispatches++;
                continue;
            }

            String narration = "Salary Payout - " + run.getPeriodEnd();
            if ("FAILED".equalsIgnoreCase(simulation)) {
                narration += " SIMULATE_FAILURE";
            }

            PayoutRequest payoutReq = new PayoutRequest(
                    config.getAccountNumber(),
                    account.getFundAccountId(),
                    payment.getAmount(),
                    payment.getCurrency(),
                    mode,
                    "salary",
                    idempotencyKey,
                    narration
            );

            PayoutResult result = provider.executePayout(config, payoutReq);

            if (result.isSuccess()) {
                payment.setPayoutId(result.getPayoutId());
                payment.setStatus(result.getStatus() != null ? result.getStatus() : PayrollPaymentStatus.PROCESSING);
                payment.setUtr(result.getUtr());
                payment.setFailureReason(null);
                successfulDispatches++;
            } else {
                payment.setStatus(PayrollPaymentStatus.FAILED);
                payment.setFailureReason(result.getFailureReason());
                failedDispatches++;
            }

            payment = payrollPaymentRepository.save(payment);
            paymentResponses.add(PayrollPaymentResponse.fromEntity(payment));
        }

        // Determine run status: if all payments are PAID, mark PAID. Otherwise keep PAYMENT_PROCESSING
        boolean allPaid = !paymentResponses.isEmpty() && paymentResponses.stream().allMatch(p -> p.getStatus() == PayrollPaymentStatus.PAID);
        if (allPaid) {
            run.setStatus(PayrollRunStatus.PAID);
        } else {
            run.setStatus(PayrollRunStatus.PAYMENT_PROCESSING);
        }
        payrollRunRepository.save(run);

        return new PaymentExecutionResponse(
                run.getId(),
                paymentResponses.size(),
                successfulDispatches,
                failedDispatches,
                run.getStatus().name(),
                paymentResponses
        );
    }

    public PayrollPaymentResponse retryPayment(Long paymentId) {
        Long organizationId = TenantContext.requireOrganizationId();

        PayrollPayment payment = payrollPaymentRepository.findByIdAndOrganizationId(paymentId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment record not found with id: " + paymentId));

        if (payment.getStatus() == PayrollPaymentStatus.PAID) {
            return PayrollPaymentResponse.fromEntity(payment);
        }

        OrganizationPaymentConfig config = configRepository.findByOrganizationId(organizationId)
                .orElseThrow(() -> new BadRequestException("Organization payment configuration is missing."));

        EmployeePaymentAccount account = employeePaymentAccountRepository
                .findByEmployeeIdAndOrganizationId(payment.getEmployeeId(), organizationId)
                .orElseThrow(() -> new BadRequestException("Employee payment account is missing."));

        PaymentProvider provider = paymentProviderFactory.getProvider(config.getProvider());

        PayoutRequest payoutReq = new PayoutRequest(
                config.getAccountNumber(),
                account.getFundAccountId(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getMode(),
                "salary",
                payment.getIdempotencyKey(),
                "Salary Payout Retry"
        );

        PayoutResult result = provider.executePayout(config, payoutReq);

        if (result.isSuccess()) {
            payment.setPayoutId(result.getPayoutId());
            payment.setStatus(result.getStatus() != null ? result.getStatus() : PayrollPaymentStatus.PROCESSING);
            payment.setUtr(result.getUtr());
            payment.setFailureReason(null);
        } else {
            payment.setStatus(PayrollPaymentStatus.FAILED);
            payment.setFailureReason(result.getFailureReason());
        }

        payment = payrollPaymentRepository.save(payment);
        return PayrollPaymentResponse.fromEntity(payment);
    }

    @Transactional(readOnly = true)
    public List<PayrollPaymentResponse> getPaymentsForRun(Long runId) {
        Long organizationId = TenantContext.requireOrganizationId();
        payrollRunRepository.findByIdAndOrganizationId(runId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Payroll run not found with id: " + runId));

        return payrollPaymentRepository.findByPayrollRunIdAndOrganizationId(runId, organizationId)
                .stream()
                .map(PayrollPaymentResponse::fromEntity)
                .toList();
    }
}
