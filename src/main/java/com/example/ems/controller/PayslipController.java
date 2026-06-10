package com.example.ems.controller;

import com.example.ems.dto.ApiResponse;
import com.example.ems.dto.ErrorResponse;
import com.example.ems.dto.PayrollGenerateRequest;
import com.example.ems.entity.Employee;
import com.example.ems.entity.Payslip;
import com.example.ems.entity.User;
import com.example.ems.repository.EmployeeRepository;
import com.example.ems.repository.UserRepository;
import com.example.ems.service.JwtService;
import com.example.ems.service.PayslipService;
import com.example.ems.service.RoleService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin("*")
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

    // ── 1. GET MY PAYSLIPS ───────────────────────────────────────────────────
    @GetMapping("/payslips/my")
    public ResponseEntity<?> getMyPayslips(
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

        return ResponseEntity.ok(ApiResponse.success("My payslips retrieved successfully", 
                payslipService.getPayslipsByEmployeeId(employee.getId())));
    }

    // ── 2. GET PAYSLIP BY ID ──────────────────────────────────────────────────
    @GetMapping("/payslips/{id}")
    public ResponseEntity<?> getPayslipById(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        Payslip ps = payslipService.getPayslipById(id).orElse(null);
        if (ps == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Payslip not found", "PS_001"));
        }

        Employee employee = resolveEmployee(currentUser);
        boolean isOwner = employee != null && ps.getPayroll().getEmployee().getId().equals(employee.getId());
        boolean hasAccess = isOwner 
                || roleService.hasPermission(currentUser.getWorkEmail(), "payroll.read")
                || roleService.hasPermission(currentUser.getWorkEmail(), "payroll.manage");

        if (!hasAccess) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: You cannot view this payslip.", "AUTH_002"));
        }

        return ResponseEntity.ok(ApiResponse.success("Payslip retrieved successfully", ps));
    }

    // ── 3. GENERATE PAYSLIPS (FINANCE / ADMIN) ────────────────────────────────
    @PostMapping("/payslips/generate")
    public ResponseEntity<?> generatePayslips(
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

        List<Payslip> generated = payslipService.generatePayslips(request.getMonth(), request.getYear());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Payslips generated successfully. Total generated: " + generated.size(), generated));
    }

    // ── 4. DOWNLOAD PAYSLIP (SIMULATED CSV) ──────────────────────────────────
    @GetMapping("/payslips/download/{id}")
    public ResponseEntity<?> downloadPayslip(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        Payslip ps = payslipService.getPayslipById(id).orElse(null);
        if (ps == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Payslip not found", "PS_001"));
        }

        Employee employee = resolveEmployee(currentUser);
        boolean isOwner = employee != null && ps.getPayroll().getEmployee().getId().equals(employee.getId());
        boolean hasAccess = isOwner 
                || roleService.hasPermission(currentUser.getWorkEmail(), "payroll.read")
                || roleService.hasPermission(currentUser.getWorkEmail(), "payroll.manage");

        if (!hasAccess) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
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

        return new ResponseEntity<>(data, headers, HttpStatus.OK);
    }
}
