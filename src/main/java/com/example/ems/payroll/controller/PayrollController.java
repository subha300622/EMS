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
import io.swagger.v3.oas.annotations.Operation;
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
    @Operation(summary = "Generate Payroll Runs", description = "Triggers the generation of payroll records for all active employees for the specified month and year.")
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
    @Operation(summary = "Get All Payroll Records", description = "Retrieves a listing of all payroll records in the system.")
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
    @Operation(summary = "Get Payroll Record by ID", description = "Retrieves detailed fields of a specific payroll record by its ID.")
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
    @Operation(summary = "Get Employee Payroll Records", description = "Retrieves all historical payroll records matching the specified employee ID.")
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
    @Operation(summary = "Update Payroll Record", description = "Updates specific salary, allowance, or deduction values on a payroll record.")
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
    @Operation(summary = "Review Payroll Record", description = "Changes the status of a specific payroll record to REVIEWED.")
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
    @Operation(summary = "Approve Payroll Record", description = "Approves a specific payroll record, moving its status to APPROVED.")
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
    @Operation(summary = "Process Payroll Record", description = "Executes payment processing for a specific payroll record, moving its status to PROCESSED.")
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
    @Operation(summary = "Get Payroll Dashboard", description = "Retrieves payroll dashboard analytics including cost trends and disbursement status summaries.")
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
    @Operation(summary = "Save Salary Structure", description = "Saves or updates the structured salary mapping configuration for an employee.")
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
    @Operation(summary = "Execute Payroll Run", description = "Processes the monthly payroll run calculations for a specific department.")
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
    @Operation(summary = "Get Payroll Calculation Preview", description = "Generates a calculation preview showing breakdowns of basic salary, allowance, taxes, and deductions.")
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
    @Operation(summary = "Approve Payroll Run Status", description = "Approves the generated payroll run status for disbursements.")
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
    @Operation(summary = "Disburse Salary Payments", description = "Executes the disbursement workflow to pay employees for a processed payroll run.")
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
    @Operation(summary = "Get Monthly Payroll History", description = "Retrieves historical logs of monthly payroll distributions.")
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
    @Operation(summary = "Get Tax Settings Configuration", description = "Retrieves the current system configuration tax slabs and settings.")
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

    @Operation(summary = "Update Tax Settings Configuration", description = "Updates the active tax configuration parameters in the system settings.")
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
    @Operation(summary = "Get Payroll Cost Trends", description = "Retrieves analytical cost timelines showing company-wide salary expenses over time.")
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

    @Operation(summary = "Get Department Payroll Costs", description = "Retrieves compiled payroll expenses categorized by department classifications.")
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
    @Operation(summary = "Get Monthly Payroll Report", description = "Generates a monthly payroll report showing all employee payslips details.")
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

    @Operation(summary = "Get Salary Register Report", description = "Retrieves the structured salary register mapping basic pay, deductions, and gross totals.")
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

    @Operation(summary = "Get Tax Deductions Report", description = "Retrieves tax deduction reports calculated and withheld during payroll runs.")
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

    @Operation(summary = "Get Disbursement Report", description = "Retrieves disbursement report logging bank transfers and payment release actions.")
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

    @Operation(summary = "Delete Payroll Record", description = "Deletes a specific payroll record from the database.")
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

    @Operation(summary = "Get Payroll Statistics", description = "Retrieves quick metrics and statistics regarding processed payroll runs.")
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

    @Operation(summary = "Get Payroll Reports Summary", description = "Retrieves payroll statistics summary reports.")
    @GetMapping("/payroll-runs/reports")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPayrollReports(
            @RequestHeader(value = "Authorization", required = false) String authHeader){
        return getPayrollStats(authHeader);
    }

    @Operation(summary = "Export Payroll to CSV", description = "Generates and downloads a CSV spreadsheet listing payroll records.")
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
