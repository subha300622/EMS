package com.example.ems.payroll.controller;

import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.exception.AccessDeniedException;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.payroll.dto.PayslipDetailResponse;
import com.example.ems.payroll.service.PayslipService;
import com.example.ems.security.service.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class PayslipController {

    private final PayslipService payslipService;
    private final EmployeeRepository employeeRepository;
    private final JwtService jwtService;

    public PayslipController(PayslipService payslipService,
                             EmployeeRepository employeeRepository,
                             JwtService jwtService) {
        this.payslipService = payslipService;
        this.employeeRepository = employeeRepository;
        this.jwtService = jwtService;
    }

    private Employee resolveCurrentEmployee(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                String email = jwtService.getClaims(token).getSubject();
                if (email != null) {
                    return employeeRepository.findByEmail(email).orElse(null);
                }
            } catch (Exception ignored) {}
        }
        return null;
    }

    @GetMapping("/employees/me/payslips")
    public ResponseEntity<ApiResponse<List<PayslipDetailResponse>>> getMyPayslips(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Employee employee = resolveCurrentEmployee(authHeader);
        if (employee == null) {
            throw new AccessDeniedException("Unable to resolve authenticated employee.");
        }
        List<PayslipDetailResponse> response = payslipService.getMyPayslips(employee.getId());
        return ResponseEntity.ok(ApiResponse.success("My payslips retrieved successfully", response));
    }

    @GetMapping("/employees/me/payslips/{payslipId}")
    public ResponseEntity<ApiResponse<PayslipDetailResponse>> getMyPayslip(
            @PathVariable Long payslipId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Employee employee = resolveCurrentEmployee(authHeader);
        if (employee == null) {
            throw new AccessDeniedException("Unable to resolve authenticated employee.");
        }
        PayslipDetailResponse response = payslipService.getMyPayslip(employee.getId(), payslipId);
        return ResponseEntity.ok(ApiResponse.success("Payslip retrieved successfully", response));
    }

    @GetMapping("/employees/{employeeId}/payslips")
    public ResponseEntity<ApiResponse<List<PayslipDetailResponse>>> getEmployeePayslips(
            @PathVariable Long employeeId) {
        List<PayslipDetailResponse> response = payslipService.getMyPayslips(employeeId);
        return ResponseEntity.ok(ApiResponse.success("Employee payslips retrieved successfully", response));
    }
}
