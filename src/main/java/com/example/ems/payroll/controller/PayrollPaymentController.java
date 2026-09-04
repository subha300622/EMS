package com.example.ems.payroll.controller;

import com.example.ems.common.dto.ApiResponse;
import com.example.ems.payroll.dto.PaymentExecutionResponse;
import com.example.ems.payroll.dto.PayrollPaymentResponse;
import com.example.ems.payroll.service.PayrollPaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class PayrollPaymentController {

    private final PayrollPaymentService paymentService;

    public PayrollPaymentController(PayrollPaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping({"/api/v1/payroll/runs/{runId}/payments", "/api/v1/payroll/runs/{runId}/payments/execute"})
    public ResponseEntity<ApiResponse<PaymentExecutionResponse>> executePayments(
            @PathVariable Long runId,
            @RequestParam(required = false) String mode,
            @RequestParam(required = false) String simulation,
            @RequestBody(required = false) @jakarta.validation.Valid com.example.ems.payroll.dto.ExecutePayrollPaymentRequest body) {

        String selectedMode = mode;
        String sim = simulation;
        if (body != null) {
            selectedMode = body.getEffectiveMode(selectedMode);
            sim = body.getEffectiveSimulation(sim);
        }
        if (selectedMode == null || selectedMode.isBlank()) {
            selectedMode = "NEFT";
        }

        PaymentExecutionResponse response = paymentService.executePayrollPayments(runId, selectedMode, sim);
        return ResponseEntity.ok(ApiResponse.success("Payroll payments executed successfully", response));
    }

    @PostMapping("/api/v1/payroll/payments/{paymentId}/retry")
    public ResponseEntity<ApiResponse<PayrollPaymentResponse>> retryPayment(
            @PathVariable Long paymentId) {
        PayrollPaymentResponse response = paymentService.retryPayment(paymentId);
        return ResponseEntity.ok(ApiResponse.success("Payment retry executed successfully", response));
    }

    @GetMapping("/api/v1/payroll/runs/{runId}/payments")
    public ResponseEntity<ApiResponse<List<PayrollPaymentResponse>>> getPayments(
            @PathVariable Long runId) {
        List<PayrollPaymentResponse> response = paymentService.getPaymentsForRun(runId);
        return ResponseEntity.ok(ApiResponse.success("Payroll payments retrieved successfully", response));
    }
}
