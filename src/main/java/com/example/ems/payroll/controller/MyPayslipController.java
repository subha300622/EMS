package com.example.ems.payroll.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.payroll.dto.*;
import com.example.ems.payroll.service.MyPayslipService;
import com.example.ems.security.service.JwtService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/my-payslips")
@CrossOrigin("*")
@Tag(name = "Employee Self Service - Payroll")
public class MyPayslipController {

    @Autowired
    private MyPayslipService myPayslipService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private RoleService roleService;

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

    private boolean checkPermission(User user, String permission) {
        if (user == null) return false;
        return roleService.hasPermission(user.getWorkEmail(), permission)
                || roleService.hasPermission(user.getWorkEmail(), "payroll.manage")
                || roleService.hasPermission(user.getWorkEmail(), "payroll.read")
                || roleService.isSuperAdmin(user.getWorkEmail());
    }

    private ResponseEntity<ErrorResponse> unauthorizedResponse() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
    }

    private ResponseEntity<ErrorResponse> forbiddenResponse(String permission) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.error("Access Denied: Requires '" + permission + "' permission.", "AUTH_002"));
    }



    // 2. Get My Payslips History
    @GetMapping("/history")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getHistory(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @RequestParam(required = false) String financialYear,
            @RequestParam(required = false) String month,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,desc") String sort){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) unauthorizedResponse();
        }
        if (!checkPermission(currentUser, "payslip.self.read")) {
            return (ResponseEntity) forbiddenResponse("payslip.self.read");
        }

        try {
            MyPayslipHistoryResponse response = myPayslipService.getPayslipHistory(
                    currentUser.getWorkEmail(), financialYear, month, status, page, size, sort
            );
            return ResponseEntity.ok(ApiResponse.success("Payslip history retrieved successfully", response));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "PR_001"));
        }
    }

    // 3. Get Specific Payslip Details
    @GetMapping("/{id}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getDetails(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable("id") Long payslipId){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) unauthorizedResponse();
        }
        if (!checkPermission(currentUser, "payslip.self.read")) {
            return (ResponseEntity) forbiddenResponse("payslip.self.read");
        }

        try {
            MyPayslipDetailsResponse response = myPayslipService.getPayslipDetails(currentUser.getWorkEmail(), payslipId);
            return ResponseEntity.ok(ApiResponse.success("Payslip details retrieved successfully", response));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "PR_001"));
        }
    }

    // 4. Preview Specific Payslip
    @GetMapping("/{id}/preview")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> preview(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable("id") Long payslipId){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) unauthorizedResponse();
        }
        if (!checkPermission(currentUser, "payslip.self.preview")) {
            return (ResponseEntity) forbiddenResponse("payslip.self.preview");
        }

        try {
            MyPayslipPreviewResponse response = myPayslipService.previewPayslip(currentUser.getWorkEmail(), payslipId);
            return ResponseEntity.ok(ApiResponse.success("Payslip preview generated successfully", response));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "PR_001"));
        }
    }

    // 5. Download Specific Payslip PDF
    @GetMapping("/{id}/download")
    public ResponseEntity<?> download(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable("id") Long payslipId){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return unauthorizedResponse();
        }
        if (!checkPermission(currentUser, "payslip.self.download")) {
            return forbiddenResponse("payslip.self.download");
        }

        try {
            byte[] pdfBytes = myPayslipService.getPayslipPdf(currentUser.getWorkEmail(), payslipId);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "payslip-" + payslipId + ".pdf");
            headers.setContentLength(pdfBytes.length);
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "PR_001"));
        }
    }

    // 6. Get Annual Salary Statement
    @GetMapping("/annual-statement")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getAnnual(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @RequestParam(required = false) String financialYear){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) unauthorizedResponse();
        }
        if (!checkPermission(currentUser, "payslip.self.read")) {
            return (ResponseEntity) forbiddenResponse("payslip.self.read");
        }

        try {
            AnnualSalaryStatementResponse response = myPayslipService.getAnnualStatement(
                    currentUser.getWorkEmail(), financialYear
            );
            return ResponseEntity.ok(ApiResponse.success("Annual salary statement retrieved successfully", response));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "PR_001"));
        }
    }

    // 7. Download Annual Salary Statement PDF
    @GetMapping("/annual-statement/download")
    public ResponseEntity<?> downloadAnnual(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @RequestParam(required = false) String financialYear){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return unauthorizedResponse();
        }
        if (!checkPermission(currentUser, "payslip.self.download")) {
            return forbiddenResponse("payslip.self.download");
        }

        try {
            byte[] pdfBytes = myPayslipService.getAnnualStatementPdf(currentUser.getWorkEmail(), financialYear);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "annual-statement.pdf");
            headers.setContentLength(pdfBytes.length);
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "PR_001"));
        }
    }

    // 8. Get Salary Revision History
    @GetMapping("/salary-revisions")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getSalaryRevisions(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) unauthorizedResponse();
        }
        if (!checkPermission(currentUser, "payslip.self.read")) {
            return (ResponseEntity) forbiddenResponse("payslip.self.read");
        }

        try {
            MySalaryRevisionsResponse response = myPayslipService.getSalaryRevisionHistory(currentUser.getWorkEmail());
            return ResponseEntity.ok(ApiResponse.success("Salary revision history retrieved successfully", response));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "PR_001"));
        }
    }

    // 9. Get Tax Summary
    @GetMapping("/tax-summary")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getTax(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) unauthorizedResponse();
        }
        if (!checkPermission(currentUser, "payslip.self.read")) {
            return (ResponseEntity) forbiddenResponse("payslip.self.read");
        }

        try {
            TaxSummaryResponse response = myPayslipService.getTaxSummary(currentUser.getWorkEmail());
            return ResponseEntity.ok(ApiResponse.success("Tax summary retrieved successfully", response));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "PR_001"));
        }
    }

    // 10. Email Specific Payslip
    @PostMapping("/{id}/email")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> email(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable("id") Long payslipId,
            @Valid @RequestBody EmailPayslipRequest request){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) unauthorizedResponse();
        }
        if (!checkPermission(currentUser, "payslip.self.export")) {
            return (ResponseEntity) forbiddenResponse("payslip.self.export");
        }

        try {
            EmailPayslipResponse response = myPayslipService.emailPayslip(
                    currentUser.getWorkEmail(), payslipId, request.getEmail()
            );
            return ResponseEntity.ok(ApiResponse.success("Payslip sent via email successfully", response));
        } catch (Exception e) {
            return (ResponseEntity) ResponseEntity.badRequest()
                    .body(ErrorResponse.error(e.getMessage(), "PR_002"));
        }
    }

    // 11. Get Payroll Timeline
    @GetMapping("/timeline")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getTimeline(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) unauthorizedResponse();
        }
        if (!checkPermission(currentUser, "payslip.self.read")) {
            return (ResponseEntity) forbiddenResponse("payslip.self.read");
        }

        try {
            PayrollTimelineResponse response = myPayslipService.getPayrollTimeline(currentUser.getWorkEmail());
            return ResponseEntity.ok(ApiResponse.success("Payroll timeline retrieved successfully", response));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "PR_001"));
        }
    }
}
