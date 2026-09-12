package com.example.ems.payroll.service;

import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.payroll.dto.EmployeePaymentAccountRequest;
import com.example.ems.payroll.dto.EmployeePaymentAccountResponse;
import com.example.ems.payroll.entity.*;
import com.example.ems.payroll.payment.PaymentAccountResult;
import com.example.ems.payroll.payment.PaymentProvider;
import com.example.ems.payroll.payment.PaymentProviderFactory;
import com.example.ems.payroll.repository.EmployeePaymentAccountRepository;
import com.example.ems.payroll.repository.OrganizationPaymentConfigRepository;
import com.example.ems.security.context.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmployeePaymentAccountServiceTest {

    @Mock
    private EmployeePaymentAccountRepository paymentAccountRepository;

    @Mock
    private OrganizationPaymentConfigRepository configRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private PaymentProvider paymentProvider;

    private EmployeePaymentAccountService accountService;

    private final Long orgId = 1L;
    private final Long empId = 100L;
    private Employee employee;

    @BeforeEach
    void setUp() {
        TenantContext.setCurrentTenant(orgId);
        lenient().when(paymentProvider.getProviderType()).thenReturn(PaymentProviderType.RAZORPAYX);
        PaymentProviderFactory factory = new PaymentProviderFactory(List.of(paymentProvider));

        accountService = new EmployeePaymentAccountService(
                paymentAccountRepository,
                configRepository,
                employeeRepository,
                factory
        );

        employee = new Employee();
        employee.setId(empId);
        employee.setFullName("Jane Doe");
        employee.setEmail("jane@example.com");
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("Register Employee Account - Success with provider contact and fund account creation")
    void testRegisterAccount_Success() {
        EmployeePaymentAccountRequest request = new EmployeePaymentAccountRequest(
                PaymentProviderType.RAZORPAYX,
                PaymentAccountType.BANK_ACCOUNT,
                "123456789012",
                "HDFC0001234",
                "Jane Doe"
        );

        OrganizationPaymentConfig config = new OrganizationPaymentConfig(
                orgId, PaymentProviderType.RAZORPAYX, PaymentEnvironment.TEST, "k", "s", "acc", "wh"
        );
        config.setActive(true);

        when(employeeRepository.findByIdAndOrganizationId(empId, orgId)).thenReturn(Optional.of(employee));
        when(configRepository.findByOrganizationId(orgId)).thenReturn(Optional.of(config));
        when(paymentProvider.createContactAndFundAccount(config, employee, request))
                .thenReturn(PaymentAccountResult.success("cont_123", "fa_456"));
        when(paymentAccountRepository.findByEmployeeIdAndOrganizationId(empId, orgId)).thenReturn(Optional.empty());
        when(paymentAccountRepository.save(any(EmployeePaymentAccount.class))).thenAnswer(inv -> {
            EmployeePaymentAccount a = inv.getArgument(0);
            a.setId(50L);
            return a;
        });

        EmployeePaymentAccountResponse response = accountService.registerAccount(empId, request);

        assertNotNull(response);
        assertEquals(50L, response.getId());
        assertEquals("cont_123", response.getContactId());
        assertEquals("fa_456", response.getFundAccountId());
        assertTrue(response.getMaskedAccountNumber().endsWith("9012"));
    }
}
