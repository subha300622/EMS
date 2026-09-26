package com.example.ems.offboarding.service;

import com.example.ems.approval.entity.ApprovalTask;
import com.example.ems.approval.entity.ApprovalWorkflowInstance;
import com.example.ems.approval.entity.WorkflowType;
import com.example.ems.approval.repository.ApprovalTaskRepository;
import com.example.ems.approval.repository.ApprovalWorkflowInstanceRepository;
import com.example.ems.approval.service.ApprovalWorkflowEngineService;
import com.example.ems.auth.entity.User;
import com.example.ems.common.exception.ConflictException;
import com.example.ems.common.exception.ResourceNotFoundException;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.employee.service.EmployeeService;
import com.example.ems.offboarding.dto.*;
import com.example.ems.offboarding.entity.EmployeeExit;
import com.example.ems.offboarding.entity.ExitFnfAudit;
import com.example.ems.offboarding.entity.FnfSettlement;
import com.example.ems.offboarding.repository.EmployeeExitRepository;
import com.example.ems.offboarding.repository.ExitFnfAuditRepository;
import com.example.ems.offboarding.repository.ExitFnfSettlementRepository;

import com.example.ems.security.context.TenantContext;
import com.example.ems.security.rls.PostgresRlsSessionBinder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.*;

@Service
public class FnfSettlementService {

    private static final Logger log = LoggerFactory.getLogger(FnfSettlementService.class);

    @Autowired
    private ExitFnfSettlementRepository fnfRepository;

    @Autowired
    private EmployeeExitRepository exitRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private ExitFnfAuditRepository auditRepository;

   
    @Autowired
    private FnfSnapshotService snapshotService;

    @Autowired
    private FnfReadinessService readinessService;

    @Autowired
    private ApprovalWorkflowEngineService approvalWorkflowEngineService;

    @Autowired
    private ApprovalWorkflowInstanceRepository instanceRepository;

    @Autowired
    private ApprovalTaskRepository taskRepository;

    @Autowired
    private ExitAuthorizationService exitAuthService;

    @Autowired(required = false)
    private PostgresRlsSessionBinder rlsSessionBinder;

    @Autowired
    private ObjectMapper objectMapper;

    @Transactional
    public FnfCalculationResponse calculateSettlement(User currentUser, Long exitId, FnfCalculationRequest request) {
        Long orgId = TenantContext.requireOrganizationId();
        bindRlsIfAvailable();

        EmployeeExit exit = exitRepository.findByIdAndOrganizationId(exitId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Exit record not found with ID: " + exitId));

        exitAuthService.assertCanCalculateFnf(currentUser, exit);

        if ("REJECTED".equalsIgnoreCase(exit.getStatus())) {
            throw new IllegalStateException("Cannot calculate F&F settlement: Exit request has been rejected.");
        }
        if ("FINALIZED".equalsIgnoreCase(exit.getStatus()) || "SETTLEMENT_COMPLETED".equalsIgnoreCase(exit.getStatus())) {
            throw new IllegalStateException("Cannot calculate F&F settlement: Exit has already finalized settlement.");
        }
        if (!"FNF_CALCULATION_PENDING".equalsIgnoreCase(exit.getStatus())) {
            throw new IllegalStateException("Cannot calculate F&F settlement: Exit request is in status '"
                    + exit.getStatus()
                    + "'. Calculation is only permitted when clearances are complete and status is FNF_CALCULATION_PENDING.");
        }

        FnfSettlement settlement = fnfRepository.findByExitIdAndOrganizationId(exitId, orgId)
                .orElseGet(() -> {
                    FnfSettlement s = new FnfSettlement();
                    s.setOrganization(exit.getOrganization());
                    s.setExit(exit);
                    s.setStatus("DRAFT");
                    return s;
                });

        assertEditableState(settlement, "calculate");

        return applyCalculation(currentUser, exit, settlement, request, "CALCULATE", "FINANCE_APPROVAL_PENDING", true, true);
    }

    @Transactional
    public FnfCalculationResponse recalculateSettlement(User currentUser, Long fnfId, FnfCalculationRequest request) {
        Long orgId = TenantContext.requireOrganizationId();
        bindRlsIfAvailable();

        FnfSettlement settlement = fnfRepository.findByIdAndOrganizationId(fnfId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("F&F Settlement not found with ID: " + fnfId));

        exitAuthService.assertCanCalculateFnf(currentUser, settlement.getExit());
        assertEditableState(settlement, "recalculate");

        return applyCalculation(currentUser, settlement.getExit(), settlement, request, "RECALCULATE", null, false, false);
    }

    @Transactional
    public FnfCalculationResponse updateSettlement(User currentUser, Long fnfId, FnfCalculationRequest request) {
        Long orgId = TenantContext.requireOrganizationId();
        bindRlsIfAvailable();

        FnfSettlement settlement = fnfRepository.findByIdAndOrganizationId(fnfId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("F&F Settlement not found with ID: " + fnfId));

        exitAuthService.assertCanCalculateFnf(currentUser, settlement.getExit());
        assertEditableState(settlement, "update");

        return applyCalculation(currentUser, settlement.getExit(), settlement, request, "UPDATE", null, false, false);
    }

    private FnfCalculationResponse applyCalculation(
            User currentUser,
            EmployeeExit exit,
            FnfSettlement settlement,
            FnfCalculationRequest request,
            String action,
            String targetStatus,
            boolean updateExitStatus,
            boolean startApprovalWorkflow) {

        Employee employee = exit.getEmployee();
        if (employee.getAnnualSalary() == null || employee.getAnnualSalary().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Employee annual salary is not configured or is zero. Cannot calculate F&F settlement without valid compensation.");
        }
        BigDecimal monthlySalary = employee.getAnnualSalary().divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);

        LocalDate lwd = exit.getLastWorkingDate() != null ? exit.getLastWorkingDate()
                : (exit.getRequestedLastWorkingDate() != null ? exit.getRequestedLastWorkingDate() : LocalDate.now());
        YearMonth ym = YearMonth.from(lwd);
        int daysInMonth = ym.lengthOfMonth();

        int workedDays = request.getSalaryDaysWorked();
        if (workedDays < 0) {
            throw new IllegalArgumentException("Salary days worked cannot be negative.");
        }
        if (workedDays > daysInMonth) {
            throw new IllegalArgumentException("Salary days worked (" + workedDays + ") cannot exceed total days in the exit month ("
                    + daysInMonth + " days in " + ym.getMonth() + " " + ym.getYear() + ").");
        }

        BigDecimal salaryAmount = BigDecimal.ZERO;
        if (workedDays > 0) {
            BigDecimal dailyRate = monthlySalary.divide(BigDecimal.valueOf(daysInMonth), 4, RoundingMode.HALF_UP);
            salaryAmount = dailyRate.multiply(BigDecimal.valueOf(workedDays)).setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal totalEarnings = salaryAmount
                .add(request.getUnpaidSalary() != null ? request.getUnpaidSalary() : BigDecimal.ZERO)
                .add(request.getLeaveEncashment() != null ? request.getLeaveEncashment() : BigDecimal.ZERO)
                .add(request.getBonus() != null ? request.getBonus() : BigDecimal.ZERO)
                .add(request.getIncentives() != null ? request.getIncentives() : BigDecimal.ZERO)
                .add(request.getOvertime() != null ? request.getOvertime() : BigDecimal.ZERO)
                .add(request.getReimbursements() != null ? request.getReimbursements() : BigDecimal.ZERO)
                .add(request.getGratuity() != null ? request.getGratuity() : BigDecimal.ZERO)
                .add(request.getOtherAllowances() != null ? request.getOtherAllowances() : BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal loanRecovery = request.getLoanRecovery() != null ? request.getLoanRecovery() : BigDecimal.ZERO;
        BigDecimal totalDeductions = (request.getNoticePeriodRecovery() != null ? request.getNoticePeriodRecovery() : BigDecimal.ZERO)
                .add(request.getAssetDamage() != null ? request.getAssetDamage() : BigDecimal.ZERO)
                .add(loanRecovery)
                .add(request.getTaxDeduction() != null ? request.getTaxDeduction() : BigDecimal.ZERO)
                .add(request.getOtherDeductions() != null ? request.getOtherDeductions() : BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal netSettlement = totalEarnings.subtract(totalDeductions).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);

        BigDecimal previousNet = settlement.getNetSettlement();

        settlement.setSalaryDaysWorked(workedDays);
        settlement.setSalaryAmount(salaryAmount);
        settlement.setUnpaidSalary(request.getUnpaidSalary() != null ? request.getUnpaidSalary() : BigDecimal.ZERO);
        settlement.setLeaveEncashment(request.getLeaveEncashment() != null ? request.getLeaveEncashment() : BigDecimal.ZERO);
        settlement.setBonus(request.getBonus() != null ? request.getBonus() : BigDecimal.ZERO);
        settlement.setIncentives(request.getIncentives() != null ? request.getIncentives() : BigDecimal.ZERO);
        settlement.setOvertime(request.getOvertime() != null ? request.getOvertime() : BigDecimal.ZERO);
        settlement.setReimbursements(request.getReimbursements() != null ? request.getReimbursements() : BigDecimal.ZERO);
        settlement.setGratuity(request.getGratuity() != null ? request.getGratuity() : BigDecimal.ZERO);
        settlement.setOtherAllowances(request.getOtherAllowances() != null ? request.getOtherAllowances() : BigDecimal.ZERO);
        settlement.setTotalEarnings(totalEarnings);

        settlement.setNoticePeriodRecovery(request.getNoticePeriodRecovery() != null ? request.getNoticePeriodRecovery() : BigDecimal.ZERO);
        settlement.setAssetDamage(request.getAssetDamage() != null ? request.getAssetDamage() : BigDecimal.ZERO);
        settlement.setLoanRecovery(loanRecovery);
        settlement.setTaxDeduction(request.getTaxDeduction() != null ? request.getTaxDeduction() : BigDecimal.ZERO);
        settlement.setOtherDeductions(request.getOtherDeductions() != null ? request.getOtherDeductions() : BigDecimal.ZERO);
        settlement.setTotalDeductions(totalDeductions);

        settlement.setNetSettlement(netSettlement);
        if (targetStatus != null) {
            settlement.setStatus(targetStatus);
        }
        settlement.setUpdatedAt(LocalDateTime.now());

        if (snapshotService != null) {
            try {
                FnfSnapshotDto snapshotDto = snapshotService.captureSnapshot(exit, settlement);
                String snapshotJson = snapshotService.serializeToJson(snapshotDto);
                String snapshotHash = snapshotService.computeCanonicalHash(snapshotDto);

                settlement.setSnapshotData(snapshotJson);
                settlement.setSnapshotHash(snapshotHash);
                settlement.setSnapshotVersion(settlement.getSnapshotVersion() == null ? 1 : settlement.getSnapshotVersion() + 1);
                settlement.setSnapshotCreatedAt(LocalDateTime.now());
            } catch (Exception e) {
                log.warn("Snapshot capture notice: {}", e.getMessage());
            }
        }

        FnfSettlement saved = fnfRepository.save(settlement);

        if (updateExitStatus && targetStatus != null) {
            exit.setStatus(targetStatus);
            exit.setUpdatedAt(LocalDateTime.now());
            exitRepository.save(exit);
        }

        recordAuditSnapshot(saved, action, currentUser, previousNet, netSettlement, "F&F calculation " + action.toLowerCase());

        if (startApprovalWorkflow && approvalWorkflowEngineService != null) {
            try {
                approvalWorkflowEngineService.startWorkflow(
                        WorkflowType.FNF_APPROVAL,
                        "FNF_SETTLEMENT",
                        saved.getId().toString(),
                        employee,
                        null);
            } catch (Exception e) {
                log.warn("Approval workflow initiation notice: {}", e.getMessage());
            }
        }

        return mapToResponse(saved);
    }

    @Transactional
    public FnfCalculationResponse submitSettlement(User currentUser, Long fnfId) {
        Long orgId = TenantContext.requireOrganizationId();
        bindRlsIfAvailable();

        FnfSettlement settlement = fnfRepository.findByIdAndOrganizationId(fnfId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("F&F Settlement not found with ID: " + fnfId));

        exitAuthService.assertCanCalculateFnf(currentUser, settlement.getExit());

        syncStatusWithWorkflow(settlement);

        if (!"CALCULATED".equalsIgnoreCase(settlement.getStatus()) && !"DRAFT".equalsIgnoreCase(settlement.getStatus())) {
            throw new ConflictException("FNF_IMMUTABLE: Settlement is in status '" + settlement.getStatus()
                    + "' and cannot be submitted. Only CALCULATED settlements can be submitted for approval.");
        }

        EmployeeExit exit = settlement.getExit();

        // 1. Verify clearance and calculation readiness
        FnfReadinessDto clearanceReadiness = readinessService.checkClearanceReadiness(exit);
        if (!clearanceReadiness.isReady()) {
            String blockers = String.join("; ", clearanceReadiness.getBlockingItems().stream().map(FnfBlockingItemDto::getDescription).toList());
            throw new IllegalStateException("Cannot submit settlement: Clearance requirements incomplete. Blockers: " + blockers);
        }

        // 2. Refresh & Lock Snapshot
        if (settlement.getSnapshotHash() == null) {
            FnfSnapshotDto snapshotDto = snapshotService.captureSnapshot(exit, settlement);
            settlement.setSnapshotData(snapshotService.serializeToJson(snapshotDto));
            settlement.setSnapshotHash(snapshotService.computeCanonicalHash(snapshotDto));
            settlement.setSnapshotVersion(settlement.getSnapshotVersion() == null ? 1 : settlement.getSnapshotVersion() + 1);
            settlement.setSnapshotCreatedAt(LocalDateTime.now());
        }

        Employee actor = resolveEmployeeForUser(currentUser, orgId);
        settlement.setStatus("SUBMITTED");
        settlement.setSubmittedAt(LocalDateTime.now());
        settlement.setSubmittedBy(actor);
        settlement.setUpdatedAt(LocalDateTime.now());

        FnfSettlement saved = fnfRepository.saveAndFlush(settlement);

        recordAuditSnapshot(saved, "SUBMIT", currentUser, saved.getNetSettlement(), saved.getNetSettlement(), "Settlement submitted for multi-stage approval");

        // 3. Initiate delegated Approval Workflow Engine
        try {
            approvalWorkflowEngineService.startWorkflow(
                    WorkflowType.FNF_APPROVAL,
                    "FNF_SETTLEMENT",
                    saved.getId().toString(),
                    exit.getEmployee(),
                    null);
            saved.setStatus("FINANCE_APPROVAL_PENDING");
            exit.setStatus("FINANCE_APPROVAL_PENDING");
            exitRepository.save(exit);
            saved = fnfRepository.save(saved);
        } catch (Exception e) {
            log.warn("Approval workflow initiation: {}", e.getMessage());
        }

        return mapToResponse(saved);
    }

    @Transactional
    public FnfCalculationResponse cancelSettlement(User currentUser, Long fnfId, String reason) {
        Long orgId = TenantContext.requireOrganizationId();
        bindRlsIfAvailable();

        FnfSettlement settlement = fnfRepository.findByIdAndOrganizationId(fnfId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("F&F Settlement not found with ID: " + fnfId));

        exitAuthService.assertCanCalculateFnf(currentUser, settlement.getExit());

        String status = settlement.getStatus();
        if ("FINALIZED".equalsIgnoreCase(status) || "PAYMENT_RELEASED".equalsIgnoreCase(status) || "CANCELLED".equalsIgnoreCase(status)) {
            throw new IllegalStateException("Cannot cancel settlement in terminal or released status: " + status);
        }

        settlement.setStatus("CANCELLED");
        settlement.setUpdatedAt(LocalDateTime.now());
        FnfSettlement saved = fnfRepository.save(settlement);

        recordAuditSnapshot(saved, "CANCEL", currentUser, saved.getNetSettlement(), BigDecimal.ZERO, reason != null ? reason : "Settlement cancelled");

        return mapToResponse(saved);
    }

    @Transactional
    public FnfCalculationResponse getSettlementById(User currentUser, Long fnfId) {
        Long orgId = TenantContext.requireOrganizationId();
        bindRlsIfAvailable();

        FnfSettlement settlement = fnfRepository.findByIdAndOrganizationId(fnfId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("F&F Settlement not found with ID: " + fnfId));

        exitAuthService.assertCanViewFnf(currentUser, settlement);
        syncStatusWithWorkflow(settlement);

        return mapToResponse(settlement);
    }

    @Transactional
    public FnfCalculationResponse getSettlementByExitId(User currentUser, Long exitId) {
        Long orgId = TenantContext.requireOrganizationId();
        bindRlsIfAvailable();

        FnfSettlement settlement = fnfRepository.findByExitIdAndOrganizationId(exitId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("F&F Settlement not found for exit ID: " + exitId));

        exitAuthService.assertCanViewFnf(currentUser, settlement);
        syncStatusWithWorkflow(settlement);

        return mapToResponse(settlement);
    }

    @Transactional
    public List<FnfCalculationResponse> getSettlementsForEmployee(User currentUser, Long employeeId) {
        Long orgId = TenantContext.requireOrganizationId();
        bindRlsIfAvailable();

        List<FnfSettlement> list = fnfRepository.findByEmployeeId(employeeId, orgId);
        List<FnfCalculationResponse> result = new ArrayList<>();
        for (FnfSettlement s : list) {
            exitAuthService.assertCanViewFnf(currentUser, s);
            syncStatusWithWorkflow(s);
            result.add(mapToResponse(s));
        }
        return result;
    }

    @Transactional
    public Map<String, Object> getSettlementStatus(User currentUser, Long fnfId) {
        Long orgId = TenantContext.requireOrganizationId();
        bindRlsIfAvailable();

        FnfSettlement settlement = fnfRepository.findByIdAndOrganizationId(fnfId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("F&F Settlement not found with ID: " + fnfId));

        exitAuthService.assertCanViewFnf(currentUser, settlement);
        syncStatusWithWorkflow(settlement);

        Map<String, Object> statusMap = new LinkedHashMap<>();
        statusMap.put("fnfId", settlement.getId());
        statusMap.put("exitId", settlement.getExit().getId());
        statusMap.put("status", settlement.getStatus());
        statusMap.put("netSettlement", settlement.getNetSettlement());
        statusMap.put("submittedAt", settlement.getSubmittedAt());
        statusMap.put("submittedById", settlement.getSubmittedById());
        statusMap.put("finalizedAt", settlement.getFinalizedAt());
        statusMap.put("finalizedById", settlement.getFinalizedById());
        statusMap.put("snapshotVersion", settlement.getSnapshotVersion());
        statusMap.put("snapshotHash", settlement.getSnapshotHash());

        List<String> allowedActions = new ArrayList<>();
        String st = settlement.getStatus();
        if ("DRAFT".equalsIgnoreCase(st) || "CALCULATED".equalsIgnoreCase(st)) {
            allowedActions.add("UPDATE");
            allowedActions.add("RECALCULATE");
            allowedActions.add("SUBMIT");
            allowedActions.add("CANCEL");
        } else if (st != null && st.contains("APPROVAL_PENDING")) {
            allowedActions.add("APPROVE");
            allowedActions.add("REJECT");
            allowedActions.add("CANCEL");
        } else if ("SETTLEMENT_APPROVED".equalsIgnoreCase(st) || "PAYMENT_PENDING".equalsIgnoreCase(st)) {
            allowedActions.add("PAYMENT_VALIDATE");
            allowedActions.add("PAYMENT");
            allowedActions.add("CANCEL");
        } else if ("PAYMENT_FAILED".equalsIgnoreCase(st)) {
            allowedActions.add("PAYMENT_RETRY");
        } else if ("PAYMENT_RELEASED".equalsIgnoreCase(st)) {
            allowedActions.add("FINALIZE");
            allowedActions.add("DOCUMENTS");
        } else if ("FINALIZED".equalsIgnoreCase(st)) {
            allowedActions.add("DOCUMENTS");
        }
        statusMap.put("allowedActions", allowedActions);

        return statusMap;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getSnapshotDetails(User currentUser, Long fnfId) {
        Long orgId = TenantContext.requireOrganizationId();
        bindRlsIfAvailable();

        FnfSettlement settlement = fnfRepository.findByIdAndOrganizationId(fnfId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("F&F Settlement not found with ID: " + fnfId));

        exitAuthService.assertCanViewFnf(currentUser, settlement);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("fnfId", settlement.getId());
        response.put("snapshotVersion", settlement.getSnapshotVersion());
        response.put("snapshotHash", settlement.getSnapshotHash());
        response.put("snapshotCreatedAt", settlement.getSnapshotCreatedAt());

        FnfSnapshotDto storedSnapshot = snapshotService.deserializeFromJson(settlement.getSnapshotData());
        response.put("snapshotData", storedSnapshot);

        boolean integrityValid = snapshotService.verifyIntegrity(settlement.getSnapshotData(), settlement.getSnapshotHash());
        response.put("hashIntegrityValid", integrityValid);

        return response;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> refreshInputsComparison(User currentUser, Long fnfId) {
        Long orgId = TenantContext.requireOrganizationId();
        bindRlsIfAvailable();

        FnfSettlement settlement = fnfRepository.findByIdAndOrganizationId(fnfId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("F&F Settlement not found with ID: " + fnfId));

        exitAuthService.assertCanViewFnf(currentUser, settlement);

        FnfSnapshotDto stored = snapshotService.deserializeFromJson(settlement.getSnapshotData());
        FnfSnapshotDto live = snapshotService.captureSnapshot(settlement.getExit(), settlement);

        String storedHash = settlement.getSnapshotHash();
        String liveHash = snapshotService.computeCanonicalHash(live);

        boolean differs = !Objects.equals(storedHash, liveHash);

        Map<String, Object> comparison = new LinkedHashMap<>();
        comparison.put("fnfId", settlement.getId());
        comparison.put("storedHash", storedHash);
        comparison.put("liveHash", liveHash);
        comparison.put("inputsModified", differs);
        comparison.put("storedSnapshot", stored);
        comparison.put("liveSnapshot", live);
        comparison.put("message", differs ? "Live dependencies have changed since submission." : "Snapshot matches live dependencies.");

        return comparison;
    }

    @Transactional(readOnly = true)
    public FnfReadinessDto checkApprovalEligibility(User currentUser, Long fnfId) {
        Long orgId = TenantContext.requireOrganizationId();
        bindRlsIfAvailable();

        FnfSettlement settlement = fnfRepository.findByIdAndOrganizationId(fnfId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("F&F Settlement not found with ID: " + fnfId));

        exitAuthService.assertCanViewFnf(currentUser, settlement);

        return readinessService.checkClearanceReadiness(settlement);
    }

    @Transactional(readOnly = true)
    public FnfReadinessDto getClearanceStatus(User currentUser, Long fnfId) {
        Long orgId = TenantContext.requireOrganizationId();
        bindRlsIfAvailable();

        FnfSettlement settlement = fnfRepository.findByIdAndOrganizationId(fnfId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("F&F Settlement not found with ID: " + fnfId));

        exitAuthService.assertCanViewFnf(currentUser, settlement);

        return readinessService.checkClearanceReadiness(settlement);
    }

    @Transactional(readOnly = true)
    public List<FnfBlockingItemDto> getBlockingItems(User currentUser, Long fnfId) {
        Long orgId = TenantContext.requireOrganizationId();
        bindRlsIfAvailable();

        FnfSettlement settlement = fnfRepository.findByIdAndOrganizationId(fnfId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("F&F Settlement not found with ID: " + fnfId));

        exitAuthService.assertCanViewFnf(currentUser, settlement);

        List<FnfBlockingItemDto> blockers = new ArrayList<>();
        blockers.addAll(readinessService.checkClearanceReadiness(settlement).getBlockingItems());
        blockers.addAll(readinessService.checkPaymentReadiness(settlement).getBlockingItems());
        return blockers;
    }

    @Transactional
    public FnfApprovalsResponse getApprovalsForSettlement(User currentUser, Long fnfId) {
        Long orgId = TenantContext.requireOrganizationId();
        bindRlsIfAvailable();

        FnfSettlement settlement = fnfRepository.findByIdAndOrganizationId(fnfId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("F&F Settlement not found with ID: " + fnfId));

        exitAuthService.assertCanViewFnf(currentUser, settlement);

        syncStatusWithWorkflow(settlement);

        Optional<ApprovalWorkflowInstance> instanceOpt = instanceRepository
                .findByBusinessReferenceTypeAndBusinessReferenceIdAndOrganizationId(
                        "FNF_SETTLEMENT", fnfId.toString(), orgId);

        List<FnfApprovalStageDto> stages = new ArrayList<>();
        String workflowStatus = settlement.getStatus();
        int currentStep = 1;

        if (instanceOpt.isPresent()) {
            ApprovalWorkflowInstance instance = instanceOpt.get();
            workflowStatus = instance.getStatus().name();
            currentStep = instance.getCurrentStep();

            List<ApprovalTask> tasks = taskRepository.findByWorkflowInstanceId(instance.getId());
            for (ApprovalTask t : tasks) {
                String roleName = t.getStep() != null && t.getStep().getApproverConfig() != null
                        ? t.getStep().getApproverConfig()
                        : (t.getStepOrder() == 1 ? "FINANCE_MANAGER"
                        : (t.getStepOrder() == 2 ? "HR_MANAGER" : "COMPANY_ADMIN"));

                LocalDateTime actedAt = t.getCompletedAt() != null
                        ? LocalDateTime.ofInstant(t.getCompletedAt(), ZoneId.systemDefault())
                        : null;

                stages.add(new FnfApprovalStageDto(
                        t.getStepOrder(),
                        roleName,
                        t.getStep() != null ? t.getStep().getStepName() : "Stage " + t.getStepOrder(),
                        t.getApprover() != null ? t.getApprover().getId() : null,
                        t.getApprover() != null ? t.getApprover().getFullName() : "Unassigned",
                        t.getStatus().name(),
                        null,
                        actedAt));
            }
        } else {
            stages.add(new FnfApprovalStageDto(1, "FINANCE_MANAGER", "Finance Manager Approval", null, "Finance Dept", "PENDING", null, null));
            stages.add(new FnfApprovalStageDto(2, "HR_MANAGER", "HR Manager Approval", null, "HR Dept", "PENDING", null, null));
            stages.add(new FnfApprovalStageDto(3, "COMPANY_ADMIN", "Company Admin Approval", null, "Admin Dept", "PENDING", null, null));
        }

        return new FnfApprovalsResponse(settlement.getId(), settlement.getExit().getId(), workflowStatus, currentStep, stages);
    }

    @Transactional
    public FnfReadinessDto validatePaymentReadiness(User currentUser, Long fnfId) {
        Long orgId = TenantContext.requireOrganizationId();
        bindRlsIfAvailable();

        FnfSettlement settlement = fnfRepository.findByIdAndOrganizationId(fnfId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("F&F Settlement not found with ID: " + fnfId));

        exitAuthService.assertCanViewFnf(currentUser, settlement);
        syncStatusWithWorkflow(settlement);

        FnfReadinessDto readiness = readinessService.checkPaymentReadiness(settlement);
        if (!readiness.isReady()) {
            throw new ConflictException("PAYMENT_NOT_READY: " + String.join("; ", readiness.getBlockingItems().stream().map(FnfBlockingItemDto::getDescription).toList()));
        }
        return readiness;
    }

    @Transactional
    public FnfPaymentResponse processPayment(User currentUser, Long fnfId, FnfPaymentRequest request) {
        Long orgId = TenantContext.requireOrganizationId();
        bindRlsIfAvailable();

        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment amount must be positive");
        }

        if (request.getIdempotencyKey() == null || request.getIdempotencyKey().isBlank()) {
            throw new IllegalArgumentException("Idempotency-Key is required for payment disbursement.");
        }
        String trimmedKey = request.getIdempotencyKey().trim();
        if (trimmedKey.length() < 4 || trimmedKey.length() > 128
                || !trimmedKey.matches("^[A-Za-z0-9_\\-\\.:]{4,128}$")) {
            throw new IllegalArgumentException("Invalid Idempotency-Key. Key must be between 4 and 128 characters.");
        }
        request.setIdempotencyKey(trimmedKey);

        // 1. Acquire Pessimistic Row Lock (SELECT FOR UPDATE)
        FnfSettlement settlement = fnfRepository.findWithLockByIdAndOrganizationId(fnfId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("F&F Settlement not found with ID: " + fnfId));

        exitAuthService.assertCanReleasePayment(currentUser, settlement);
        syncStatusWithWorkflow(settlement);

        // 2. Terminal & Released State Checking + Strict Idempotency Conflict Guard
        if ("FINALIZED".equalsIgnoreCase(settlement.getStatus())) {
            throw new ConflictException("FNF_IMMUTABLE: Settlement ID " + fnfId + " is FINALIZED. Payment cannot be modified or re-executed.");
        }

        if ("PAYMENT_RELEASED".equalsIgnoreCase(settlement.getStatus())) {
            boolean keyMatch = trimmedKey.equals(settlement.getIdempotencyKey());
            boolean refMatch = request.getTransactionReference().equals(settlement.getPaymentReference());

            if (keyMatch || refMatch) {
                boolean amountMatch = request.getAmount().compareTo(settlement.getPaidAmount()) == 0;
                boolean exactRefMatch = request.getTransactionReference().equals(settlement.getPaymentReference());

                if (amountMatch && exactRefMatch) {
                    log.info("Idempotent payment replay verified for settlement ID {}. Returning existing receipt.", fnfId);
                    return mapToPaymentResponse(settlement);
                } else {
                    throw new IllegalStateException("IDEMPOTENCY_KEY_REUSED: Payment idempotency conflict: Same idempotency key '"
                            + trimmedKey + "' was used with differing amount (" + request.getAmount() + " vs " + settlement.getPaidAmount()
                            + ") or reference ('" + request.getTransactionReference() + "' vs '" + settlement.getPaymentReference() + "').");
                }
            } else {
                throw new IllegalStateException("PAYMENT_ALREADY_RELEASED: Payment has already been released for settlement ID: " + fnfId);
            }
        }

        // 3. Organization-Wide Idempotency Conflict Protection
        Optional<FnfSettlement> existingKeyRecord = fnfRepository.findByOrganizationIdAndIdempotencyKey(orgId, trimmedKey);
        if (existingKeyRecord.isPresent() && !existingKeyRecord.get().getId().equals(fnfId)) {
            throw new IllegalStateException("IDEMPOTENCY_KEY_REUSED: Idempotency key '" + trimmedKey + "' has already been used on another settlement.");
        }

        // 4. Strict State Gate: Must be PAYMENT_PENDING or SETTLEMENT_APPROVED
        String status = settlement.getStatus();
        if ("REJECTED".equalsIgnoreCase(status)) {
            throw new IllegalStateException("Cannot release payment: Settlement has been rejected.");
        }
        if (!"PAYMENT_PENDING".equalsIgnoreCase(status) && !"SETTLEMENT_APPROVED".equalsIgnoreCase(status)) {
            throw new IllegalStateException("Cannot release payment: Settlement is in status '" + status
                    + "'. All 3 approval stages (Finance, HR, Admin) must be approved before payment can be disbursed.");
        }

        // 5. Exact Amount Match Verification
        if (request.getAmount() == null || request.getAmount().compareTo(settlement.getNetSettlement()) != 0) {
            throw new IllegalArgumentException("Payment amount (" + request.getAmount()
                    + ") does not match calculated net settlement amount (" + settlement.getNetSettlement() + ")");
        }

        // 6. Readiness & Blocking Item Enforcement
        if (readinessService != null) {
            FnfReadinessDto readiness = readinessService.checkPaymentReadiness(settlement);
            if (readiness != null && !readiness.isReady()) {
                throw new IllegalStateException("Cannot release payment: " + String.join("; ", readiness.getBlockingItems().stream().map(FnfBlockingItemDto::getDescription).toList()));
            }
        }

        // 7. Transaction Reference Uniqueness
        Optional<FnfSettlement> existingRef = fnfRepository.findByOrganizationIdAndPaymentReference(orgId, request.getTransactionReference());
        if (existingRef.isPresent() && !existingRef.get().getId().equals(fnfId)) {
            throw new IllegalStateException("Payment reference '" + request.getTransactionReference() + "' has already been used in organization.");
        }

        Employee paidBy = resolveEmployeeForUser(currentUser, orgId);

        settlement.setPaymentMethod(request.getPaymentMethod().toUpperCase());
        settlement.setPaymentDate(request.getPaymentDate());
        settlement.setPaymentReference(request.getTransactionReference());
        settlement.setIdempotencyKey(trimmedKey);
        settlement.setPaymentRemarks(request.getRemarks());
        settlement.setPaidAmount(request.getAmount());
        settlement.setPaidAt(LocalDateTime.now());
        settlement.setPaidBy(paidBy);
        settlement.setPaymentAttempts(settlement.getPaymentAttempts() == null ? 1 : settlement.getPaymentAttempts() + 1);
        settlement.setStatus("PAYMENT_RELEASED");
        settlement.setUpdatedAt(LocalDateTime.now());

        FnfSettlement saved;
        try {
            saved = fnfRepository.saveAndFlush(settlement);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Duplicate payment constraint violation: A disbursement with this reference or idempotency key was already recorded.");
        }

        // Multi-Entity Transition: Exit -> SETTLEMENT_COMPLETED
        EmployeeExit exit = settlement.getExit();
        exit.setStatus("SETTLEMENT_COMPLETED");
        exit.setUpdatedAt(LocalDateTime.now());
        exitRepository.save(exit);

        // Multi-Entity Transition: Employee Lifecycle Termination Boundary
        Employee employee = exit.getEmployee();
        employeeService.terminateEmployee(
                employee.getId(),
                "Full and final settlement payment disbursed"
        );

        recordAuditSnapshot(saved, "PAYMENT", currentUser, settlement.getNetSettlement(), request.getAmount(),
                "Payment disbursed via " + request.getPaymentMethod() + " with ref: " + request.getTransactionReference());

        return mapToPaymentResponse(saved);
    }

    @Transactional
    public FnfPaymentResponse retryPayment(User currentUser, Long fnfId, FnfPaymentRequest request) {
        Long orgId = TenantContext.requireOrganizationId();
        bindRlsIfAvailable();

        FnfSettlement settlement = fnfRepository.findWithLockByIdAndOrganizationId(fnfId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("F&F Settlement not found with ID: " + fnfId));

        exitAuthService.assertCanReleasePayment(currentUser, settlement);

        if ("PAYMENT_RELEASED".equalsIgnoreCase(settlement.getStatus())) {
            throw new ConflictException("PAYMENT_ALREADY_RELEASED: Cannot retry payment. Payment has already been released successfully.");
        }
        if ("FINALIZED".equalsIgnoreCase(settlement.getStatus())) {
            throw new ConflictException("FNF_IMMUTABLE: Settlement is finalized.");
        }
        if (!"PAYMENT_FAILED".equalsIgnoreCase(settlement.getStatus())) {
            throw new IllegalStateException("Payment retry is only permitted when settlement is in status 'PAYMENT_FAILED'. Current status: " + settlement.getStatus());
        }

        settlement.setStatus("PAYMENT_PENDING");
        settlement.setUpdatedAt(LocalDateTime.now());
        fnfRepository.save(settlement);

        return processPayment(currentUser, fnfId, request);
    }

    @Transactional
    public Map<String, Object> finalizeSettlement(User currentUser, Long fnfId) {
        Long orgId = TenantContext.requireOrganizationId();
        bindRlsIfAvailable();

        FnfSettlement settlement = fnfRepository.findWithLockByIdAndOrganizationId(fnfId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("F&F Settlement not found with ID: " + fnfId));

        exitAuthService.assertCanReleasePayment(currentUser, settlement);

        if ("FINALIZED".equalsIgnoreCase(settlement.getStatus())) {
            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("fnfId", settlement.getId());
            resp.put("status", "FINALIZED");
            resp.put("finalizedAt", settlement.getFinalizedAt());
            resp.put("finalizedById", settlement.getFinalizedById());
            resp.put("message", "Settlement is already finalized.");
            return resp;
        }

        if (!"PAYMENT_RELEASED".equalsIgnoreCase(settlement.getStatus())) {
            throw new IllegalStateException("Cannot finalize settlement: Settlement must be in status 'PAYMENT_RELEASED'. Current status: " + settlement.getStatus());
        }

        FnfReadinessDto readiness = readinessService.checkFinalizationReadiness(settlement);
        if (!readiness.isReady()) {
            throw new IllegalStateException("Cannot finalize settlement: " + String.join("; ", readiness.getBlockingItems().stream().map(FnfBlockingItemDto::getDescription).toList()));
        }

        Employee actor = resolveEmployeeForUser(currentUser, orgId);
        settlement.setStatus("FINALIZED");
        settlement.setFinalizedAt(LocalDateTime.now());
        settlement.setFinalizedBy(actor);
        settlement.setUpdatedAt(LocalDateTime.now());

        FnfSettlement saved = fnfRepository.saveAndFlush(settlement);

        EmployeeExit exit = settlement.getExit();
        exit.setStatus("FINALIZED");
        exit.setUpdatedAt(LocalDateTime.now());
        exitRepository.save(exit);

        recordAuditSnapshot(saved, "FINALIZE", currentUser, saved.getNetSettlement(), saved.getNetSettlement(), "F&F settlement formally finalized and permanently locked");

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("fnfId", saved.getId());
        resp.put("exitId", exit.getId());
        resp.put("status", "FINALIZED");
        resp.put("finalizedAt", saved.getFinalizedAt());
        resp.put("finalizedById", saved.getFinalizedById());
        resp.put("message", "Full & Final Settlement finalized successfully.");
        return resp;
    }

    @Transactional(readOnly = true)
    public FnfReadinessDto getFinalizationStatus(User currentUser, Long fnfId) {
        Long orgId = TenantContext.requireOrganizationId();
        bindRlsIfAvailable();

        FnfSettlement settlement = fnfRepository.findByIdAndOrganizationId(fnfId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("F&F Settlement not found with ID: " + fnfId));

        exitAuthService.assertCanViewFnf(currentUser, settlement);

        return readinessService.checkFinalizationReadiness(settlement);
    }

    @Transactional(readOnly = true)
    public List<FnfDocumentResponse> getSettlementDocuments(User currentUser, Long fnfId) {
        Long orgId = TenantContext.requireOrganizationId();
        bindRlsIfAvailable();

        FnfSettlement settlement = fnfRepository.findByIdAndOrganizationId(fnfId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("F&F Settlement not found with ID: " + fnfId));

        exitAuthService.assertCanViewFnf(currentUser, settlement);

        List<FnfDocumentResponse> docs = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        docs.add(new FnfDocumentResponse(
                "SETTLEMENT_STATEMENT",
                "Full & Final Settlement Statement",
                "fnf_settlement_" + fnfId + ".pdf",
                "/api/v1/fnf/" + fnfId + "/statement/download",
                settlement.getStatus(),
                now));

        docs.add(new FnfDocumentResponse(
                "CLEARANCE_SIGN_OFF",
                "Multi-Department Clearance Summary",
                "clearance_signoff_" + settlement.getExit().getId() + ".pdf",
                "/api/v1/employee-exits/" + settlement.getExit().getId() + "/clearances/download",
                "COMPLETED",
                now));

        if ("PAYMENT_RELEASED".equalsIgnoreCase(settlement.getStatus()) || "FINALIZED".equalsIgnoreCase(settlement.getStatus())) {
            docs.add(new FnfDocumentResponse(
                    "EXPERIENCE_LETTER",
                    "Service & Experience Certificate",
                    "experience_letter_" + settlement.getExit().getEmployee().getEmployeeId() + ".pdf",
                    "/api/v1/my-exit/experience-letter",
                    "READY",
                    now));
        }

        return docs;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getFinancialBreakdown(User currentUser, Long fnfId) {
        Long orgId = TenantContext.requireOrganizationId();
        bindRlsIfAvailable();

        FnfSettlement s = fnfRepository.findByIdAndOrganizationId(fnfId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("F&F Settlement not found with ID: " + fnfId));

        exitAuthService.assertCanViewFnf(currentUser, s);

        Map<String, Object> breakdown = new LinkedHashMap<>();
        breakdown.put("fnfId", s.getId());
        breakdown.put("status", s.getStatus());
        breakdown.put("earnings", mapToResponse(s).getEarnings());
        breakdown.put("deductions", mapToResponse(s).getDeductions());
        breakdown.put("netSettlement", s.getNetSettlement());
        return breakdown;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getDashboardSummary(User currentUser) {
        Long orgId = TenantContext.requireOrganizationId();
        bindRlsIfAvailable();

        Map<String, Object> dashboard = new LinkedHashMap<>();
        dashboard.put("draftCount", fnfRepository.countByOrganizationIdAndStatus(orgId, "DRAFT"));
        dashboard.put("calculatedCount", fnfRepository.countByOrganizationIdAndStatus(orgId, "CALCULATED"));
        dashboard.put("submittedCount", fnfRepository.countByOrganizationIdAndStatus(orgId, "SUBMITTED"));
        dashboard.put("approvalPendingCount",
                fnfRepository.countByOrganizationIdAndStatus(orgId, "FINANCE_APPROVAL_PENDING")
                        + fnfRepository.countByOrganizationIdAndStatus(orgId, "HR_APPROVAL_PENDING")
                        + fnfRepository.countByOrganizationIdAndStatus(orgId, "ADMIN_APPROVAL_PENDING"));
        dashboard.put("paymentPendingCount", fnfRepository.countByOrganizationIdAndStatus(orgId, "PAYMENT_PENDING") + fnfRepository.countByOrganizationIdAndStatus(orgId, "SETTLEMENT_APPROVED"));
        dashboard.put("paymentReleasedCount", fnfRepository.countByOrganizationIdAndStatus(orgId, "PAYMENT_RELEASED"));
        dashboard.put("finalizedCount", fnfRepository.countByOrganizationIdAndStatus(orgId, "FINALIZED"));
        dashboard.put("totalPaidAmount", fnfRepository.sumTotalPaidSettlements(orgId));
        dashboard.put("totalPendingAmount", fnfRepository.sumTotalPendingSettlements(orgId));
        return dashboard;
    }

    private void syncStatusWithWorkflow(FnfSettlement settlement) {
        if (settlement == null || settlement.getId() == null) return;
        String status = settlement.getStatus();

        if ("PAYMENT_RELEASED".equalsIgnoreCase(status) || "FINALIZED".equalsIgnoreCase(status)
                || "CANCELLED".equalsIgnoreCase(status) || "PAYMENT_FAILED".equalsIgnoreCase(status)
                || "DRAFT".equalsIgnoreCase(status) || "CALCULATED".equalsIgnoreCase(status)) {
            return;
        }

        Optional<ApprovalWorkflowInstance> instanceOpt = instanceRepository
                .findByBusinessReferenceTypeAndBusinessReferenceIdAndOrganizationId(
                        "FNF_SETTLEMENT", settlement.getId().toString(), settlement.getOrganization().getId());

        if (instanceOpt.isPresent()) {
            ApprovalWorkflowInstance instance = instanceOpt.get();
            if (instance.getStatus() == com.example.ems.approval.entity.ApprovalStatus.APPROVED) {
                if (!"PAYMENT_RELEASED".equalsIgnoreCase(status) && !"FINALIZED".equalsIgnoreCase(status)) {
                    settlement.setStatus("PAYMENT_PENDING");
                    settlement.setUpdatedAt(LocalDateTime.now());
                    fnfRepository.save(settlement);
                }
            } else if (instance.getStatus() == com.example.ems.approval.entity.ApprovalStatus.REJECTED) {
                settlement.setStatus("REJECTED");
                settlement.setUpdatedAt(LocalDateTime.now());
                fnfRepository.save(settlement);
            } else if (instance.getStatus() == com.example.ems.approval.entity.ApprovalStatus.IN_PROGRESS) {
                String targetStatus;
                int step = instance.getCurrentStep();
                if (step == 1) {
                    targetStatus = "FINANCE_APPROVAL_PENDING";
                } else if (step == 2) {
                    targetStatus = "HR_APPROVAL_PENDING";
                } else {
                    targetStatus = "ADMIN_APPROVAL_PENDING";
                }
                if (!targetStatus.equalsIgnoreCase(settlement.getStatus())) {
                    settlement.setStatus(targetStatus);
                    settlement.setUpdatedAt(LocalDateTime.now());
                    fnfRepository.save(settlement);
                }
            }
        }
    }

    private void assertEditableState(FnfSettlement s, String operation) {
        String status = s.getStatus();
        if (status == null || "DRAFT".equalsIgnoreCase(status) || "CALCULATED".equalsIgnoreCase(status)
                || "FINANCE_APPROVAL_PENDING".equalsIgnoreCase(status) || "HR_APPROVAL_PENDING".equalsIgnoreCase(status)
                || "ADMIN_APPROVAL_PENDING".equalsIgnoreCase(status)) {
            return;
        }
        if ("PAYMENT_RELEASED".equalsIgnoreCase(status) || "FINALIZED".equalsIgnoreCase(status) || "SETTLEMENT_COMPLETED".equalsIgnoreCase(status)) {
            throw new IllegalStateException("Cannot " + operation + " settlement: Payment has already been released.");
        }
        throw new ConflictException("FNF_IMMUTABLE: Cannot " + operation + " settlement: Settlement is in status '" + status
                + "'. Settlements cannot be modified in terminal or released status.");
    }

    private void recordAuditSnapshot(FnfSettlement s, String action, User actorUser, BigDecimal before,
                                     BigDecimal after, String reason) {
        try {
            Employee actor = resolveEmployeeForUser(actorUser, s.getOrganization().getId());
            String actorName = actor != null ? actor.getFullName()
                    : (actorUser != null ? actorUser.getFullName() : "System");

            String snapshotJson = objectMapper.writeValueAsString(mapToResponse(s));
            ExitFnfAudit audit = new ExitFnfAudit(
                    s.getOrganization(),
                    s.getId(),
                    action,
                    actor,
                    actorName,
                    before,
                    after,
                    reason,
                    snapshotJson);
            auditRepository.save(audit);
        } catch (Exception e) {
            log.warn("Failed to record F&F audit snapshot: {}", e.getMessage());
        }
    }

    private void bindRlsIfAvailable() {
        if (rlsSessionBinder != null) {
            try {
                rlsSessionBinder.bindCurrentTenant();
            } catch (Exception ignored) {
            }
        }
    }

    private FnfCalculationResponse mapToResponse(FnfSettlement s) {
        FnfEarningsBreakdown earnings = new FnfEarningsBreakdown();
        earnings.setSalary(s.getSalaryAmount());
        earnings.setUnpaidSalary(s.getUnpaidSalary());
        earnings.setLeaveEncashment(s.getLeaveEncashment());
        earnings.setBonus(s.getBonus());
        earnings.setIncentives(s.getIncentives());
        earnings.setOvertime(s.getOvertime());
        earnings.setReimbursements(s.getReimbursements());
        earnings.setGratuity(s.getGratuity());
        earnings.setOtherAllowances(s.getOtherAllowances());
        earnings.setTotal(s.getTotalEarnings());

        FnfDeductionsBreakdown deductions = new FnfDeductionsBreakdown();
        deductions.setNoticeRecovery(s.getNoticePeriodRecovery());
        deductions.setAssetDamage(s.getAssetDamage());
        deductions.setLoanRecovery(s.getLoanRecovery());
        deductions.setTax(s.getTaxDeduction());
        deductions.setOther(s.getOtherDeductions());
        deductions.setTotal(s.getTotalDeductions());

        return new FnfCalculationResponse(
                s.getId(),
                s.getExit().getId(),
                earnings,
                deductions,
                s.getNetSettlement(),
                s.getStatus());
    }

    private FnfPaymentResponse mapToPaymentResponse(FnfSettlement saved) {
        Employee paidBy = saved.getPaidBy();
        EmployeeExit exit = saved.getExit();
        Employee employee = exit.getEmployee();

        FnfPaymentResponse resp = new FnfPaymentResponse();
        resp.setFnfId(saved.getId());
        resp.setExitId(exit.getId());
        resp.setEmployeeId(employee.getId());
        resp.setEmployeeName(employee.getFullName());
        resp.setStatus(saved.getStatus());
        resp.setPaymentMethod(saved.getPaymentMethod());
        resp.setPaymentDate(saved.getPaymentDate());
        resp.setAmount(saved.getPaidAmount());
        resp.setTransactionReference(saved.getPaymentReference());
        resp.setIdempotencyKey(saved.getIdempotencyKey());
        resp.setRemarks(saved.getPaymentRemarks());
        resp.setPaidAt(saved.getPaidAt());
        resp.setPaidByName(paidBy != null ? paidBy.getFullName() : "Finance Office");
        return resp;
    }

    private Employee resolveEmployeeForUser(User user, Long orgId) {
        if (user != null && user.getWorkEmail() != null) {
            Optional<Employee> emp = employeeRepository.findByEmail(user.getWorkEmail());
            if (emp.isPresent())
                return emp.get();
        }
        List<Employee> emps = employeeRepository.findByOrganizationId(orgId);
        return emps.isEmpty() ? null : emps.get(0);
    }
}

