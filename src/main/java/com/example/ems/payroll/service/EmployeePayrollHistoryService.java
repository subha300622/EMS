package com.example.ems.payroll.service;

import com.example.ems.common.exception.ResourceNotFoundException;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.payroll.dto.EmployeePayrollHistoryResponse;
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
public class EmployeePayrollHistoryService {

    private final EmployeeRepository employeeRepository;
    private final PayrollEmployeeRepository payrollEmployeeRepository;
    private final PayrollRunRepository payrollRunRepository;
    private final PayrollItemRepository payrollItemRepository;

    public EmployeePayrollHistoryService(EmployeeRepository employeeRepository,
                                         PayrollEmployeeRepository payrollEmployeeRepository,
                                         PayrollRunRepository payrollRunRepository,
                                         PayrollItemRepository payrollItemRepository) {
        this.employeeRepository = employeeRepository;
        this.payrollEmployeeRepository = payrollEmployeeRepository;
        this.payrollRunRepository = payrollRunRepository;
        this.payrollItemRepository = payrollItemRepository;
    }

    public List<EmployeePayrollHistoryResponse> getPayrollHistory(Long employeeId) {
        Long organizationId = TenantContext.requireOrganizationId();

        employeeRepository.findByIdAndOrganizationId(employeeId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));

        List<PayrollEmployee> snapshots = payrollEmployeeRepository
                .findByEmployeeIdAndOrganizationIdOrderByCalculationDateDesc(employeeId, organizationId);

        return snapshots.stream().map(pe -> {
            PayrollRun run = payrollRunRepository.findByIdAndOrganizationId(pe.getPayrollRunId(), organizationId)
                    .orElse(null);
            return EmployeePayrollHistoryResponse.from(pe, run);
        }).toList();
    }

    public PayslipDetailResponse getPayrollHistoryByRun(Long employeeId, Long runId) {
        Long organizationId = TenantContext.requireOrganizationId();

        employeeRepository.findByIdAndOrganizationId(employeeId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));

        PayrollRun run = payrollRunRepository.findByIdAndOrganizationId(runId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Payroll run not found with id: " + runId));

        PayrollEmployee pe = payrollEmployeeRepository
                .findByPayrollRunIdAndEmployeeIdAndOrganizationId(runId, employeeId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Payroll history not found for employee " + employeeId + " in run " + runId));

        List<PayrollItemResponse> items = payrollItemRepository
                .findByPayrollEmployeeIdAndOrganizationIdOrderByIdAsc(pe.getId(), organizationId)
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
