package com.example.ems.payroll.controller;

import com.example.ems.common.dto.ApiResponse;
import com.example.ems.payroll.dto.PayrollEmployeeResponse;
import com.example.ems.payroll.dto.PayrollItemResponse;
import com.example.ems.payroll.dto.PayslipDetailResponse;
import com.example.ems.payroll.service.PayrollDetailService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payroll/runs/{runId}/employees")
public class PayrollEmployeeController {

    private final PayrollDetailService payrollDetailService;

    public PayrollEmployeeController(PayrollDetailService payrollDetailService) {
        this.payrollDetailService = payrollDetailService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PayrollEmployeeResponse>>> getPayrollEmployees(
            @PathVariable Long runId) {
        List<PayrollEmployeeResponse> response = payrollDetailService.getPayrollEmployees(runId);
        return ResponseEntity.ok(ApiResponse.success("Payroll employees retrieved successfully", response));
    }

    @GetMapping("/{payrollEmployeeId}")
    public ResponseEntity<ApiResponse<PayrollEmployeeResponse>> getPayrollEmployee(
            @PathVariable Long runId,
            @PathVariable Long payrollEmployeeId) {
        PayrollEmployeeResponse response = payrollDetailService.getPayrollEmployee(runId, payrollEmployeeId);
        return ResponseEntity.ok(ApiResponse.success("Payroll employee retrieved successfully", response));
    }

    @GetMapping("/{payrollEmployeeId}/items")
    public ResponseEntity<ApiResponse<List<PayrollItemResponse>>> getPayrollItems(
            @PathVariable Long runId,
            @PathVariable Long payrollEmployeeId) {
        List<PayrollItemResponse> response = payrollDetailService.getPayrollItems(runId, payrollEmployeeId);
        return ResponseEntity.ok(ApiResponse.success("Payroll items retrieved successfully", response));
    }

    @GetMapping("/{payrollEmployeeId}/payslip")
    public ResponseEntity<ApiResponse<PayslipDetailResponse>> getPayslip(
            @PathVariable Long runId,
            @PathVariable Long payrollEmployeeId) {
        PayslipDetailResponse response = payrollDetailService.getPayslip(runId, payrollEmployeeId);
        return ResponseEntity.ok(ApiResponse.success("Payslip retrieved successfully", response));
    }
}
