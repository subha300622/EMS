package com.example.ems.incentive.controller;

import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.exception.ResourceNotFoundException;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.incentive.dto.*;
import com.example.ems.incentive.entity.IncentivePayrollStatus;
import com.example.ems.incentive.entity.IncentiveRecord;
import com.example.ems.incentive.entity.IncentiveStatus;
import com.example.ems.incentive.repository.IncentiveRecordRepository;
import com.example.ems.incentive.service.IncentiveAdjustmentService;
import com.example.ems.incentive.service.IncentiveCalculationService;
import com.example.ems.incentive.service.IncentivePayrollIntegrationService;
import com.example.ems.incentive.service.IncentiveWorkflowService;
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
@RequestMapping({"/api/v1/incentives", "/api/v1/incentive-records"})
@CrossOrigin("*")
@Tag(name = "Incentive Management", description = "Endpoints for Incentive Calculation Preview, Persistence, Adjustments, and Workflow Submissions.")
public class IncentiveRecordController {

    private final IncentiveCalculationService calculationService;
    private final IncentiveAdjustmentService adjustmentService;
    private final IncentiveWorkflowService workflowService;
    private final IncentivePayrollIntegrationService payrollIntegrationService;
    private final IncentiveRecordRepository recordRepository;
    private final EmployeeRepository employeeRepository;

    public IncentiveRecordController(IncentiveCalculationService calculationService,
                                     IncentiveAdjustmentService adjustmentService,
                                     IncentiveWorkflowService workflowService,
                                     IncentivePayrollIntegrationService payrollIntegrationService,
                                     IncentiveRecordRepository recordRepository,
                                     EmployeeRepository employeeRepository) {
        this.calculationService = calculationService;
        this.adjustmentService = adjustmentService;
        this.workflowService = workflowService;
        this.payrollIntegrationService = payrollIntegrationService;
        this.recordRepository = recordRepository;
        this.employeeRepository = employeeRepository;
    }

    @Operation(summary = "Preview Incentive Calculation", description = "Stateless preview calculation of incentive amounts, eligibility, and target achievement without persisting.")
    @PostMapping("/calculate/preview")
    @PreAuthorize("hasAuthority('INCENTIVE_VIEW') or hasRole('EMPLOYEE') or hasRole('MANAGER') or hasRole('HR') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<IncentivePreviewResponse>> previewIncentive(
            @Valid @RequestBody IncentiveCalculateRequest request) {
        IncentivePreviewResponse response = calculationService.previewIncentive(request);
        return ResponseEntity.ok(ApiResponse.success("Incentive preview calculated successfully", response));
    }

    @Operation(summary = "Calculate and Persist Incentive", description = "Evaluates eligibility, target achievement, and calculation method, persisting record in CALCULATED status.")
    @PostMapping("/calculate")
    @PreAuthorize("hasAuthority('INCENTIVE_CREATE') or hasRole('MANAGER') or hasRole('HR') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<IncentiveRecordResponse>> calculateIncentive(
            @Valid @RequestBody IncentiveCalculateRequest request) {
        IncentiveRecordResponse response = calculationService.calculateAndPersist(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Incentive calculated and saved successfully", response));
    }

    @Operation(summary = "Adjust Incentive Record", description = "Allows manager/HR to adjust incentive amount with mandatory reason while strictly preserving original calculated values.")
    @PostMapping("/{id}/adjust")
    @PreAuthorize("hasAuthority('INCENTIVE_ADJUST') or hasRole('MANAGER') or hasRole('HR') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<IncentiveRecordResponse>> adjustIncentive(
            @PathVariable("id") Long id,
            @Valid @RequestBody IncentiveAdjustmentRequest request) {
        IncentiveRecordResponse response = adjustmentService.adjustIncentive(id, request);
        return ResponseEntity.ok(ApiResponse.success("Incentive record adjusted successfully", response));
    }

    @Operation(summary = "Submit Incentive for Approval", description = "Submits an incentive record for Central Approval Platform review or auto-approves if not required by policy.")
    @PostMapping("/{id}/submit")
    @PreAuthorize("hasAuthority('INCENTIVE_CREATE') or hasRole('MANAGER') or hasRole('HR') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<IncentiveRecordResponse>> submitIncentive(@PathVariable("id") Long id) {
        IncentiveRecordResponse response = workflowService.submitForApproval(id);
        return ResponseEntity.ok(ApiResponse.success("Incentive record submitted successfully", response));
    }

    @Operation(summary = "Cancel Incentive Record", description = "Cancels an incentive record prior to payroll posting.")
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('INCENTIVE_CREATE') or hasRole('MANAGER') or hasRole('HR') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<IncentiveRecordResponse>> cancelIncentive(
            @PathVariable("id") Long id,
            @RequestParam(value = "reason", required = false) String reason) {
        IncentiveRecordResponse response = workflowService.cancelIncentive(id, reason);
        return ResponseEntity.ok(ApiResponse.success("Incentive record cancelled successfully", response));
    }

    @Operation(summary = "Get Incentive Record By ID", description = "Retrieves an incentive record by ID scoped to current organization.")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('INCENTIVE_VIEW') or hasRole('EMPLOYEE') or hasRole('MANAGER') or hasRole('HR') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<IncentiveRecordResponse>> getIncentiveById(@PathVariable("id") Long id) {
        Long orgId = TenantContext.requireOrganizationId();
        IncentiveRecord record = recordRepository.findByIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Incentive record not found with ID: " + id));
        return ResponseEntity.ok(ApiResponse.success("Incentive record retrieved successfully", IncentiveRecordResponse.fromEntity(record)));
    }

    @Operation(summary = "Get Eligible Records for Payroll", description = "Returns approved incentive records eligible for a payroll period.")
    @GetMapping("/payroll/eligible")
    @PreAuthorize("hasAuthority('INCENTIVE_PAYROLL_POST') or hasAuthority('INCENTIVE_VIEW') or hasRole('HR') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<IncentiveRecordResponse>>> getEligibleForPayroll(
            @RequestParam("employeeId") Long employeeId,
            @RequestParam("periodStart") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodStart,
            @RequestParam("periodEnd") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodEnd) {
        List<IncentiveRecordResponse> responses = payrollIntegrationService.getEligibleIncentiveRecords(employeeId, periodStart, periodEnd);
        return ResponseEntity.ok(ApiResponse.success("Eligible incentive records retrieved successfully", responses));
    }

    @Operation(summary = "Get Filtered Incentive Records", description = "Queries incentive records with multi-criteria filtering for reporting and administration.")
    @GetMapping
    @PreAuthorize("hasAuthority('INCENTIVE_VIEW') or hasRole('MANAGER') or hasRole('HR') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Page<IncentiveRecordResponse>>> getIncentiveRecords(
            @RequestParam(value = "employeeId", required = false) Long employeeId,
            @RequestParam(value = "status", required = false) IncentiveStatus status,
            @RequestParam(value = "payrollStatus", required = false) IncentivePayrollStatus payrollStatus,
            @RequestParam(value = "periodStart", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodStart,
            @RequestParam(value = "periodEnd", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodEnd,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long orgId = TenantContext.requireOrganizationId();
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(100, Math.max(1, size)), Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<IncentiveRecord> records = recordRepository.findFiltered(orgId, employeeId, status, payrollStatus, periodStart, periodEnd, pageable);
        return ResponseEntity.ok(ApiResponse.success("Incentive records retrieved successfully", records.map(IncentiveRecordResponse::fromEntity)));
    }

    @Operation(summary = "Get My Incentive Records", description = "Retrieves paginated incentive records for the currently authenticated employee.")
    @GetMapping("/me")
    @PreAuthorize("hasAuthority('INCENTIVE_VIEW') or hasRole('EMPLOYEE') or hasRole('MANAGER') or hasRole('HR') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Page<IncentiveRecordResponse>>> getMyIncentiveRecords(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long orgId = TenantContext.requireOrganizationId();
        Employee employee = resolveCurrentEmployee(orgId);
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(100, Math.max(1, size)), Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<IncentiveRecord> records = recordRepository.findByOrganizationIdAndEmployeeId(orgId, employee.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success("My incentive records retrieved successfully", records.map(IncentiveRecordResponse::fromEntity)));
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
