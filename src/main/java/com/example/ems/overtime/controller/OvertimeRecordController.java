package com.example.ems.overtime.controller;

import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.exception.ResourceNotFoundException;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.overtime.dto.*;
import com.example.ems.overtime.entity.OvertimePayrollStatus;
import com.example.ems.overtime.entity.OvertimeRecord;
import com.example.ems.overtime.entity.OvertimeStatus;
import com.example.ems.overtime.repository.OvertimeRecordRepository;
import com.example.ems.overtime.service.OvertimeAdjustmentService;
import com.example.ems.overtime.service.OvertimeCalculationService;
import com.example.ems.overtime.service.OvertimeWorkflowService;
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

@RestController
@RequestMapping("/api/v1/overtime")
@CrossOrigin("*")
@Tag(name = "Overtime Management", description = "Endpoints for Overtime Preview, Calculation, Adjustments, and Workflow Submissions.")
public class OvertimeRecordController {

    private final OvertimeCalculationService calculationService;
    private final OvertimeAdjustmentService adjustmentService;
    private final OvertimeWorkflowService workflowService;
    private final OvertimeRecordRepository recordRepository;
    private final EmployeeRepository employeeRepository;

    public OvertimeRecordController(OvertimeCalculationService calculationService,
                                    OvertimeAdjustmentService adjustmentService,
                                    OvertimeWorkflowService workflowService,
                                    OvertimeRecordRepository recordRepository,
                                    EmployeeRepository employeeRepository) {
        this.calculationService = calculationService;
        this.adjustmentService = adjustmentService;
        this.workflowService = workflowService;
        this.recordRepository = recordRepository;
        this.employeeRepository = employeeRepository;
    }

    @Operation(summary = "Preview Overtime Calculation", description = "Stateless preview calculation of overtime hours, hourly rate, multipliers, and calculated amounts without persisting.")
    @PostMapping("/calculate/preview")
    @PreAuthorize("hasAuthority('OVERTIME_VIEW') or hasRole('EMPLOYEE') or hasRole('MANAGER') or hasRole('HR') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<OvertimePreviewResponse>> previewOvertime(
            @Valid @RequestBody OvertimeCalculateRequest request) {
        OvertimePreviewResponse response = calculationService.previewOvertime(request.getAttendanceId());
        return ResponseEntity.ok(ApiResponse.success("Overtime preview calculated successfully", response));
    }

    @Operation(summary = "Calculate and Persist Overtime", description = "Derives overtime from attendance, applies active applicable policy rules, and persists in CALCULATED status.")
    @PostMapping("/calculate")
    @PreAuthorize("hasAuthority('OVERTIME_CREATE') or hasRole('EMPLOYEE') or hasRole('MANAGER') or hasRole('HR') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<OvertimeRecordResponse>> calculateOvertime(
            @Valid @RequestBody OvertimeCalculateRequest request) {
        OvertimeRecordResponse response = calculationService.calculateOvertime(request.getAttendanceId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Overtime calculated and saved successfully", response));
    }

    @Operation(summary = "Adjust Overtime Record", description = "Allows manager/HR to adjust overtime hours with mandatory reason while strictly preserving original calculated values.")
    @PostMapping("/{id}/adjust")
    @PreAuthorize("hasAuthority('OVERTIME_ADJUST') or hasRole('MANAGER') or hasRole('HR') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<OvertimeRecordResponse>> adjustOvertime(
            @PathVariable("id") Long id,
            @Valid @RequestBody OvertimeAdjustmentRequest request) {
        OvertimeRecordResponse response = adjustmentService.adjustOvertime(id, request);
        return ResponseEntity.ok(ApiResponse.success("Overtime record adjusted successfully", response));
    }

    @Operation(summary = "Submit Overtime for Approval", description = "Submits an overtime record for Central Approval Platform review or auto-approves if approval is not required by policy.")
    @PostMapping("/{id}/submit")
    @PreAuthorize("hasAuthority('OVERTIME_CREATE') or hasRole('EMPLOYEE') or hasRole('MANAGER') or hasRole('HR') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<OvertimeRecordResponse>> submitOvertime(@PathVariable("id") Long id) {
        OvertimeRecordResponse response = workflowService.submitOvertime(id);
        return ResponseEntity.ok(ApiResponse.success("Overtime record submitted successfully", response));
    }

    @Operation(summary = "Get Overtime Record By ID", description = "Retrieves an overtime record by ID scoped to current organization.")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('OVERTIME_VIEW') or hasRole('EMPLOYEE') or hasRole('MANAGER') or hasRole('HR') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<OvertimeRecordResponse>> getOvertimeById(@PathVariable("id") Long id) {
        Long orgId = TenantContext.requireOrganizationId();
        OvertimeRecord record = recordRepository.findByIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Overtime record not found with ID: " + id));
        return ResponseEntity.ok(ApiResponse.success("Overtime record retrieved successfully", OvertimeRecordResponse.fromEntity(record)));
    }

    @Operation(summary = "Get Filtered Overtime Records", description = "Queries overtime records with multi-criteria filtering for reporting and administration.")
    @GetMapping
    @PreAuthorize("hasAuthority('OVERTIME_VIEW') or hasRole('MANAGER') or hasRole('HR') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Page<OvertimeRecordResponse>>> getOvertimeRecords(
            @RequestParam(value = "employeeId", required = false) Long employeeId,
            @RequestParam(value = "status", required = false) OvertimeStatus status,
            @RequestParam(value = "payrollStatus", required = false) OvertimePayrollStatus payrollStatus,
            @RequestParam(value = "fromDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(value = "toDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long orgId = TenantContext.requireOrganizationId();
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(100, Math.max(1, size)), Sort.by(Sort.Direction.DESC, "workDate"));
        Page<OvertimeRecord> records = recordRepository.findFiltered(orgId, employeeId, status, payrollStatus, fromDate, toDate, pageable);
        return ResponseEntity.ok(ApiResponse.success("Overtime records retrieved successfully", records.map(OvertimeRecordResponse::fromEntity)));
    }

    @Operation(summary = "Get My Overtime Records", description = "Retrieves paginated overtime records for the currently authenticated employee.")
    @GetMapping("/me")
    @PreAuthorize("hasAuthority('OVERTIME_VIEW') or hasRole('EMPLOYEE') or hasRole('MANAGER') or hasRole('HR') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Page<OvertimeRecordResponse>>> getMyOvertimeRecords(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long orgId = TenantContext.requireOrganizationId();
        Employee employee = resolveCurrentEmployee(orgId);
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(100, Math.max(1, size)), Sort.by(Sort.Direction.DESC, "workDate"));
        Page<OvertimeRecord> records = recordRepository.findByOrganizationIdAndEmployeeId(orgId, employee.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success("My overtime records retrieved successfully", records.map(OvertimeRecordResponse::fromEntity)));
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
