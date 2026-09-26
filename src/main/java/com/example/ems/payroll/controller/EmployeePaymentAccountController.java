package com.example.ems.payroll.controller;

import com.example.ems.common.dto.ApiResponse;
import com.example.ems.payroll.dto.EmployeePaymentAccountRequest;
import com.example.ems.payroll.dto.EmployeePaymentAccountResponse;
import com.example.ems.payroll.service.EmployeePaymentAccountService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/employees/{employeeId}/payment-account")
public class EmployeePaymentAccountController {

    private final EmployeePaymentAccountService accountService;

    public EmployeePaymentAccountController(EmployeePaymentAccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<EmployeePaymentAccountResponse>> registerAccount(
            @PathVariable Long employeeId,
            @Valid @RequestBody EmployeePaymentAccountRequest request) {
        EmployeePaymentAccountResponse response = accountService.registerAccount(employeeId, request);
        return ResponseEntity.ok(ApiResponse.success("Employee payment account registered successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<EmployeePaymentAccountResponse>> getAccount(
            @PathVariable Long employeeId) {
        EmployeePaymentAccountResponse response = accountService.getAccount(employeeId);
        return ResponseEntity.ok(ApiResponse.success("Employee payment account retrieved successfully", response));
    }
}
