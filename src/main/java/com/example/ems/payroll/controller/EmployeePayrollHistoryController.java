package com.example.ems.payroll.controller;

import com.example.ems.common.dto.ApiResponse;
import com.example.ems.payroll.dto.EmployeePayrollHistoryResponse;
import com.example.ems.payroll.dto.PayslipDetailResponse;
import com.example.ems.payroll.service.EmployeePayrollHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/employees/{employeeId}/payroll-history")
@CrossOrigin("*")
@Tag(name = "Employee Payroll History APIs")
public class EmployeePayrollHistoryController {

    private final EmployeePayrollHistoryService employeePayrollHistoryService;

    public EmployeePayrollHistoryController(EmployeePayrollHistoryService employeePayrollHistoryService) {
        this.employeePayrollHistoryService = employeePayrollHistoryService;
    }

    @Operation(summary = "Get Employee Payroll History")
    @GetMapping
    public ResponseEntity<ApiResponse<List<EmployeePayrollHistoryResponse>>> getPayrollHistory(
            @PathVariable Long employeeId) {
        List<EmployeePayrollHistoryResponse> response = employeePayrollHistoryService.getPayrollHistory(employeeId);
        return ResponseEntity.ok(ApiResponse.success("Employee payroll history retrieved successfully", response));
    }

    @Operation(summary = "Get Employee Payroll History for a Specific Run")
    @GetMapping("/{runId}")
    public ResponseEntity<ApiResponse<PayslipDetailResponse>> getPayrollHistoryByRun(
            @PathVariable Long employeeId,
            @PathVariable Long runId) {
        PayslipDetailResponse response = employeePayrollHistoryService.getPayrollHistoryByRun(employeeId, runId);
        return ResponseEntity.ok(ApiResponse.success("Employee payroll history for run retrieved successfully", response));
    }
}
