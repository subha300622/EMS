package com.example.ems.payroll.service;

import com.example.ems.common.exception.BadRequestException;
import com.example.ems.payroll.dto.PaymentExecutionResponse;
import com.example.ems.payroll.entity.*;
import com.example.ems.payroll.payment.PaymentProvider;
import com.example.ems.payroll.payment.PaymentProviderFactory;
import com.example.ems.payroll.payment.PayoutRequest;
import com.example.ems.payroll.payment.PayoutResult;
import com.example.ems.payroll.repository.*;
import com.example.ems.security.context.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PayrollPaymentServiceTest {

    @Mock
    private PayrollRunRepository payrollRunRepository;

    @Mock
    private PayrollEmployeeRepository payrollEmployeeRepository;

    @Mock
    private PayrollPaymentRepository payrollPaymentRepository;

    @Mock
    private EmployeePaymentAccountRepository employeePaymentAccountRepository;

    @Mock
    private OrganizationPaymentConfigRepository configRepository;

    @Mock
    private PaymentProvider paymentProvider;

    private PayrollPaymentService paymentService;

    private final Long orgId = 1L;
    private final Long runId = 10L;
    private PayrollRun run;
    private PayrollEmployee pe;
    private OrganizationPaymentConfig config;
    private EmployeePaymentAccount account;

    @BeforeEach
    void setUp() {
        TenantContext.setCurrentTenant(orgId);

        lenient().when(paymentProvider.getProviderType()).thenReturn(PaymentProviderType.RAZORPAYX);
        PaymentProviderFactory factory = new PaymentProviderFactory(List.of(paymentProvider));

        paymentService = new PayrollPaymentService(
                payrollRunRepository,
                payrollEmployeeRepository,
                payrollPaymentRepository,
                employeePaymentAccountRepository,
                configRepository,
                factory
        );

        run = new PayrollRun(orgId, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30), "INR");
        run.setId(runId);
        run.setStatus(PayrollRunStatus.FINALIZED);

        pe = new PayrollEmployee();
        pe.setId(101L);
        pe.setOrganizationId(orgId);
        pe.setPayrollRunId(runId);
        pe.setEmployeeId(100L);
        pe.setNetAmount(BigDecimal.valueOf(50000));
        pe.setStatus(PayrollEmployeeStatus.CALCULATED);

        config = new OrganizationPaymentConfig(
                orgId, PaymentProviderType.RAZORPAYX, PaymentEnvironment.TEST, "key", "secret", "232323", "whsec"
        );
        config.setActive(true);

        account = new EmployeePaymentAccount(
                orgId, 100L, PaymentProviderType.RAZORPAYX, "cont_100", "fa_100",
                PaymentAccountType.BANK_ACCOUNT, "987654321", "HDFC0001", "John"
        );
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("Execute Payments - Success with deterministic idempotency key and Payout dispatch")
    void testExecutePayments_Success() {
        when(payrollRunRepository.findByIdAndOrganizationId(runId, orgId)).thenReturn(Optional.of(run));
        when(configRepository.findByOrganizationId(orgId)).thenReturn(Optional.of(config));
        when(payrollEmployeeRepository.findByPayrollRunIdAndOrganizationIdOrderByIdAsc(runId, orgId))
                .thenReturn(List.of(pe));
        when(payrollPaymentRepository.findByIdempotencyKey("PAYROLL-10-EMP-100")).thenReturn(Optional.empty());
        when(employeePaymentAccountRepository.findByEmployeeIdAndOrganizationId(100L, orgId)).thenReturn(Optional.of(account));
        when(paymentProvider.executePayout(eq(config), any(PayoutRequest.class)))
                .thenReturn(PayoutResult.success("pout_999", PayrollPaymentStatus.PROCESSING, "UTR123"));
        when(payrollPaymentRepository.save(any(PayrollPayment.class))).thenAnswer(inv -> inv.getArgument(0));

        PaymentExecutionResponse response = paymentService.executePayrollPayments(runId, "NEFT");

        assertNotNull(response);
        assertEquals(1, response.getTotalPayments());
        assertEquals(1, response.getSuccessfulDispatches());
        assertEquals(0, response.getFailedDispatches());
        assertEquals(1, response.getPayments().size());
        assertEquals("PAYROLL-10-EMP-100", response.getPayments().get(0).getIdempotencyKey());
        assertEquals("pout_999", response.getPayments().get(0).getPayoutId());
    }

    @Test
    @DisplayName("Execute Payments - Missing employee account fails individual payment without failing run")
    void testExecutePayments_MissingAccount_FailsPaymentOnly() {
        when(payrollRunRepository.findByIdAndOrganizationId(runId, orgId)).thenReturn(Optional.of(run));
        when(configRepository.findByOrganizationId(orgId)).thenReturn(Optional.of(config));
        when(payrollEmployeeRepository.findByPayrollRunIdAndOrganizationIdOrderByIdAsc(runId, orgId))
                .thenReturn(List.of(pe));
        when(payrollPaymentRepository.findByIdempotencyKey("PAYROLL-10-EMP-100")).thenReturn(Optional.empty());
        when(employeePaymentAccountRepository.findByEmployeeIdAndOrganizationId(100L, orgId)).thenReturn(Optional.empty());
        when(payrollPaymentRepository.save(any(PayrollPayment.class))).thenAnswer(inv -> inv.getArgument(0));

        PaymentExecutionResponse response = paymentService.executePayrollPayments(runId, "NEFT");

        assertNotNull(response);
        assertEquals(1, response.getTotalPayments());
        assertEquals(0, response.getSuccessfulDispatches());
        assertEquals(1, response.getFailedDispatches());
        assertEquals(PayrollPaymentStatus.FAILED, response.getPayments().get(0).getStatus());
        assertEquals("PAYMENT_PROCESSING", response.getRunStatus());
    }

    @Test
    @DisplayName("Execute Payments - Unfinalized run throws BadRequestException")
    void testExecutePayments_UnfinalizedRun_ThrowsBadRequest() {
        run.setStatus(PayrollRunStatus.DRAFT);
        when(payrollRunRepository.findByIdAndOrganizationId(runId, orgId)).thenReturn(Optional.of(run));

        assertThrows(BadRequestException.class, () -> paymentService.executePayrollPayments(runId, "NEFT"));
    }
}
