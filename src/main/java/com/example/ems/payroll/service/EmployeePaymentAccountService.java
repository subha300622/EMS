package com.example.ems.payroll.service;

import com.example.ems.common.exception.BadRequestException;
import com.example.ems.common.exception.ResourceNotFoundException;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.payroll.dto.EmployeePaymentAccountRequest;
import com.example.ems.payroll.dto.EmployeePaymentAccountResponse;
import com.example.ems.payroll.entity.EmployeePaymentAccount;
import com.example.ems.payroll.entity.OrganizationPaymentConfig;
import com.example.ems.payroll.payment.PaymentAccountResult;
import com.example.ems.payroll.payment.PaymentProvider;
import com.example.ems.payroll.payment.PaymentProviderFactory;
import com.example.ems.payroll.repository.EmployeePaymentAccountRepository;
import com.example.ems.payroll.repository.OrganizationPaymentConfigRepository;
import com.example.ems.security.context.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class EmployeePaymentAccountService {

    private final EmployeePaymentAccountRepository paymentAccountRepository;
    private final OrganizationPaymentConfigRepository configRepository;
    private final EmployeeRepository employeeRepository;
    private final PaymentProviderFactory paymentProviderFactory;

    public EmployeePaymentAccountService(EmployeePaymentAccountRepository paymentAccountRepository,
                                         OrganizationPaymentConfigRepository configRepository,
                                         EmployeeRepository employeeRepository,
                                         PaymentProviderFactory paymentProviderFactory) {
        this.paymentAccountRepository = paymentAccountRepository;
        this.configRepository = configRepository;
        this.employeeRepository = employeeRepository;
        this.paymentProviderFactory = paymentProviderFactory;
    }

    public EmployeePaymentAccountResponse registerAccount(Long employeeId, EmployeePaymentAccountRequest request) {
        Long organizationId = TenantContext.requireOrganizationId();

        Employee employee = employeeRepository.findByIdAndOrganizationId(employeeId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));

        OrganizationPaymentConfig config = configRepository.findByOrganizationId(organizationId)
                .orElse(null);

        String contactId = null;
        String fundAccountId = null;

        if (config != null && Boolean.TRUE.equals(config.getActive())) {
            PaymentProvider provider = paymentProviderFactory.getProvider(config.getProvider());
            PaymentAccountResult res = provider.createContactAndFundAccount(config, employee, request);
            if (res.isSuccess()) {
                contactId = res.getContactId();
                fundAccountId = res.getFundAccountId();
            } else {
                throw new BadRequestException("Failed to register account with payment provider: " + res.getErrorMessage());
            }
        }

        EmployeePaymentAccount account = paymentAccountRepository.findByEmployeeIdAndOrganizationId(employeeId, organizationId)
                .orElseGet(() -> {
                    EmployeePaymentAccount newAcc = new EmployeePaymentAccount();
                    newAcc.setOrganizationId(organizationId);
                    newAcc.setEmployeeId(employeeId);
                    return newAcc;
                });

        account.setProvider(request.getProvider());
        account.setAccountType(request.getAccountType());
        account.setAccountNumber(request.getAccountNumber());
        account.setIfscCode(request.getIfscCode());
        account.setBeneficiaryName(request.getBeneficiaryName());
        account.setContactId(contactId);
        account.setFundAccountId(fundAccountId);
        account.setActive(true);

        account = paymentAccountRepository.save(account);
        return EmployeePaymentAccountResponse.fromEntity(account);
    }

    @Transactional(readOnly = true)
    public EmployeePaymentAccountResponse getAccount(Long employeeId) {
        Long organizationId = TenantContext.requireOrganizationId();
        EmployeePaymentAccount account = paymentAccountRepository.findByEmployeeIdAndOrganizationId(employeeId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment account not found for employee id: " + employeeId));
        return EmployeePaymentAccountResponse.fromEntity(account);
    }
}
