package com.example.ems.payroll.service;

import com.example.ems.common.exception.ResourceNotFoundException;
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
public class PayslipService {

    private final PayrollRunRepository payrollRunRepository;
    private final PayrollEmployeeRepository payrollEmployeeRepository;
    private final PayrollItemRepository payrollItemRepository;

    public PayslipService(PayrollRunRepository payrollRunRepository,
                          PayrollEmployeeRepository payrollEmployeeRepository,
                          PayrollItemRepository payrollItemRepository) {
        this.payrollRunRepository = payrollRunRepository;
        this.payrollEmployeeRepository = payrollEmployeeRepository;
        this.payrollItemRepository = payrollItemRepository;
    }

    public List<PayslipDetailResponse> getMyPayslips(Long employeeId) {
        Long organizationId = TenantContext.requireOrganizationId();

        List<PayrollEmployee> employeeSnapshots = payrollEmployeeRepository
                .findByEmployeeIdAndOrganizationIdOrderByCalculationDateDesc(employeeId, organizationId);

        return employeeSnapshots.stream()
                .map(pe -> {
                    PayrollRun run = payrollRunRepository.findByIdAndOrganizationId(pe.getPayrollRunId(), organizationId)
                            .orElse(null);
                    List<PayrollItemResponse> items = payrollItemRepository
                            .findByPayrollEmployeeIdAndOrganizationIdOrderByIdAsc(pe.getId(), organizationId)
                            .stream()
                            .map(PayrollItemResponse::fromEntity)
                            .toList();

                    return new PayslipDetailResponse(
                            run != null ? run.getId() : pe.getPayrollRunId(),
                            pe.getId(),
                            pe.getEmployeeId(),
                            pe.getEmployeeName(),
                            pe.getEmployeeCode(),
                            run != null ? run.getPeriodStart() : null,
                            run != null ? run.getPeriodEnd() : null,
                            pe.getCurrency(),
                            pe.getGrossAmount(),
                            pe.getBenefitsAmount(),
                            pe.getDeductionsAmount(),
                            pe.getNetAmount(),
                            pe.getStatus().name(),
                            pe.getCalculationDate(),
                            items
                    );
                })
                .toList();
    }

    public PayslipDetailResponse getMyPayslip(Long employeeId, Long payrollEmployeeId) {
        Long organizationId = TenantContext.requireOrganizationId();

        PayrollEmployee pe = payrollEmployeeRepository.findByIdAndOrganizationId(payrollEmployeeId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Payslip not found with id: " + payrollEmployeeId));

        if (!pe.getEmployeeId().equals(employeeId)) {
            throw new ResourceNotFoundException("Payslip not found for current employee.");
        }

        PayrollRun run = payrollRunRepository.findByIdAndOrganizationId(pe.getPayrollRunId(), organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Payroll run not found with id: " + pe.getPayrollRunId()));

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
