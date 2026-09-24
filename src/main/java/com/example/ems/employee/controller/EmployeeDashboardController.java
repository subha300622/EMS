package com.example.ems.employee.controller;

import com.example.ems.attendance.dto.AttendanceCoreResponse;
import com.example.ems.auth.service.PermissionRegistry;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.employee.dto.MyDocumentDetailsResponse;
import com.example.ems.employee.dto.MyDocumentUploadResponse;
import com.example.ems.employee.dto.dashboard.*;
import com.example.ems.employee.service.EmployeeDashboardService;
import com.example.ems.leave.dto.LeaveRequest;
import com.example.ems.leave.entity.Leave;
import com.example.ems.performance.dto.EnterprisePerformanceReviewResponse;
import com.example.ems.performance.dto.EnterpriseSelfReviewRequest;
import com.example.ems.security.service.PermissionCheckService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/v1/employee", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin("*")
@Tag(name = "Employee Dashboard & Self-Service", description = "Employee self-service dashboard, action center, and drill-down APIs")
public class EmployeeDashboardController {

    @Autowired
    private EmployeeDashboardService dashboardService;

    @Autowired
    private PermissionCheckService permissionCheckService;

    private boolean isNotAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName());
    }

    // == 1. MAIN DASHBOARD =====================================================

    @Operation(summary = "Get Employee Dashboard", description = "Aggregated employee dashboard providing attendance, leave balances, compensation CTC, performance summary, and action center.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dashboard retrieved successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = EmployeeDashboardResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden - Missing dashboard permission",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PreAuthorize("hasAuthority('employee.dashboard.read') or hasAuthority('employee.dashboard.view')")
    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard() {
        if (isNotAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Full authentication is required to access employee dashboard.", "AUTH_014"));
        }
        permissionCheckService.requireAnyPermission(PermissionRegistry.EMPLOYEE_DASHBOARD_READ, PermissionRegistry.EMPLOYEE_DASHBOARD_VIEW);
        EmployeeDashboardResponse response = dashboardService.getDashboard();
        return ResponseEntity.ok(response);
    }

    // == 2. ATTENDANCE APIS ====================================================

    @Operation(summary = "Get Attendance Summary", description = "Monthly attendance summary with working days, present days, percentage, and trend direction.")
    @PreAuthorize("hasAuthority('employee.attendance.read') or hasAuthority('attendance.self.read') or hasAuthority('employee.dashboard.read')")
    @GetMapping("/attendance/summary")
    public ResponseEntity<?> getAttendanceSummary() {
        if (isNotAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Full authentication is required.", "AUTH_014"));
        }
        permissionCheckService.requireAnyPermission(
                PermissionRegistry.EMPLOYEE_ATTENDANCE_READ,
                PermissionRegistry.ATTENDANCE_SELF_READ,
                PermissionRegistry.EMPLOYEE_DASHBOARD_READ
        );
        EmployeeAttendanceDetailSummaryDto response = dashboardService.getAttendanceSummary();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get Attendance History", description = "Retrieves attendance records for the authenticated employee for a specified month.")
    @PreAuthorize("hasAuthority('employee.attendance.read') or hasAuthority('attendance.self.read') or hasAuthority('employee.dashboard.read')")
    @GetMapping("/attendance")
    public ResponseEntity<?> getAttendanceHistory(@RequestParam(value = "month", required = false) String month) {
        if (isNotAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Full authentication is required.", "AUTH_014"));
        }
        permissionCheckService.requireAnyPermission(
                PermissionRegistry.EMPLOYEE_ATTENDANCE_READ,
                PermissionRegistry.ATTENDANCE_SELF_READ,
                PermissionRegistry.EMPLOYEE_DASHBOARD_READ
        );
        List<AttendanceCoreResponse> response = dashboardService.getAttendanceHistory(month);
        return ResponseEntity.ok(response);
    }

    // == 3. LEAVE BALANCE & LEAVE APIS =========================================

    @Operation(summary = "Get Leave Balance", description = "Breakdown of available leave balances by type according to canonical leave rules.")
    @PreAuthorize("hasAuthority('employee.leave.read') or hasAuthority('leave.self.read') or hasAuthority('employee.dashboard.read')")
    @GetMapping("/leave-balance")
    public ResponseEntity<?> getLeaveBalance() {
        if (isNotAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Full authentication is required.", "AUTH_014"));
        }
        permissionCheckService.requireAnyPermission(
                PermissionRegistry.EMPLOYEE_LEAVE_READ,
                PermissionRegistry.LEAVE_SELF_READ,
                PermissionRegistry.EMPLOYEE_DASHBOARD_READ
        );
        EmployeeLeaveBalanceDetailDto response = dashboardService.getLeaveBalance();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get My Leaves", description = "Retrieves leave requests belonging to the authenticated employee.")
    @PreAuthorize("hasAuthority('employee.leave.read') or hasAuthority('leave.self.read')")
    @GetMapping("/leave")
    public ResponseEntity<?> getMyLeaves() {
        if (isNotAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Full authentication is required.", "AUTH_014"));
        }
        permissionCheckService.requireAnyPermission(PermissionRegistry.EMPLOYEE_LEAVE_READ, PermissionRegistry.LEAVE_SELF_READ);
        List<Leave> response = dashboardService.getMyLeaves();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get My Leave by ID", description = "Retrieves a single leave request by ID, strictly verifying ownership.")
    @PreAuthorize("hasAuthority('employee.leave.read') or hasAuthority('leave.self.read')")
    @GetMapping("/leave/{id}")
    public ResponseEntity<?> getMyLeaveById(@PathVariable("id") Long id) {
        if (isNotAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Full authentication is required.", "AUTH_014"));
        }
        permissionCheckService.requireAnyPermission(PermissionRegistry.EMPLOYEE_LEAVE_READ, PermissionRegistry.LEAVE_SELF_READ);
        Leave response = dashboardService.getMyLeaveById(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Apply Leave", description = "Submits a leave request for the authenticated employee.")
    @PreAuthorize("hasAuthority('employee.leave.create') or hasAuthority('leave.self.create')")
    @PostMapping("/leave")
    public ResponseEntity<?> applyMyLeave(@RequestBody @Valid LeaveRequest request) {
        if (isNotAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Full authentication is required.", "AUTH_014"));
        }
        permissionCheckService.requireAnyPermission(PermissionRegistry.EMPLOYEE_LEAVE_CREATE, "leave.self.create");
        Leave response = dashboardService.applyMyLeave(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // == 4. COMPENSATION API ===================================================

    @Operation(summary = "Get Current CTC Compensation", description = "Retrieves authorized salary and CTC compensation information for the employee.")
    @PreAuthorize("hasAuthority('employee.compensation.read') or hasAuthority('employee.payslip.read') or hasAuthority('employee.dashboard.read')")
    @GetMapping("/compensation/current")
    public ResponseEntity<?> getCurrentCompensation() {
        if (isNotAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Full authentication is required.", "AUTH_014"));
        }
        permissionCheckService.requireAnyPermission(
                PermissionRegistry.EMPLOYEE_COMPENSATION_READ,
                "employee.payslip.read",
                PermissionRegistry.EMPLOYEE_DASHBOARD_READ
        );
        EmployeeCompensationSummaryDto response = dashboardService.getCurrentCompensation();
        return ResponseEntity.ok(response);
    }

    // == 5. PERFORMANCE APIS ===================================================

    @Operation(summary = "Get Performance Summary", description = "Retrieves the latest official performance appraisal summary.")
    @PreAuthorize("hasAuthority('employee.performance.read') or hasAuthority('performance.self.read') or hasAuthority('employee.dashboard.read')")
    @GetMapping("/performance/summary")
    public ResponseEntity<?> getPerformanceSummary() {
        if (isNotAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Full authentication is required.", "AUTH_014"));
        }
        permissionCheckService.requireAnyPermission(
                "employee.performance.read",
                PermissionRegistry.PERFORMANCE_SELF_READ,
                PermissionRegistry.EMPLOYEE_DASHBOARD_READ
        );
        EmployeePerformanceSummaryDto response = dashboardService.getPerformanceSummary();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get My Performance Reviews", description = "Retrieves all performance reviews belonging to the employee.")
    @PreAuthorize("hasAuthority('employee.performance.read') or hasAuthority('performance.self.read')")
    @GetMapping({"/performance", "/performance/self-reviews"})
    public ResponseEntity<?> getMyPerformanceReviews() {
        if (isNotAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Full authentication is required.", "AUTH_014"));
        }
        permissionCheckService.requireAnyPermission("employee.performance.read", PermissionRegistry.PERFORMANCE_SELF_READ);
        List<EnterprisePerformanceReviewResponse> response = dashboardService.getMyPerformanceReviews();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get My Self-Review by ID", description = "Retrieves a single performance review/self-review by ID, verifying ownership.")
    @PreAuthorize("hasAuthority('employee.performance.read') or hasAuthority('performance.self.read')")
    @GetMapping("/performance/self-reviews/{id}")
    public ResponseEntity<?> getMyPerformanceReviewById(@PathVariable("id") Long id) {
        if (isNotAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Full authentication is required.", "AUTH_014"));
        }
        permissionCheckService.requireAnyPermission("employee.performance.read", PermissionRegistry.PERFORMANCE_SELF_READ);
        EnterprisePerformanceReviewResponse response = dashboardService.getMyPerformanceReviewById(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Submit Self-Review", description = "Submits an employee self-evaluation review within an active appraisal cycle.")
    @PreAuthorize("hasAuthority('employee.performance.self-review.submit') or hasAuthority('employee.performance.read')")
    @PostMapping("/performance/self-reviews/{id}/submit")
    public ResponseEntity<?> submitMySelfReview(@PathVariable("id") Long id,
                                                @RequestBody @Valid EnterpriseSelfReviewRequest request) {
        if (isNotAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Full authentication is required.", "AUTH_014"));
        }
        permissionCheckService.requireAnyPermission("employee.performance.self-review.submit", "employee.performance.read");
        EnterprisePerformanceReviewResponse response = dashboardService.submitMySelfReview(id, request);
        return ResponseEntity.ok(response);
    }

    // == 6. DOCUMENTS APIS =====================================================

    @Operation(summary = "Get My Documents", description = "Retrieves uploaded and pending documents overview for authenticated employee.")
    @PreAuthorize("hasAuthority('employee.document.read') or hasAuthority('document.self.read')")
    @GetMapping("/documents")
    public ResponseEntity<?> getMyDocuments() {
        if (isNotAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Full authentication is required.", "AUTH_014"));
        }
        permissionCheckService.requireAnyPermission("employee.document.read", "document.self.read");
        Object response = dashboardService.getMyDocuments();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Upload Document", description = "Uploads a document file for the authenticated employee.")
    @PreAuthorize("hasAuthority('employee.document.upload') or hasAuthority('document.self.upload')")
    @PostMapping(value = "/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadMyDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam("documentType") String documentType,
            @RequestParam(value = "documentNumber", required = false) String documentNumber,
            @RequestParam(value = "issuedDate", required = false) String issuedDate,
            @RequestParam(value = "expiryDate", required = false) String expiryDate,
            @RequestParam(value = "remarks", required = false) String remarks) {
        if (isNotAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Full authentication is required.", "AUTH_014"));
        }
        permissionCheckService.requireAnyPermission("employee.document.upload", "document.self.upload");
        MyDocumentUploadResponse response = dashboardService.uploadMyDocument(
                file, categoryId, documentType, documentNumber, issuedDate, expiryDate, remarks);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get Document Details", description = "Retrieves document metadata, strictly enforcing employee ownership.")
    @PreAuthorize("hasAuthority('employee.document.read') or hasAuthority('document.self.read')")
    @GetMapping("/documents/{id}")
    public ResponseEntity<?> getMyDocumentDetails(@PathVariable("id") Long id) {
        if (isNotAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Full authentication is required.", "AUTH_014"));
        }
        permissionCheckService.requireAnyPermission("employee.document.read", "document.self.read");
        MyDocumentDetailsResponse response = dashboardService.getMyDocumentDetails(id);
        return ResponseEntity.ok(response);
    }

    // == 7. TRAINING APIS ======================================================

    @Operation(summary = "Get My Trainings", description = "Retrieves assigned courses and training progress for the employee.")
    @PreAuthorize("hasAuthority('employee.training.read') or hasAuthority('employee.dashboard.read')")
    @GetMapping("/training")
    public ResponseEntity<?> getMyTrainings() {
        if (isNotAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Full authentication is required.", "AUTH_014"));
        }
        permissionCheckService.requireAnyPermission("employee.training.read", PermissionRegistry.EMPLOYEE_DASHBOARD_READ);
        List<Map<String, Object>> response = dashboardService.getMyTrainings();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get My Training Details", description = "Retrieves assigned course details, validating employee assignment.")
    @PreAuthorize("hasAuthority('employee.training.read') or hasAuthority('employee.dashboard.read')")
    @GetMapping("/training/{id}")
    public ResponseEntity<?> getMyTrainingDetails(@PathVariable("id") Long id) {
        if (isNotAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Full authentication is required.", "AUTH_014"));
        }
        permissionCheckService.requireAnyPermission("employee.training.read", PermissionRegistry.EMPLOYEE_DASHBOARD_READ);
        Map<String, Object> response = dashboardService.getMyTrainingDetails(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Complete Training", description = "Marks an assigned training module as completed for the employee.")
    @PreAuthorize("hasAuthority('employee.training.complete') or hasAuthority('employee.training.read')")
    @PostMapping("/training/{id}/complete")
    public ResponseEntity<?> completeMyTraining(@PathVariable("id") Long id) {
        if (isNotAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Full authentication is required.", "AUTH_014"));
        }
        permissionCheckService.requireAnyPermission("employee.training.complete", "employee.training.read");
        Map<String, Object> response = dashboardService.completeMyTraining(id);
        return ResponseEntity.ok(response);
    }

    // == 8. ACTION CENTER ======================================================

    @Operation(summary = "Get Action Center", description = "Aggregates actionable items across leaves, documents, performance appraisals, and trainings.")
    @PreAuthorize("hasAuthority('employee.dashboard.read') or hasAuthority('employee.dashboard.view') or hasAuthority('employee.action-center.read')")
    @GetMapping("/action-center")
    public ResponseEntity<?> getActionCenter() {
        if (isNotAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Full authentication is required.", "AUTH_014"));
        }
        permissionCheckService.requireAnyPermission(
                PermissionRegistry.EMPLOYEE_DASHBOARD_READ,
                PermissionRegistry.EMPLOYEE_DASHBOARD_VIEW,
                PermissionRegistry.EMPLOYEE_ACTION_CENTER_READ
        );
        EmployeeActionCenterResponseDto response = dashboardService.getActionCenter();
        return ResponseEntity.ok(response);
    }
}
