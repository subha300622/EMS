package com.example.ems.payroll.controller;

import com.example.ems.common.dto.ApiResponse;
import com.example.ems.payroll.dto.PayrollRunCreateRequest;
import com.example.ems.payroll.dto.PayrollRunResponse;
import com.example.ems.payroll.service.PayrollRunService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payroll/runs")
public class PayrollRunController {

    private final PayrollRunService payrollRunService;
    private final com.example.ems.payroll.service.PayrollDetailService payrollDetailService;

    public PayrollRunController(PayrollRunService payrollRunService,
                                com.example.ems.payroll.service.PayrollDetailService payrollDetailService) {
        this.payrollRunService = payrollRunService;
        this.payrollDetailService = payrollDetailService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PayrollRunResponse>> createPayrollRun(
            @Valid @RequestBody PayrollRunCreateRequest request) {
        PayrollRunResponse response = payrollRunService.createPayrollRun(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Payroll run created successfully", response));
    }

    @PostMapping("/{runId}/process")
    public ResponseEntity<ApiResponse<PayrollRunResponse>> processPayrollRun(
            @PathVariable Long runId) {
        PayrollRunResponse response = payrollRunService.processPayrollRun(runId);
        return ResponseEntity.ok(ApiResponse.success("Payroll run processed successfully", response));
    }

    @PostMapping("/{runId}/finalize")
    public ResponseEntity<ApiResponse<PayrollRunResponse>> finalizePayrollRun(
            @PathVariable Long runId) {
        PayrollRunResponse response = payrollRunService.finalizePayrollRun(runId);
        return ResponseEntity.ok(ApiResponse.success("Payroll run finalized successfully", response));
    }

    @PostMapping("/{runId}/submit-approval")
    public ResponseEntity<ApiResponse<PayrollRunResponse>> submitForApproval(
            @PathVariable Long runId) {
        PayrollRunResponse response = payrollRunService.submitForApproval(runId);
        return ResponseEntity.ok(ApiResponse.success("Payroll run submitted for approval successfully", response));
    }

    @PostMapping("/{runId}/cancel-approval")
    public ResponseEntity<ApiResponse<PayrollRunResponse>> cancelApproval(
            @PathVariable Long runId,
            @RequestBody(required = false) com.example.ems.payroll.dto.PayrollApprovalCancelRequest request) {
        String reason = request != null ? request.getReason() : "Approval cancelled by manager";
        PayrollRunResponse response = payrollRunService.cancelApproval(runId, reason);
        return ResponseEntity.ok(ApiResponse.success("Payroll run approval cancelled successfully", response));
    }

    @PostMapping("/{runId}/lock")
    public ResponseEntity<ApiResponse<PayrollRunResponse>> lockPayrollRun(
            @PathVariable Long runId) {
        PayrollRunResponse response = payrollRunService.lockPayrollRun(runId);
        return ResponseEntity.ok(ApiResponse.success("Payroll run locked successfully", response));
    }

    @GetMapping("/{runId}")
    public ResponseEntity<ApiResponse<PayrollRunResponse>> getPayrollRun(
            @PathVariable Long runId) {
        PayrollRunResponse response = payrollRunService.getPayrollRun(runId);
        return ResponseEntity.ok(ApiResponse.success("Payroll run retrieved successfully", response));
    }

    @GetMapping("/{runId}/summary")
    public ResponseEntity<ApiResponse<PayrollRunResponse>> getPayrollRunSummary(
            @PathVariable Long runId) {
        PayrollRunResponse response = payrollRunService.getPayrollRunSummary(runId);
        return ResponseEntity.ok(ApiResponse.success("Payroll run summary retrieved successfully", response));
    }

    @GetMapping("/{runId}/status")
    public ResponseEntity<ApiResponse<com.example.ems.payroll.dto.PayrollRunStatusResponse>> getPayrollRunStatus(
            @PathVariable Long runId) {
        com.example.ems.payroll.dto.PayrollRunStatusResponse response = payrollRunService.getPayrollRunStatus(runId);
        return ResponseEntity.ok(ApiResponse.success("Payroll run status retrieved successfully", response));
    }

    @GetMapping("/{runId}/payslips")
    public ResponseEntity<ApiResponse<List<com.example.ems.payroll.dto.PayslipDetailResponse>>> getPayrollRunPayslips(
            @PathVariable Long runId) {
        List<com.example.ems.payroll.dto.PayslipDetailResponse> response = payrollDetailService.getPayslips(runId);
        return ResponseEntity.ok(ApiResponse.success("Payroll run payslips retrieved successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PayrollRunResponse>>> listPayrollRuns() {
        List<PayrollRunResponse> response = payrollRunService.listPayrollRuns();
        return ResponseEntity.ok(ApiResponse.success("Payroll runs retrieved successfully", response));
    }
}
