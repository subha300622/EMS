package com.example.ems.payroll.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.payroll.dto.PayrollGenerateRequest;
import com.example.ems.payroll.entity.Payslip;
import com.example.ems.payroll.service.PayslipService;
import com.example.ems.security.service.JwtService;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin("*")
@Tag(name = "Payslip Management")
public class PayslipController {

    @Autowired
    private PayslipService payslipService;

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


    // ── 2. GET PAYSLIP BY ID ──────────────────────────────────────────────────
    @Operation(summary = "Get Payslip Details", description = "Retrieves the detail fields of a specific employee payslip by ID.")
    @GetMapping("/payslips/{id}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getPayslipById(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id){

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        Payslip ps = payslipService.getPayslipById(id).orElse(null);
        if (ps == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Payslip not found", "PS_001"));
        }

        Employee employee = resolveEmployee(currentUser);
        boolean isOwner = employee != null && ps.getPayroll().getEmployee().getId().equals(employee.getId());
        boolean hasAccess = isOwner 
                || roleService.hasPermission(currentUser.getWorkEmail(), "payroll.read")
                || roleService.hasPermission(currentUser.getWorkEmail(), "payroll.manage");

        if (!hasAccess) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: You cannot view this payslip.", "AUTH_002"));
        }

        return ResponseEntity.ok(ApiResponse.success("Payslip retrieved successfully", ps));
    }

    // ── 3. GENERATE PAYSLIPS (FINANCE / ADMIN) ────────────────────────────────
    @Operation(summary = "Generate Employee Payslips", description = "Triggers bulk generation of employee payslips for the specified month and year.")
    @PostMapping("/payslips/generate")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> generatePayslips(
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

        List<Payslip> generated = payslipService.generatePayslips(request.getMonth(), request.getYear());
        return (ResponseEntity) ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Payslips generated successfully. Total generated: " + generated.size(), generated));
    }

    // ── 4. DOWNLOAD PAYSLIP (SIMULATED CSV) ──────────────────────────────────
    @Operation(summary = "Download Payslip", description = "Downloads a simulated CSV format payslip document for the specified payslip ID.")
    @GetMapping("/payslips/download/{id}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<byte[]> downloadPayslip(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id){

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        Payslip ps = payslipService.getPayslipById(id).orElse(null);
        if (ps == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Payslip not found", "PS_001"));
        }

        Employee employee = resolveEmployee(currentUser);
        boolean isOwner = employee != null && ps.getPayroll().getEmployee().getId().equals(employee.getId());
        boolean hasAccess = isOwner 
                || roleService.hasPermission(currentUser.getWorkEmail(), "payroll.read")
                || roleService.hasPermission(currentUser.getWorkEmail(), "payroll.manage");

        if (!hasAccess) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: You cannot download this payslip.", "AUTH_002"));
        }

        // Generate simulated CSV file content
        StringBuilder csv = new StringBuilder();
        csv.append("Payslip Number,").append(ps.getPayslipNumber()).append("\n");
        csv.append("Employee Name,").append(ps.getPayroll().getEmployee().getFullName()).append("\n");
        csv.append("Period,").append(ps.getPayroll().getMonth()).append("/").append(ps.getPayroll().getYear()).append("\n");
        csv.append("Basic Salary,").append(ps.getPayroll().getBasicSalary()).append("\n");
        csv.append("Allowances,").append(ps.getPayroll().getAllowances()).append("\n");
        csv.append("Deductions,").append(ps.getPayroll().getDeductions()).append("\n");
        csv.append("Net Pay,").append(ps.getPayroll().getNetPay()).append("\n");

        byte[] data = csv.toString().getBytes();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "payslip-" + id + ".csv");
        headers.setContentLength(data.length);

        return (ResponseEntity) new ResponseEntity<>(data, headers, HttpStatus.OK);
    }

    @Operation(summary = "Delete Payslip", description = "Deletes a specific payslip entry from records.")
    @DeleteMapping("/payslips/{id}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> deletePayslip(
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

        boolean deleted = payslipService.deletePayslip(id);
        if (deleted) {
            return ResponseEntity.ok(ApiResponse.success("Payslip deleted successfully"));
        } else {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Payslip not found with ID: " + id, "PS_001"));
        }
    }

    @Operation(summary = "Export Payslips list to CSV", description = "Generates and downloads a CSV spreadsheet listing all payslips records.")
    @GetMapping("/payslips/export")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<byte[]> exportPayslips(
            @RequestHeader(value = "Authorization", required = false) String authHeader){

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "payroll.read")
                && !roleService.hasPermission(currentUser.getWorkEmail(), "payroll.manage")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'payroll.read' or 'payroll.manage' permission.", "AUTH_002"));
        }

        List<Payslip> list = payslipService.getAllPayslips();
        StringBuilder csv = new StringBuilder("ID,Payslip Number,Employee ID,Employee Name,Month,Year,Net Pay,Generated At\n");
        for (Payslip p : list) {
            csv.append(p.getId()).append(",")
               .append(p.getPayslipNumber()).append(",")
               .append(p.getPayroll().getEmployee().getEmployeeId() != null ? p.getPayroll().getEmployee().getEmployeeId() : "").append(",")
               .append(p.getPayroll().getEmployee().getFullName()).append(",")
               .append(p.getPayroll().getMonth()).append(",")
               .append(p.getPayroll().getYear()).append(",")
               .append(p.getPayroll().getNetPay()).append(",")
               .append(p.getGeneratedAt()).append("\n");
        }

        byte[] data = csv.toString().getBytes();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "payslips_export.csv");
        headers.setContentLength(data.length);

        return (ResponseEntity) new ResponseEntity<>(data, headers, HttpStatus.OK);
    }
}
