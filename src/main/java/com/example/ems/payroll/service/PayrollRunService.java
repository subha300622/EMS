package com.example.ems.payroll.service;

import com.example.ems.common.exception.BadRequestException;
import com.example.ems.common.exception.ConflictException;
import com.example.ems.common.exception.ResourceNotFoundException;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.payroll.dto.*;
import com.example.ems.payroll.entity.*;
import com.example.ems.payroll.repository.PayrollEmployeeRepository;
import com.example.ems.payroll.repository.PayrollItemRepository;
import com.example.ems.payroll.repository.PayrollRunRepository;
import com.example.ems.security.context.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class PayrollRunService {

    private final PayrollRunRepository payrollRunRepository;
    private final PayrollEmployeeRepository payrollEmployeeRepository;
    private final PayrollItemRepository payrollItemRepository;
    private final EmployeeRepository employeeRepository;
    private final SalaryCalculationService salaryCalculationService;

    public PayrollRunService(PayrollRunRepository payrollRunRepository,
                             PayrollEmployeeRepository payrollEmployeeRepository,
                             PayrollItemRepository payrollItemRepository,
                             EmployeeRepository employeeRepository,
                             SalaryCalculationService salaryCalculationService) {
        this.payrollRunRepository = payrollRunRepository;
        this.payrollEmployeeRepository = payrollEmployeeRepository;
        this.payrollItemRepository = payrollItemRepository;
        this.employeeRepository = employeeRepository;
        this.salaryCalculationService = salaryCalculationService;
    }

    public PayrollRunResponse createPayrollRun(PayrollRunCreateRequest request) {
        Long organizationId = TenantContext.requireOrganizationId();

        if (request.getPeriodStart().isAfter(request.getPeriodEnd())) {
            throw new BadRequestException("periodStart (" + request.getPeriodStart() +
                    ") cannot be after periodEnd (" + request.getPeriodEnd() + ").");
        }

        if (payrollRunRepository.existsByOrganizationIdAndPeriodStartAndPeriodEnd(
                organizationId, request.getPeriodStart(), request.getPeriodEnd())) {
            throw new ConflictException("A payroll run already exists for period " +
                    request.getPeriodStart() + " to " + request.getPeriodEnd() + ".");
        }

        PayrollRun run = new PayrollRun(
                organizationId,
                request.getPeriodStart(),
                request.getPeriodEnd(),
                request.getCurrency() != null ? request.getCurrency() : "INR"
        );

        run = payrollRunRepository.save(run);
        return PayrollRunResponse.fromEntity(run);
    }

    public PayrollRunResponse processPayrollRun(Long runId) {
        Long organizationId = TenantContext.requireOrganizationId();

        PayrollRun run = payrollRunRepository.findByIdAndOrganizationId(runId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Payroll run not found with id: " + runId));

        if (run.getStatus() == PayrollRunStatus.FINALIZED ||
            run.getStatus() == PayrollRunStatus.PAYMENT_PROCESSING ||
            run.getStatus() == PayrollRunStatus.PAID) {
            throw new ConflictException("Payroll run in status " + run.getStatus() + " cannot be re-processed.");
        }

        run.setStatus(PayrollRunStatus.PROCESSING);

        // Delete existing payroll employee snapshot records if re-processing a DRAFT / CALCULATED / FAILED run
        payrollEmployeeRepository.deleteByPayrollRunIdAndOrganizationId(runId, organizationId);

        List<Employee> employees = employeeRepository.findByOrganizationId(organizationId);

        int totalEmployees = employees.size();
        int processedEmployees = 0;
        BigDecimal totalGross = BigDecimal.ZERO;
        BigDecimal totalBenefits = BigDecimal.ZERO;
        BigDecimal totalDeductions = BigDecimal.ZERO;
        BigDecimal totalNet = BigDecimal.ZERO;
        boolean hasFailures = false;

        for (Employee employee : employees) {
            try {
                SalaryCalculationResponse calcResponse = salaryCalculationService.calculateSalaryForDate(
                        employee.getId(),
                        run.getPeriodEnd()
                );

                PayrollEmployee pe = new PayrollEmployee(
                        organizationId,
                        run.getId(),
                        employee.getId(),
                        employee.getFullName(),
                        employee.getEmployeeId(),
                        calcResponse.getGrossPay(),
                        calcResponse.getTotalBenefits(),
                        calcResponse.getTotalDeductions(),
                        calcResponse.getNetPay(),
                        calcResponse.getCurrency(),
                        PayrollEmployeeStatus.CALCULATED,
                        run.getPeriodEnd(),
                        null
                );
                pe = payrollEmployeeRepository.save(pe);

                // Save individual component ledger items
                if (calcResponse.getComponents() != null) {
                    for (SalaryCalculatedComponentResponse item : calcResponse.getComponents()) {
                        PayrollItem pi = new PayrollItem(
                                organizationId,
                                pe.getId(),
                                item.getComponentId(),
                                item.getComponentCode(),
                                item.getComponentName(),
                                item.getComponentType() != null ? item.getComponentType().name() : "EARNING",
                                item.getCalculationType() != null ? item.getCalculationType().name() : "FIXED",
                                item.getAmount(),
                                item.getAppliedRate(),
                                null
                        );
                        payrollItemRepository.save(pi);
                    }
                }

                processedEmployees++;
                totalGross = totalGross.add(calcResponse.getGrossPay() != null ? calcResponse.getGrossPay() : BigDecimal.ZERO);
                totalBenefits = totalBenefits.add(calcResponse.getTotalBenefits() != null ? calcResponse.getTotalBenefits() : BigDecimal.ZERO);
                totalDeductions = totalDeductions.add(calcResponse.getTotalDeductions() != null ? calcResponse.getTotalDeductions() : BigDecimal.ZERO);
                totalNet = totalNet.add(calcResponse.getNetPay() != null ? calcResponse.getNetPay() : BigDecimal.ZERO);

            } catch (Exception ex) {
                hasFailures = true;
                PayrollEmployee failedPe = new PayrollEmployee(
                        organizationId,
                        run.getId(),
                        employee.getId(),
                        employee.getFullName(),
                        employee.getEmployeeId(),
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        run.getCurrency(),
                        PayrollEmployeeStatus.FAILED,
                        run.getPeriodEnd(),
                        ex.getMessage()
                );
                payrollEmployeeRepository.save(failedPe);
            }
        }

        run.setTotalEmployees(totalEmployees);
        run.setProcessedEmployees(processedEmployees);
        run.setTotalGross(totalGross);
        run.setTotalBenefits(totalBenefits);
        run.setTotalDeductions(totalDeductions);
        run.setTotalNet(totalNet);

        if (hasFailures) {
            run.setStatus(PayrollRunStatus.FAILED);
        } else {
            run.setStatus(PayrollRunStatus.CALCULATED);
        }

        run = payrollRunRepository.save(run);
        return PayrollRunResponse.fromEntity(run);
    }

    public PayrollRunResponse finalizePayrollRun(Long runId) {
        Long organizationId = TenantContext.requireOrganizationId();

        PayrollRun run = payrollRunRepository.findByIdAndOrganizationId(runId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Payroll run not found with id: " + runId));

        if (run.getStatus() != PayrollRunStatus.CALCULATED) {
            throw new BadRequestException("Only CALCULATED payroll runs can be finalized. Current status: " + run.getStatus());
        }

        if (run.getProcessedEmployees() < run.getTotalEmployees()) {
            throw new BadRequestException("Cannot finalize payroll run with failed or uncalculated employees. (" +
                    run.getProcessedEmployees() + "/" + run.getTotalEmployees() + " processed).");
        }

        run.setStatus(PayrollRunStatus.FINALIZED);
        run.setFinalizedAt(LocalDateTime.now());
        run = payrollRunRepository.save(run);
        return PayrollRunResponse.fromEntity(run);
    }

    @Transactional(readOnly = true)
    public PayrollRunResponse getPayrollRun(Long runId) {
        Long organizationId = TenantContext.requireOrganizationId();
        PayrollRun run = payrollRunRepository.findByIdAndOrganizationId(runId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Payroll run not found with id: " + runId));
        return PayrollRunResponse.fromEntity(run);
    }

    @Transactional(readOnly = true)
    public List<PayrollRunResponse> listPayrollRuns() {
        Long organizationId = TenantContext.requireOrganizationId();
        return payrollRunRepository.findByOrganizationIdOrderByPeriodStartDesc(organizationId)
                .stream()
                .map(PayrollRunResponse::fromEntity)
                .toList();
    }
}
