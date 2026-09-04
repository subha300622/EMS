package com.example.ems.payroll.service;

import com.example.ems.common.exception.ResourceNotFoundException;
import com.example.ems.payroll.dto.PayrollEmployeeResponse;
import com.example.ems.payroll.dto.PayrollItemResponse;
import com.example.ems.payroll.dto.PayslipDetailResponse;
import com.example.ems.payroll.entity.PayrollEmployee;
import com.example.ems.payroll.entity.PayrollRun;
import com.example.ems.payroll.repository.PayrollEmployeeRepository;
import com.example.ems.payroll.repository.PayrollItemRepository;
import com.example.ems.payroll.repository.PayrollRunRepository;
import com.example.ems.security.context.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class PayrollDetailService {

    private final PayrollRunRepository payrollRunRepository;
    private final PayrollEmployeeRepository payrollEmployeeRepository;
    private final PayrollItemRepository payrollItemRepository;

    public PayrollDetailService(PayrollRunRepository payrollRunRepository,
                                PayrollEmployeeRepository payrollEmployeeRepository,
                                PayrollItemRepository payrollItemRepository) {
        this.payrollRunRepository = payrollRunRepository;
        this.payrollEmployeeRepository = payrollEmployeeRepository;
        this.payrollItemRepository = payrollItemRepository;
    }

    public List<PayrollEmployeeResponse> getPayrollEmployees(Long runId) {
        Long organizationId = TenantContext.requireOrganizationId();
        // Verify run belongs to tenant
        payrollRunRepository.findByIdAndOrganizationId(runId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Payroll run not found with id: " + runId));

        return payrollEmployeeRepository.findByPayrollRunIdAndOrganizationIdOrderByIdAsc(runId, organizationId)
                .stream()
                .map(PayrollEmployeeResponse::fromEntity)
                .toList();
    }

    private PayrollEmployee findPayrollEmployee(Long runId, Long id, Long organizationId) {
        return payrollEmployeeRepository.findByIdAndOrganizationId(id, organizationId)
                .or(() -> payrollEmployeeRepository.findByPayrollRunIdAndEmployeeIdAndOrganizationId(runId, id, organizationId))
                .orElseThrow(() -> new ResourceNotFoundException("Payroll employee snapshot not found with id: " + id));
    }

    public PayrollEmployeeResponse getPayrollEmployee(Long runId, Long payrollEmployeeId) {
        Long organizationId = TenantContext.requireOrganizationId();
        payrollRunRepository.findByIdAndOrganizationId(runId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Payroll run not found with id: " + runId));

        PayrollEmployee pe = findPayrollEmployee(runId, payrollEmployeeId, organizationId);
        return PayrollEmployeeResponse.fromEntity(pe);
    }

    public List<PayrollItemResponse> getPayrollItems(Long runId, Long payrollEmployeeId) {
        Long organizationId = TenantContext.requireOrganizationId();
        payrollRunRepository.findByIdAndOrganizationId(runId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Payroll run not found with id: " + runId));

        PayrollEmployee pe = findPayrollEmployee(runId, payrollEmployeeId, organizationId);

        return payrollItemRepository.findByPayrollEmployeeIdAndOrganizationIdOrderByIdAsc(pe.getId(), organizationId)
                .stream()
                .map(PayrollItemResponse::fromEntity)
                .toList();
    }

    public PayslipDetailResponse getPayslip(Long runId, Long payrollEmployeeId) {
        Long organizationId = TenantContext.requireOrganizationId();
        PayrollRun run = payrollRunRepository.findByIdAndOrganizationId(runId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Payroll run not found with id: " + runId));

        PayrollEmployee pe = findPayrollEmployee(runId, payrollEmployeeId, organizationId);

        List<PayrollItemResponse> items = payrollItemRepository.findByPayrollEmployeeIdAndOrganizationIdOrderByIdAsc(pe.getId(), organizationId)
                .stream()
                .map(PayrollItemResponse::fromEntity)
                .toList();

        return new PayslipDetailResponse(
                run.getId(),
                pe.getId(),
                pe.getEmployeeId(),
                pe.getEmployeeName(),
                pe.getEmployeeCode(),
                run.getPeriodStart(),
                run.getPeriodEnd(),
                pe.getCurrency(),
                pe.getGrossAmount(),
                pe.getBenefitsAmount(),
                pe.getDeductionsAmount(),
                pe.getNetAmount(),
                pe.getStatus().name(),
                pe.getCalculationDate(),
                items
        );
    }
}
