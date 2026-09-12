package com.example.ems.bonus.controller;

import com.example.ems.bonus.dto.*;
import com.example.ems.bonus.entity.BonusPayrollStatus;
import com.example.ems.bonus.entity.BonusRecord;
import com.example.ems.bonus.entity.BonusStatus;
import com.example.ems.bonus.entity.BonusType;
import com.example.ems.bonus.repository.BonusRecordRepository;
import com.example.ems.bonus.service.BonusAdjustmentService;
import com.example.ems.bonus.service.BonusCalculationService;
import com.example.ems.bonus.service.BonusPayrollIntegrationService;
import com.example.ems.bonus.service.BonusWorkflowService;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.exception.ResourceNotFoundException;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.security.context.TenantContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping({"/api/v1/bonuses", "/api/v1/bonus-records"})
@CrossOrigin("*")
@Tag(name = "Bonus Management", description = "Endpoints for Bonus Calculation Preview, Persistence, Adjustments, Workflow Submissions, and Payroll Integration.")
public class BonusRecordController {

    private final BonusCalculationService calculationService;
    private final BonusAdjustmentService adjustmentService;
    private final BonusWorkflowService workflowService;
    private final BonusPayrollIntegrationService payrollIntegrationService;
    private final BonusRecordRepository recordRepository;
    private final EmployeeRepository employeeRepository;

    public BonusRecordController(BonusCalculationService calculationService,
                                 BonusAdjustmentService adjustmentService,
                                 BonusWorkflowService workflowService,
                                 BonusPayrollIntegrationService payrollIntegrationService,
                                 BonusRecordRepository recordRepository,
                                 EmployeeRepository employeeRepository) {
        this.calculationService = calculationService;
        this.adjustmentService = adjustmentService;
        this.workflowService = workflowService;
        this.payrollIntegrationService = payrollIntegrationService;
        this.recordRepository = recordRepository;
        this.employeeRepository = employeeRepository;
    }

    @Operation(summary = "Preview Bonus Calculation", description = "Stateless preview calculation of bonus amounts and eligibility without persisting.")
    @PostMapping("/calculate/preview")
    @PreAuthorize("hasAuthority('BONUS_VIEW') or hasRole('EMPLOYEE') or hasRole('MANAGER') or hasRole('HR') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<BonusPreviewResponse>> previewBonus(
            @Valid @RequestBody BonusCalculateRequest request) {
        BonusPreviewResponse response = calculationService.previewBonus(request);
        return ResponseEntity.ok(ApiResponse.success("Bonus preview calculated successfully", response));
    }

    @Operation(summary = "Calculate and Persist Bonus", description = "Evaluates eligibility, calculates bonus amount, and persists record in CALCULATED status.")
    @PostMapping("/calculate")
    @PreAuthorize("hasAuthority('BONUS_CREATE') or hasRole('MANAGER') or hasRole('HR') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<BonusRecordResponse>> calculateBonus(
            @Valid @RequestBody BonusCalculateRequest request) {
        BonusRecordResponse response = calculationService.calculateAndPersist(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Bonus calculated and saved successfully", response));
    }

    @Operation(summary = "Adjust Bonus Record", description = "Allows manager/HR to adjust bonus amount with mandatory reason without overwriting original calculated value.")
    @PostMapping("/{id}/adjust")
    @PreAuthorize("hasAuthority('BONUS_ADJUST') or hasRole('MANAGER') or hasRole('HR') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<BonusRecordResponse>> adjustBonus(
            @PathVariable("id") Long id,
            @Valid @RequestBody BonusAdjustmentRequest request) {
        BonusRecordResponse response = adjustmentService.adjustBonus(id, request);
        return ResponseEntity.ok(ApiResponse.success("Bonus record adjusted successfully", response));
    }

    @Operation(summary = "Submit Bonus for Approval", description = "Submits a bonus record for Central Approval Platform review or auto-approves if not required by policy.")
    @PostMapping("/{id}/submit")
    @PreAuthorize("hasAuthority('BONUS_CREATE') or hasRole('MANAGER') or hasRole('HR') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<BonusRecordResponse>> submitBonus(@PathVariable("id") Long id) {
        BonusRecordResponse response = workflowService.submitForApproval(id);
        return ResponseEntity.ok(ApiResponse.success("Bonus record submitted successfully", response));
    }

    @Operation(summary = "Cancel Bonus Record", description = "Cancels a bonus record prior to payroll posting.")
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('BONUS_CREATE') or hasRole('MANAGER') or hasRole('HR') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<BonusRecordResponse>> cancelBonus(
            @PathVariable("id") Long id,
            @RequestParam(value = "reason", required = false) String reason) {
        BonusRecordResponse response = workflowService.cancelBonus(id, reason);
        return ResponseEntity.ok(ApiResponse.success("Bonus record cancelled successfully", response));
    }

    @Operation(summary = "Get Bonus Record By ID", description = "Retrieves a bonus record by ID scoped to current organization.")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('BONUS_VIEW') or hasRole('EMPLOYEE') or hasRole('MANAGER') or hasRole('HR') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<BonusRecordResponse>> getBonusById(@PathVariable("id") Long id) {
        Long orgId = TenantContext.requireOrganizationId();
        BonusRecord record = recordRepository.findByIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Bonus record not found with ID: " + id));
        return ResponseEntity.ok(ApiResponse.success("Bonus record retrieved successfully", BonusRecordResponse.fromEntity(record)));
    }

    @Operation(summary = "Get Eligible Records for Payroll", description = "Returns approved bonus records eligible for a payroll period with optional payroll status filter (defaults to PENDING).")
    @GetMapping("/payroll/eligible")
    @PreAuthorize("hasAuthority('BONUS_PAYROLL_POST') or hasAuthority('BONUS_VIEW') or hasRole('HR') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<BonusRecordResponse>>> getEligibleForPayroll(
            @RequestParam("employeeId") Long employeeId,
            @RequestParam("periodStart") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodStart,
            @RequestParam("periodEnd") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodEnd,
            @RequestParam(value = "payrollStatus", required = false) BonusPayrollStatus payrollStatus) {
        List<BonusRecordResponse> responses = (payrollStatus != null)
                ? payrollIntegrationService.getEligibleBonusRecords(employeeId, periodStart, periodEnd, payrollStatus)
                : payrollIntegrationService.getEligibleBonusRecords(employeeId, periodStart, periodEnd);
        return ResponseEntity.ok(ApiResponse.success("Eligible bonus records retrieved successfully", responses));
    }

    @Operation(summary = "Get Filtered Bonus Records", description = "Queries bonus records with multi-criteria filtering for reporting and administration.")
    @GetMapping
    @PreAuthorize("hasAuthority('BONUS_VIEW') or hasRole('MANAGER') or hasRole('HR') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Page<BonusRecordResponse>>> getBonusRecords(
            @RequestParam(value = "employeeId", required = false) Long employeeId,
            @RequestParam(value = "policyId", required = false) Long policyId,
            @RequestParam(value = "bonusType", required = false) BonusType bonusType,
            @RequestParam(value = "status", required = false) BonusStatus status,
            @RequestParam(value = "payrollStatus", required = false) BonusPayrollStatus payrollStatus,
            @RequestParam(value = "periodStart", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodStart,
            @RequestParam(value = "periodEnd", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodEnd,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long orgId = TenantContext.requireOrganizationId();
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(100, Math.max(1, size)), Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<BonusRecord> records = recordRepository.findFiltered(
                orgId, employeeId, policyId, bonusType, status, payrollStatus, periodStart, periodEnd, pageable);
        return ResponseEntity.ok(ApiResponse.success("Bonus records retrieved successfully", records.map(BonusRecordResponse::fromEntity)));
    }

    @Operation(summary = "Get My Bonus Records", description = "Retrieves paginated bonus records for the currently authenticated employee.")
    @GetMapping("/me")
    @PreAuthorize("hasAuthority('BONUS_VIEW') or hasRole('EMPLOYEE') or hasRole('MANAGER') or hasRole('HR') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Page<BonusRecordResponse>>> getMyBonusRecords(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long orgId = TenantContext.requireOrganizationId();
        Employee employee = resolveCurrentEmployee(orgId);
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(100, Math.max(1, size)), Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<BonusRecord> records = recordRepository.findByOrganizationIdAndEmployeeId(orgId, employee.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success("My bonus records retrieved successfully", records.map(BonusRecordResponse::fromEntity)));
    }

    private Employee resolveCurrentEmployee(Long orgId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getName() != null) {
            return employeeRepository.findByEmailAndOrganizationId(auth.getName().trim().toLowerCase(), orgId)
                    .orElseThrow(() -> new ResourceNotFoundException("Employee profile not found for authenticated user."));
        }
        throw new SecurityException("No authenticated security context found.");
    }
}
