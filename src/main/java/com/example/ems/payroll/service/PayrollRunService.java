package com.example.ems.payroll.service;

import com.example.ems.approval.dto.ApprovalContext;
import com.example.ems.approval.entity.ApprovalWorkflowInstance;
import com.example.ems.approval.entity.WorkflowType;
import com.example.ems.approval.service.ApprovalFacade;
import com.example.ems.common.exception.BadRequestException;
import com.example.ems.common.exception.ConflictException;
import com.example.ems.common.exception.ResourceNotFoundException;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.leave.dto.LeavePeriodSummaryDto;
import com.example.ems.leave.service.LeaveService;
import com.example.ems.payroll.dto.*;
import com.example.ems.payroll.entity.*;
import com.example.ems.payroll.integration.BonusPayrollAdapter;
import com.example.ems.payroll.integration.IncentivePayrollAdapter;
import com.example.ems.payroll.integration.OvertimePayrollAdapter;
import com.example.ems.payroll.integration.ReimbursementPayrollAdapter;
import com.example.ems.payroll.repository.PayrollEmployeeRepository;
import com.example.ems.payroll.repository.PayrollItemRepository;
import com.example.ems.payroll.repository.PayrollRunRepository;
import com.example.ems.payroll.statutory.StatutoryCalculationResult;
import com.example.ems.payroll.statutory.StatutoryEngineService;
import com.example.ems.security.context.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class PayrollRunService {

    private final PayrollRunRepository payrollRunRepository;
    private final PayrollEmployeeRepository payrollEmployeeRepository;
    private final PayrollItemRepository payrollItemRepository;
    private final EmployeeRepository employeeRepository;
    private final SalaryCalculationService salaryCalculationService;
    private final PayrollService payrollService;
    private final LeaveService leaveService;
    private final OvertimePayrollAdapter overtimePayrollAdapter;
    private final IncentivePayrollAdapter incentivePayrollAdapter;
    private final BonusPayrollAdapter bonusPayrollAdapter;
    private final ReimbursementPayrollAdapter reimbursementPayrollAdapter;
    private final StatutoryEngineService statutoryEngineService;

    @Autowired(required = false)
    private ApprovalFacade approvalFacade;

    public PayrollRunService(PayrollRunRepository payrollRunRepository,
                             PayrollEmployeeRepository payrollEmployeeRepository,
                             PayrollItemRepository payrollItemRepository,
                             EmployeeRepository employeeRepository,
                             SalaryCalculationService salaryCalculationService,
                             PayrollService payrollService,
                             LeaveService leaveService,
                             OvertimePayrollAdapter overtimePayrollAdapter,
                             IncentivePayrollAdapter incentivePayrollAdapter,
                             BonusPayrollAdapter bonusPayrollAdapter,
                             ReimbursementPayrollAdapter reimbursementPayrollAdapter,
                             StatutoryEngineService statutoryEngineService) {
        this.payrollRunRepository = payrollRunRepository;
        this.payrollEmployeeRepository = payrollEmployeeRepository;
        this.payrollItemRepository = payrollItemRepository;
        this.employeeRepository = employeeRepository;
        this.salaryCalculationService = salaryCalculationService;
        this.payrollService = payrollService;
        this.leaveService = leaveService;
        this.overtimePayrollAdapter = overtimePayrollAdapter;
        this.incentivePayrollAdapter = incentivePayrollAdapter;
        this.bonusPayrollAdapter = bonusPayrollAdapter;
        this.reimbursementPayrollAdapter = reimbursementPayrollAdapter;
        this.statutoryEngineService = statutoryEngineService;
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

    @Transactional(noRollbackFor = {Exception.class})
    public PayrollRunResponse processPayrollRun(Long runId) {
        Long organizationId = TenantContext.requireOrganizationId();

        PayrollRun run = payrollRunRepository.findByIdAndOrganizationId(runId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Payroll run not found with id: " + runId));

        if (!isStatusEditable(run.getStatus())) {
            throw new ConflictException("Payroll run in status " + run.getStatus() + " is immutable and cannot be re-processed.");
        }

        run.setStatus(PayrollRunStatus.PROCESSING);

        // Delete existing payroll employee snapshot records and child items if re-processing a DRAFT / CALCULATED / REJECTED run
        List<PayrollEmployee> existingPes = payrollEmployeeRepository.findByPayrollRunIdAndOrganizationIdOrderByIdAsc(runId, organizationId);
        if (!existingPes.isEmpty()) {
            for (PayrollEmployee pe : existingPes) {
                payrollItemRepository.deleteByPayrollEmployeeIdAndOrganizationId(pe.getId(), organizationId);
            }
            payrollItemRepository.flush();
            payrollEmployeeRepository.deleteAll(existingPes);
            payrollEmployeeRepository.flush();
        }

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
                // 1. Fixed Salary Calculation from DAG
                SalaryCalculationResponse calcResponse = salaryCalculationService.calculateSalaryForDate(
                        employee.getId(),
                        run.getPeriodEnd()
                );
                BigDecimal fixedGross = calcResponse.getGrossPay() != null ? calcResponse.getGrossPay() : BigDecimal.ZERO;
                BigDecimal fixedDeductions = calcResponse.getTotalDeductions() != null ? calcResponse.getTotalDeductions() : BigDecimal.ZERO;

                // 2. Working days and Leave Period Summary (Paid, LOP, Encashment)
                int workingDays = payrollService != null ? payrollService.calculateWorkingDays(organizationId, run.getPeriodStart(), run.getPeriodEnd()) : 22;
                LeavePeriodSummaryDto leaveSummary = leaveService != null ? leaveService.getLeavePeriodSummary(employee.getId(), run.getPeriodStart(), run.getPeriodEnd()) : null;
                double lopDays = leaveSummary != null && leaveSummary.getLopDays() != null ? leaveSummary.getLopDays() : 0.0;
                double encashmentDays = leaveSummary != null && leaveSummary.getEncashmentDays() != null ? leaveSummary.getEncashmentDays() : 0.0;

                BigDecimal dailyRate = workingDays > 0 ? fixedGross.divide(BigDecimal.valueOf(workingDays), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
                BigDecimal lopDeduction = dailyRate.multiply(BigDecimal.valueOf(lopDays)).setScale(2, RoundingMode.HALF_UP);
                BigDecimal encashmentAddition = dailyRate.multiply(BigDecimal.valueOf(encashmentDays)).setScale(2, RoundingMode.HALF_UP);

                // 3. Approved Overtime
                OvertimePeriodSummaryDto otSummary = overtimePayrollAdapter != null ? overtimePayrollAdapter.getOvertimeSummary(
                        employee.getId(), organizationId, run.getPeriodStart(), run.getPeriodEnd(), workingDays, calcResponse
                ) : null;
                BigDecimal otAmount = otSummary != null && otSummary.getAmount() != null ? otSummary.getAmount() : BigDecimal.ZERO;

                // 4. Approved Incentive & Bonus
                IncentivePeriodSummaryDto incentiveSummary = incentivePayrollAdapter != null ? incentivePayrollAdapter.getIncentiveSummary(
                        employee.getId(), organizationId, run.getPeriodStart(), run.getPeriodEnd()
                ) : null;
                BigDecimal incentiveAmount = incentiveSummary != null && incentiveSummary.getAmount() != null ? incentiveSummary.getAmount() : BigDecimal.ZERO;

                BonusPeriodSummaryDto bonusSummary = bonusPayrollAdapter != null ? bonusPayrollAdapter.getBonusSummary(
                        employee.getId(), organizationId, run.getPeriodStart(), run.getPeriodEnd()
                ) : null;
                BigDecimal bonusAmount = bonusSummary != null && bonusSummary.getAmount() != null ? bonusSummary.getAmount() : BigDecimal.ZERO;

                // 5. Approved Expense Reimbursements (Tracked separately from Salary Gross)
                ReimbursementPeriodSummaryDto reimbursementSummary = reimbursementPayrollAdapter != null ? reimbursementPayrollAdapter.getReimbursementSummary(
                        employee.getId(), organizationId, run.getPeriodStart(), run.getPeriodEnd()
                ) : null;
                BigDecimal reimbursementAmount = reimbursementSummary != null && reimbursementSummary.getAmount() != null ? reimbursementSummary.getAmount() : BigDecimal.ZERO;

                // 6. Aggregate Adjusted Gross (Payroll Earnings)
                BigDecimal variableAdditions = otAmount.add(incentiveAmount).add(bonusAmount).add(encashmentAddition);
                BigDecimal adjustedGross = fixedGross.add(variableAdditions).subtract(lopDeduction);
                if (adjustedGross.compareTo(BigDecimal.ZERO) < 0) {
                    adjustedGross = BigDecimal.ZERO;
                }

                // 7. Dynamic Statutory Calculations (PF, ESI, PT, TDS)
                boolean hasPfInDag = false;
                boolean hasEsiInDag = false;

                if (calcResponse.getComponents() != null) {
                    for (SalaryCalculatedComponentResponse comp : calcResponse.getComponents()) {
                        String code = comp.getComponentCode() != null ? comp.getComponentCode().toUpperCase() : "";
                        if ("PF".equals(code) || "PROVIDENT_FUND".equals(code)) hasPfInDag = true;
                        if ("ESI".equals(code)) hasEsiInDag = true;
                    }
                }

                StatutoryCalculationResult statutoryResult = statutoryEngineService != null ? statutoryEngineService.calculateStatutory(
                        employee.getId(), organizationId, adjustedGross, calcResponse, workingDays, run.getPeriodEnd(), "KA", "NEW"
                ) : null;

                BigDecimal statutoryDeductionsToAdd = BigDecimal.ZERO;

                BigDecimal employeeDeductions = fixedDeductions.add(lopDeduction).add(statutoryDeductionsToAdd);

                // Net Pay = Adjusted Gross + Reimbursement - Non-LOP Employee Deductions
                BigDecimal nonLopDeductions = fixedDeductions.add(statutoryDeductionsToAdd);
                BigDecimal netPay = adjustedGross.add(reimbursementAmount).subtract(nonLopDeductions);
                if (netPay.compareTo(BigDecimal.ZERO) < 0) {
                    netPay = BigDecimal.ZERO;
                }

                PayrollEmployee pe = new PayrollEmployee(
                        organizationId,
                        run.getId(),
                        employee.getId(),
                        employee.getFullName(),
                        employee.getEmployeeId(),
                        adjustedGross,
                        reimbursementAmount,
                        employeeDeductions,
                        netPay,
                        calcResponse.getCurrency(),
                        PayrollEmployeeStatus.CALCULATED,
                        run.getPeriodEnd(),
                        null
                );
                pe = payrollEmployeeRepository.save(pe);

                // 8. Save itemized PayrollItem snapshots
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

                // Variable Earnings / LOP items
                if (lopDeduction.compareTo(BigDecimal.ZERO) > 0) {
                    PayrollItem pi = new PayrollItem(
                            organizationId, pe.getId(), null, "LOP_DEDUCTION", "Loss of Pay Deduction",
                            "DEDUCTION", "DAILY_RATE", lopDeduction, null, "GROSS_SALARY"
                    );
                    payrollItemRepository.save(pi);
                }
                if (encashmentAddition.compareTo(BigDecimal.ZERO) > 0) {
                    PayrollItem pi = new PayrollItem(
                            organizationId, pe.getId(), null, "LEAVE_ENCASHMENT", "Leave Encashment",
                            "EARNING", "DAILY_RATE", encashmentAddition, null, "GROSS_SALARY"
                    );
                    payrollItemRepository.save(pi);
                }
                if (otAmount.compareTo(BigDecimal.ZERO) > 0) {
                    PayrollItem pi = new PayrollItem(
                            organizationId, pe.getId(), null, "OT", "Overtime",
                            "EARNING", "HOURLY_RATE", otAmount, BigDecimal.valueOf(otSummary.getMultiplier()), "BASIC_HOURLY_RATE"
                    );
                    payrollItemRepository.save(pi);
                }
                if (incentiveAmount.compareTo(BigDecimal.ZERO) > 0) {
                    PayrollItem pi = new PayrollItem(
                            organizationId, pe.getId(), null, "INCENTIVE", "Performance Incentive",
                            "EARNING", "APPROVED_AMOUNT", incentiveAmount, null, "APPROVED_INCENTIVE"
                    );
                    payrollItemRepository.save(pi);
                }
                if (bonusAmount.compareTo(BigDecimal.ZERO) > 0) {
                    PayrollItem pi = new PayrollItem(
                            organizationId, pe.getId(), null, "BONUS", "Performance Bonus",
                            "EARNING", "APPROVED_AMOUNT", bonusAmount, null, "APPROVED_BONUS"
                    );
                    payrollItemRepository.save(pi);
                }
                if (reimbursementAmount.compareTo(BigDecimal.ZERO) > 0) {
                    PayrollItem pi = new PayrollItem(
                            organizationId, pe.getId(), null, "REIMBURSEMENT", "Expense Reimbursement",
                            "REIMBURSEMENT", "APPROVED_AMOUNT", reimbursementAmount, null, "APPROVED_EXPENSE"
                    );
                    payrollItemRepository.save(pi);
                }

                // Statutory items (Employee Deductions & Employer Costs)
                // Statutory items (Employer Costs - never reduces net pay)
                if (statutoryResult != null) {
                    if (hasPfInDag && statutoryResult.getEmployerEpfContribution().compareTo(BigDecimal.ZERO) > 0) {
                        PayrollItem pi = new PayrollItem(
                                organizationId, pe.getId(), null, "EMPLOYER_EPF", "Employer EPF Contribution",
                                "EMPLOYER_COST", "PERCENTAGE", statutoryResult.getEmployerEpfContribution(), BigDecimal.valueOf(3.67), "PF_WAGE"
                        );
                        payrollItemRepository.save(pi);
                    }
                    if (hasPfInDag && statutoryResult.getEmployerEpsContribution().compareTo(BigDecimal.ZERO) > 0) {
                        PayrollItem pi = new PayrollItem(
                                organizationId, pe.getId(), null, "EMPLOYER_EPS", "Employer EPS Contribution",
                                "EMPLOYER_COST", "PERCENTAGE", statutoryResult.getEmployerEpsContribution(), BigDecimal.valueOf(8.33), "PF_WAGE"
                        );
                        payrollItemRepository.save(pi);
                    }
                    if (hasEsiInDag && statutoryResult.getEmployerEsiContribution().compareTo(BigDecimal.ZERO) > 0) {
                        PayrollItem pi = new PayrollItem(
                                organizationId, pe.getId(), null, "EMPLOYER_ESI", "Employer ESI Contribution",
                                "EMPLOYER_COST", "PERCENTAGE", statutoryResult.getEmployerEsiContribution(), BigDecimal.valueOf(3.25), "ESI_WAGE"
                        );
                        payrollItemRepository.save(pi);
                    }
                }

                // 9. Consume upstream records for idempotency
                if (encashmentDays > 0) {
                    leaveService.markEncashmentsAsProcessed(employee.getId(), run.getPeriodStart(), run.getPeriodEnd());
                }
                if (incentiveAmount.compareTo(BigDecimal.ZERO) > 0) {
                    incentivePayrollAdapter.markIncentivesProcessed(employee.getId(), organizationId, run.getPeriodStart(), run.getId());
                }
                if (bonusAmount.compareTo(BigDecimal.ZERO) > 0) {
                    bonusPayrollAdapter.markBonusesProcessed(employee.getId(), organizationId, run.getPeriodStart(), run.getId());
                }
                if (reimbursementAmount.compareTo(BigDecimal.ZERO) > 0 && !reimbursementSummary.getExpenseIds().isEmpty()) {
                    reimbursementPayrollAdapter.markReimbursementsProcessed(reimbursementSummary.getExpenseIds(), run.getId());
                }

                processedEmployees++;
                totalGross = totalGross.add(adjustedGross);
                totalBenefits = totalBenefits.add(reimbursementAmount);
                totalDeductions = totalDeductions.add(employeeDeductions);
                totalNet = totalNet.add(netPay);

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

    public boolean isStatusEditable(PayrollRunStatus status) {
        return status == PayrollRunStatus.DRAFT ||
               status == PayrollRunStatus.CALCULATING ||
               status == PayrollRunStatus.PROCESSING ||
               status == PayrollRunStatus.CALCULATED ||
               status == PayrollRunStatus.REJECTED ||
               status == PayrollRunStatus.CHANGES_REQUESTED ||
               status == PayrollRunStatus.FAILED;
    }

    public PayrollRunResponse submitForApproval(Long runId) {
        Long organizationId = TenantContext.requireOrganizationId();

        PayrollRun run = payrollRunRepository.findByIdAndOrganizationId(runId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Payroll run not found with id: " + runId));

        if (run.getStatus() != PayrollRunStatus.CALCULATED &&
            run.getStatus() != PayrollRunStatus.CHANGES_REQUESTED &&
            run.getStatus() != PayrollRunStatus.REJECTED) {
            throw new ConflictException("Only CALCULATED, REJECTED, or CHANGES_REQUESTED payroll runs can be submitted for approval. Current status: " + run.getStatus());
        }

        long employeeCount = payrollEmployeeRepository.countByPayrollRunIdAndOrganizationId(runId, organizationId);
        if (employeeCount == 0) {
            throw new ConflictException("Cannot submit an empty payroll run for approval. Please process the run first.");
        }

        ApprovalContext context = new ApprovalContext();
        context.setModule("PAYROLL");
        context.setResourceId(runId.toString());
        context.setAmount(run.getTotalNet());
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("totalGross", run.getTotalGross());
        metadata.put("totalDeductions", run.getTotalDeductions());
        metadata.put("totalNet", run.getTotalNet());
        metadata.put("totalEmployees", run.getTotalEmployees());
        metadata.put("periodStart", run.getPeriodStart().toString());
        metadata.put("periodEnd", run.getPeriodEnd().toString());
        context.setMetadata(metadata);

        if (approvalFacade != null) {
            try {
                ApprovalWorkflowInstance instance = approvalFacade.startApproval(context);
                if (instance != null) {
                    run.setApprovalInstanceId(instance.getWorkflowInstanceId());
                }
            } catch (Exception ex) {
                run.setApprovalInstanceId("WF-PAYROLL-" + runId + "-" + System.currentTimeMillis());
            }
        } else {
            run.setApprovalInstanceId("WF-PAYROLL-" + runId + "-" + System.currentTimeMillis());
        }

        run.setStatus(PayrollRunStatus.PENDING_APPROVAL);
        run = payrollRunRepository.save(run);
        return PayrollRunResponse.fromEntity(run);
    }

    public PayrollRunResponse cancelApproval(Long runId, String reason) {
        Long organizationId = TenantContext.requireOrganizationId();

        PayrollRun run = payrollRunRepository.findByIdAndOrganizationId(runId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Payroll run not found with id: " + runId));

        if (run.getStatus() != PayrollRunStatus.PENDING_APPROVAL) {
            throw new ConflictException("Only PENDING_APPROVAL payroll runs can have their approval cancelled. Current status: " + run.getStatus());
        }

        if (approvalFacade != null) {
            try {
                approvalFacade.cancel(WorkflowType.PAYROLL_APPROVAL, "PAYROLL", runId.toString(), reason != null ? reason : "Approval cancelled by payroll manager");
            } catch (Exception ignored) {}
        }

        run.setStatus(PayrollRunStatus.CALCULATED);
        run.setRejectionReason(null);
        run = payrollRunRepository.save(run);
        return PayrollRunResponse.fromEntity(run);
    }

    public PayrollRunResponse lockPayrollRun(Long runId) {
        Long organizationId = TenantContext.requireOrganizationId();

        PayrollRun run = payrollRunRepository.findByIdAndOrganizationId(runId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Payroll run not found with id: " + runId));

        if (run.getStatus() != PayrollRunStatus.APPROVED && run.getStatus() != PayrollRunStatus.CALCULATED) {
            throw new ConflictException("Payroll run must be APPROVED before locking. Current status: " + run.getStatus());
        }

        run.setStatus(PayrollRunStatus.LOCKED);
        run.setFinalizedAt(LocalDateTime.now());
        run = payrollRunRepository.save(run);
        return PayrollRunResponse.fromEntity(run);
    }

    public PayrollRunResponse finalizePayrollRun(Long runId) {
        Long organizationId = TenantContext.requireOrganizationId();

        PayrollRun run = payrollRunRepository.findByIdAndOrganizationId(runId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Payroll run not found with id: " + runId));

        if (run.getStatus() != PayrollRunStatus.CALCULATED &&
            run.getStatus() != PayrollRunStatus.APPROVED &&
            run.getStatus() != PayrollRunStatus.LOCKED) {
            throw new BadRequestException("Only CALCULATED, APPROVED, or LOCKED payroll runs can be finalized. Current status: " + run.getStatus());
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
    public PayrollRunResponse getPayrollRunSummary(Long runId) {
        return getPayrollRun(runId);
    }

    @Transactional(readOnly = true)
    public com.example.ems.payroll.dto.PayrollRunStatusResponse getPayrollRunStatus(Long runId) {
        Long organizationId = TenantContext.requireOrganizationId();
        PayrollRun run = payrollRunRepository.findByIdAndOrganizationId(runId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Payroll run not found with id: " + runId));
        return com.example.ems.payroll.dto.PayrollRunStatusResponse.fromEntity(run);
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
