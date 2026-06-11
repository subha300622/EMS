package com.example.ems.payroll.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.payroll.dto.PayrollGenerateRequest;
import com.example.ems.payroll.dto.PayrollUpdateRequest;
import com.example.ems.payroll.entity.Payroll;
import com.example.ems.payroll.service.PayrollService;
import com.example.ems.security.service.JwtService;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.LinkedHashMap;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin("*")
public class PayrollController {

    @Autowired
    private PayrollService payrollService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private JwtService jwtService;

    private User resolveUser(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtService.validateAccessToken(token)) {
                String email = jwtService.getEmailFromToken(token);
                return userRepository.findByWorkEmail(email).orElse(null);
            }
        }
        return null;
    }

    private Employee resolveEmployee(User currentUser) {
        if (currentUser == null) return null;
        return employeeRepository.findByEmail(currentUser.getWorkEmail()).orElse(null);
    }

    // ── 1. GENERATE PAYROLL (FINANCE / ADMIN) ────────────────────────────────
    @PostMapping("/payroll-runs")
    public ResponseEntity<?> generatePayroll(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody @Valid PayrollGenerateRequest request) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "payroll.manage")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'payroll.manage' permission.", "AUTH_002"));
        }

        List<Payroll> generated = payrollService.generatePayroll(request.getMonth(), request.getYear());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Payroll generated successfully. Records created: " + generated.size(), generated));
    }

    // ── 2. GET MY PAYROLL HISTORY ────────────────────────────────────────────
    @GetMapping("/payroll-runs/my")
    public ResponseEntity<?> getMyPayroll(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        Employee employee = resolveEmployee(currentUser);
        if (employee == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.error("Employee profile not found for user", "EMP_002"));
        }

        return ResponseEntity.ok(ApiResponse.success("My payroll history retrieved successfully", 
                payrollService.getPayrollByEmployeeId(employee.getId())));
    }

    // ── 3. GET MY PAYROLL BY ID ──────────────────────────────────────────────
    @GetMapping("/payroll-runs/my/{id}")
    public ResponseEntity<?> getMyPayrollById(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        Payroll payroll = payrollService.getPayrollById(id).orElse(null);
        if (payroll == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Payroll record not found with ID: " + id, "PR_001"));
        }

        Employee employee = resolveEmployee(currentUser);
        if (employee == null || !payroll.getEmployee().getId().equals(employee.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: You cannot view this payroll record.", "AUTH_002"));
        }

        return ResponseEntity.ok(ApiResponse.success("Payroll record retrieved successfully", payroll));
    }

    // ── 4. GET ALL PAYROLL RECORDS (HR / FINANCE / ADMIN) ────────────────────
    @GetMapping("/payroll-runs")
    public ResponseEntity<?> getAllPayroll(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "payroll.read")
                && !roleService.hasPermission(currentUser.getWorkEmail(), "payroll.manage")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'payroll.read' or 'payroll.manage' permission.", "AUTH_002"));
        }

        return ResponseEntity.ok(ApiResponse.success("Payroll records retrieved successfully", 
                payrollService.getAllPayroll()));
    }

    // ── 5. GET PAYROLL BY ID (HR / FINANCE / ADMIN) ──────────────────────────
    @GetMapping("/payroll-runs/{id}")
    public ResponseEntity<?> getPayrollById(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "payroll.read")
                && !roleService.hasPermission(currentUser.getWorkEmail(), "payroll.manage")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'payroll.read' or 'payroll.manage' permission.", "AUTH_002"));
        }

        return payrollService.getPayrollById(id)
                .<ResponseEntity<?>>map(p -> ResponseEntity.ok(ApiResponse.success("Payroll record retrieved successfully", p)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ErrorResponse.error("Payroll record not found with ID: " + id, "PR_001")));
    }

    // ── 6. GET PAYROLL BY EMPLOYEE ID (HR / FINANCE / ADMIN) ──────────────────
    @GetMapping("/payroll-runs/employee/{employeeId}")
    public ResponseEntity<?> getPayrollByEmployee(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long employeeId) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "payroll.read")
                && !roleService.hasPermission(currentUser.getWorkEmail(), "payroll.manage")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'payroll.read' or 'payroll.manage' permission.", "AUTH_002"));
        }

        return ResponseEntity.ok(ApiResponse.success("Employee payroll records retrieved successfully", 
                payrollService.getPayrollByEmployeeId(employeeId)));
    }

    // ── 7. UPDATE PAYROLL (FINANCE / ADMIN) ──────────────────────────────────
    @PutMapping("/payroll-runs/{id}")
    public ResponseEntity<?> updatePayroll(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestBody @Valid PayrollUpdateRequest request) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "payroll.manage")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'payroll.manage' permission.", "AUTH_002"));
        }

        try {
            Payroll updated = payrollService.updatePayroll(id, request);
            return ResponseEntity.ok(ApiResponse.success("Payroll record updated successfully", updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "PR_002"));
        }
    }

    // ── 8. REVIEW PAYROLL ────────────────────────────────────────────────────
    @PutMapping("/payroll-runs/{id}/review")
    public ResponseEntity<?> reviewPayroll(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "payroll.manage")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'payroll.manage' permission.", "AUTH_002"));
        }

        try {
            Payroll updated = payrollService.reviewPayroll(id);
            return ResponseEntity.ok(ApiResponse.success("Payroll status updated to REVIEWED", updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "PR_003"));
        }
    }

    // ── 9. APPROVE PAYROLL ───────────────────────────────────────────────────
    @PatchMapping("/payroll-runs/{id}/approve")
    public ResponseEntity<?> approvePayroll(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "payroll.manage")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'payroll.manage' permission.", "AUTH_002"));
        }

        try {
            Payroll updated = payrollService.approvePayroll(id);
            return ResponseEntity.ok(ApiResponse.success("Payroll status updated to APPROVED", updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "PR_004"));
        }
    }

    // ── 10. PROCESS PAYROLL ──────────────────────────────────────────────────
    @PutMapping("/payroll-runs/{id}/process")
    public ResponseEntity<?> processPayroll(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "payroll.manage")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'payroll.manage' permission.", "AUTH_002"));
        }

        try {
            Payroll updated = payrollService.processPayroll(id);
            return ResponseEntity.ok(ApiResponse.success("Payroll status updated to PROCESSED", updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "PR_005"));
        }
    }

    // ── 10b. BATCH PROCESS PAYROLL ───────────────────────────────────────────
    @PostMapping("/payroll-runs/process")
    public ResponseEntity<?> processPayrollBatch(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody List<Long> ids) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "payroll.manage")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'payroll.manage' permission.", "AUTH_002"));
        }

        List<Payroll> processed = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        for (Long id : ids) {
            try {
                processed.add(payrollService.processPayroll(id));
            } catch (Exception e) {
                errors.add("ID " + id + ": " + e.getMessage());
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("processedCount", processed.size());
        result.put("processedRecords", processed);
        result.put("errors", errors);

        return ResponseEntity.ok(ApiResponse.success("Batch payroll processing completed", result));
    }

    // ── 11. PAY PAYROLL ──────────────────────────────────────────────────────
    @PatchMapping("/payroll-runs/{id}/payment")
    public ResponseEntity<?> payPayroll(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "payroll.manage")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'payroll.manage' permission.", "AUTH_002"));
        }

        try {
            Payroll updated = payrollService.payPayroll(id);
            return ResponseEntity.ok(ApiResponse.success("Payroll status updated to PAID", updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "PR_006"));
        }
    }

    // ── 12. GET STATS (HR / FINANCE / ADMIN) ─────────────────────────────────
    @GetMapping("/payroll-runs/stats")
    public ResponseEntity<?> getPayrollStats(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "payroll.read")
                && !roleService.hasPermission(currentUser.getWorkEmail(), "payroll.manage")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'payroll.read' or 'payroll.manage' permission.", "AUTH_002"));
        }

        return ResponseEntity.ok(ApiResponse.success("Payroll statistics retrieved successfully", 
                payrollService.getPayrollStats()));
    }

    @DeleteMapping("/payroll-runs/{id}")
    public ResponseEntity<?> deletePayroll(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "payroll.manage")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'payroll.manage' permission.", "AUTH_002"));
        }

        boolean deleted = payrollService.deletePayroll(id);
        if (deleted) {
            return ResponseEntity.ok(ApiResponse.success("Payroll record deleted successfully"));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Payroll record not found with ID: " + id, "PR_001"));
        }
    }

    @GetMapping("/payroll-runs/reports")
    public ResponseEntity<?> getPayrollReports(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return getPayrollStats(authHeader);
    }

    @GetMapping("/payroll-runs/export")
    public ResponseEntity<?> exportPayroll(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "payroll.read")
                && !roleService.hasPermission(currentUser.getWorkEmail(), "payroll.manage")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'payroll.read' or 'payroll.manage' permission.", "AUTH_002"));
        }

        List<Payroll> list = payrollService.getAllPayroll();
        StringBuilder csv = new StringBuilder("ID,Employee ID,Employee Name,Month,Year,Basic Salary,Allowances,Deductions,Net Pay,Status\n");
        for (Payroll p : list) {
            csv.append(p.getId()).append(",")
               .append(p.getEmployee().getEmployeeId() != null ? p.getEmployee().getEmployeeId() : "").append(",")
               .append(p.getEmployee().getFullName()).append(",")
               .append(p.getMonth()).append(",")
               .append(p.getYear()).append(",")
               .append(p.getBasicSalary()).append(",")
               .append(p.getAllowances()).append(",")
               .append(p.getDeductions()).append(",")
               .append(p.getNetPay()).append(",")
               .append(p.getStatus()).append("\n");
        }

        byte[] data = csv.toString().getBytes();
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "payroll_export.csv");
        headers.setContentLength(data.length);

        return new ResponseEntity<>(data, headers, HttpStatus.OK);
    }
}
