package com.example.ems.payroll.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.payroll.dto.PayrollGenerateRequest;
import com.example.ems.payroll.dto.PayrollUpdateRequest;
import com.example.ems.payroll.dto.SalaryStructureRequest;
import com.example.ems.payroll.entity.Payroll;
import com.example.ems.payroll.entity.SalaryStructure;
import com.example.ems.payroll.service.PayrollService;
import com.example.ems.security.service.JwtService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin("*")
@Tag(name = "Payroll Processing")
public class PayrollController {

    @Autowired
    private PayrollService payrollService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private UserRepository userRepository;

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

    // ── 1. GENERATE PAYROLL (FINANCE / ADMIN) ────────────────────────────────
    @PostMapping("/payroll-runs")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> generatePayroll(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody @Valid PayrollGenerateRequest request){

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "payroll.manage")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'payroll.manage' permission.", "AUTH_002"));
        }

        List<Payroll> generated = payrollService.generatePayroll(request.getMonth(), request.getYear());
        return (ResponseEntity) ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Payroll generated successfully. Records created: " + generated.size(),
                        generated));
    }

    // ── 4. GET ALL PAYROLL RECORDS (HR / FINANCE / ADMIN) ────────────────────
    @GetMapping("/payroll-runs")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<List<Payroll>>> getAllPayroll(
            @RequestHeader(value = "Authorization", required = false) String authHeader){

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "payroll.read")
                && !roleService.hasPermission(currentUser.getWorkEmail(), "payroll.manage")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'payroll.read' or 'payroll.manage' permission.",
                            "AUTH_002"));
        }

        return ResponseEntity.ok(ApiResponse.success("Payroll records retrieved successfully",
                payrollService.getAllPayroll()));
    }

    // ── 5. GET PAYROLL BY ID (HR / FINANCE / ADMIN) ──────────────────────────
    @GetMapping("/payroll-runs/{id}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getPayrollById(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id){

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "payroll.read")
                && !roleService.hasPermission(currentUser.getWorkEmail(), "payroll.manage")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'payroll.read' or 'payroll.manage' permission.",
                            "AUTH_002"));
        }

        return (ResponseEntity) payrollService.getPayrollById(id)
                .<ResponseEntity<?>>map(
                        p -> ResponseEntity.ok(ApiResponse.success("Payroll record retrieved successfully", p)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ErrorResponse.error("Payroll record not found with ID: " + id, "PR_001")));
    }

    // ── 6. GET PAYROLL BY EMPLOYEE ID (HR / FINANCE / ADMIN) ──────────────────
    @GetMapping("/payroll-runs/employee/{employeeId}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<List<Payroll>>> getPayrollByEmployee(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long employeeId){

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "payroll.read")
                && !roleService.hasPermission(currentUser.getWorkEmail(), "payroll.manage")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'payroll.read' or 'payroll.manage' permission.",
                            "AUTH_002"));
        }

        return ResponseEntity.ok(ApiResponse.success("Employee payroll records retrieved successfully",
                payrollService.getPayrollByEmployeeId(employeeId)));
    }

    // ── 7. UPDATE PAYROLL (FINANCE / ADMIN) ──────────────────────────────────
    @PutMapping("/payroll-runs/{id}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> updatePayroll(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestBody @Valid PayrollUpdateRequest request){

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "payroll.manage")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'payroll.manage' permission.", "AUTH_002"));
        }

        try {
            Payroll updated = payrollService.updatePayroll(id, request);
            return ResponseEntity.ok(ApiResponse.success("Payroll record updated successfully", updated));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "PR_002"));
        }
    }

    // ── 8. REVIEW PAYROLL ────────────────────────────────────────────────────
    @PutMapping("/payroll-runs/{id}/review")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> reviewPayroll(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id){

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "payroll.manage")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'payroll.manage' permission.", "AUTH_002"));
        }

        try {
            Payroll updated = payrollService.reviewPayroll(id);
            return ResponseEntity.ok(ApiResponse.success("Payroll status updated to REVIEWED", updated));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "PR_003"));
        }
    }

    // ── 9. APPROVE PAYROLL ───────────────────────────────────────────────────
    @PatchMapping("/payroll-runs/{id}/approve")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> approvePayroll(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id){

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "payroll.manage")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'payroll.manage' permission.", "AUTH_002"));
        }

        try {
            Payroll updated = payrollService.approvePayroll(id);
            return ResponseEntity.ok(ApiResponse.success("Payroll status updated to APPROVED", updated));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "PR_004"));
        }
    }

    // ── 10. PROCESS PAYROLL ──────────────────────────────────────────────────
    @PostMapping("/payroll-runs/{id}/process")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> processPayroll(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id){

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "payroll.manage")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'payroll.manage' permission.", "AUTH_002"));
        }

        try {
            Payroll updated = payrollService.processPayroll(id);
            return ResponseEntity.ok(ApiResponse.success("Payroll status updated to PROCESSED", updated));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "PR_005"));
        }
    }

    // ── NEW STRUCTURED PAYROLL ENDPOINTS ──────────────────────────────────────

    // 1. Dashboard
    @GetMapping("/payroll/dashboard")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPayrollDashboard(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        return ResponseEntity.ok(ApiResponse.success("Payroll dashboard retrieved successfully",
                payrollService.getPayrollDashboard()));
    }

    // 2. Salary Structure setup
    @PostMapping("/payroll/salary-structures")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> saveSalaryStructure(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody SalaryStructureRequest request) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        try {
            SalaryStructure ss = payrollService.saveSalaryStructure(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Salary structure saved successfully", ss));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "PR_007"));
        }
    }

    // 5. Process Payroll
    @PostMapping("/payroll/process")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Map<String, Object>>> processPayrollRun(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, Object> requestBody) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        String month = (String) requestBody.get("month");
        Number deptId = (Number) requestBody.get("departmentId");
        Long departmentId = deptId != null ? deptId.longValue() : null;

        Map<String, Object> result = payrollService.processPayrollRun(month, departmentId);
        return ResponseEntity.ok(ApiResponse.success("Payroll processed successfully", result));
    }

    // 6. Payroll Calculation
    @GetMapping("/payroll/{employeeId}/calculation")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCalculationPreview(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long employeeId) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        return ResponseEntity.ok(ApiResponse.success("Payroll calculation preview generated",
                payrollService.calculatePreview(employeeId)));
    }

    // 7. Payroll Approval
    @PostMapping("/payroll/{id}/approve")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> approvePayrollRun(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        try {
            Payroll p = payrollService.approvePayroll(id);
            return ResponseEntity.ok(ApiResponse.success("Payroll run approved successfully", p));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "PR_004"));
        }
    }


    // 9. Salary Disbursement
    @PostMapping("/payroll/disburse")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Map<String, Object>>> disburseSalary(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, Long> requestBody) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        Long payrollRunId = requestBody.get("payrollRunId");
        try {
            Map<String, Object> result = payrollService.disbursePayment(payrollRunId);
            return ResponseEntity.ok(ApiResponse.success("Salary disbursement completed", result));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "PR_006"));
        }
    }

    // 10. Payroll History
    @GetMapping("/payroll/history")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getPayrollHistory(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        return ResponseEntity.ok(ApiResponse.success("Payroll history retrieved successfully",
                payrollService.getMonthlyReport()));
    }


    // 12. Tax Configuration
    @GetMapping("/payroll/taxes")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTaxConfiguration(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        return ResponseEntity.ok(ApiResponse.success("Tax configuration retrieved successfully",
                payrollService.getTaxSettings()));
    }

    @PutMapping("/payroll/taxes")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateTaxConfiguration(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, Object> requestBody) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        return ResponseEntity.ok(ApiResponse.success("Tax configuration updated successfully",
                payrollService.updateTaxSettings(requestBody)));
    }

    // 13. Payroll Analytics
    @GetMapping("/payroll/analytics/cost-trend")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCostTrend(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        return ResponseEntity.ok(ApiResponse.success("Cost trend retrieved successfully",
                payrollService.getCostTrend()));
    }

    @GetMapping("/payroll/analytics/department-cost")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDepartmentCost(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        return ResponseEntity.ok(ApiResponse.success("Department payroll costs retrieved successfully",
                payrollService.getDepartmentCost()));
    }

    // 14. Payroll Reports
    @GetMapping("/payroll/reports/monthly")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getMonthlyPayrollReport(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        return ResponseEntity.ok(ApiResponse.success("Monthly report retrieved", payrollService.getMonthlyReport()));
    }

    @GetMapping("/payroll/reports/salary-register")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getSalaryRegister(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        return ResponseEntity.ok(ApiResponse.success("Salary register retrieved", payrollService.getSalaryRegister()));
    }

    @GetMapping("/payroll/reports/tax")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getTaxReport(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        return ResponseEntity.ok(ApiResponse.success("Tax report retrieved", payrollService.getTaxReport()));
    }

    @GetMapping("/payroll/reports/disbursement")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getDisbursementReport(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        return ResponseEntity.ok(ApiResponse.success("Disbursement report retrieved", payrollService.getDisbursementReport()));
    }

    @DeleteMapping("/payroll-runs/{id}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> deletePayroll(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id){

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "payroll.manage")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'payroll.manage' permission.", "AUTH_002"));
        }

        boolean deleted = payrollService.deletePayroll(id);
        if (deleted) {
            return ResponseEntity.ok(ApiResponse.success("Payroll record deleted successfully"));
        } else {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Payroll record not found with ID: " + id, "PR_001"));
        }
    }

    @GetMapping("/payroll-runs/stats")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPayrollStats(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        return ResponseEntity.ok(ApiResponse.success("Payroll stats retrieved", payrollService.getPayrollStats()));
    }

    @GetMapping("/payroll-runs/reports")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPayrollReports(
            @RequestHeader(value = "Authorization", required = false) String authHeader){
        return getPayrollStats(authHeader);
    }

    @GetMapping("/payroll-runs/export")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<byte[]> exportPayroll(
            @RequestHeader(value = "Authorization", required = false) String authHeader){

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "payroll.read")
                && !roleService.hasPermission(currentUser.getWorkEmail(), "payroll.manage")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'payroll.read' or 'payroll.manage' permission.",
                            "AUTH_002"));
        }

        List<Payroll> list = payrollService.getAllPayroll();
        StringBuilder csv = new StringBuilder(
                "ID,Employee ID,Employee Name,Month,Year,Basic Salary,Allowances,Deductions,Net Pay,Status\n");
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

        return (ResponseEntity) new ResponseEntity<>(data, headers, HttpStatus.OK);
    }
}
