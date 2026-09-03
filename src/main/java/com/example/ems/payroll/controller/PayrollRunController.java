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

    public PayrollRunController(PayrollRunService payrollRunService) {
        this.payrollRunService = payrollRunService;
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

    @GetMapping("/{runId}")
    public ResponseEntity<ApiResponse<PayrollRunResponse>> getPayrollRun(
            @PathVariable Long runId) {
        PayrollRunResponse response = payrollRunService.getPayrollRun(runId);
        return ResponseEntity.ok(ApiResponse.success("Payroll run retrieved successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PayrollRunResponse>>> listPayrollRuns() {
        List<PayrollRunResponse> response = payrollRunService.listPayrollRuns();
        return ResponseEntity.ok(ApiResponse.success("Payroll runs retrieved successfully", response));
    }
}
