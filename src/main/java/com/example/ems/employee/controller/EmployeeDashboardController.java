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
import io.swagger.v3.oas.annotations.media.ArraySchema;
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
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Attendance summary retrieved successfully",
                    content = @Content(schema = @Schema(implementation = EmployeeAttendanceDetailSummaryDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
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
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Attendance history retrieved successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = AttendanceCoreResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
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
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Leave balance retrieved successfully",
                    content = @Content(schema = @Schema(implementation = EmployeeLeaveBalanceDetailDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
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
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Leave requests retrieved successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Leave.class)))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
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
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Leave request retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Leave.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Leave request not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
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
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Leave applied successfully",
                    content = @Content(schema = @Schema(implementation = Leave.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
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
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Compensation details retrieved successfully",
                    content = @Content(schema = @Schema(implementation = EmployeeCompensationSummaryDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
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
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Performance summary retrieved successfully",
                    content = @Content(schema = @Schema(implementation = EmployeePerformanceSummaryDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
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
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Performance reviews retrieved successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = EnterprisePerformanceReviewResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
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
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Performance review retrieved successfully",
                    content = @Content(schema = @Schema(implementation = EnterprisePerformanceReviewResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Performance review not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
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
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Self-review submitted successfully",
                    content = @Content(schema = @Schema(implementation = EnterprisePerformanceReviewResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
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
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Documents overview retrieved successfully",
                    content = @Content(schema = @Schema(implementation = com.example.ems.employee.dto.MyDocumentsDashboardResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
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
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Document uploaded successfully",
                    content = @Content(schema = @Schema(implementation = MyDocumentUploadResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
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
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Document details retrieved successfully",
                    content = @Content(schema = @Schema(implementation = MyDocumentDetailsResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Document not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
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
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Trainings retrieved successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = com.example.ems.training.dto.TrainingAssignmentItemResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
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
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Training details retrieved successfully",
                    content = @Content(schema = @Schema(implementation = com.example.ems.training.dto.EmployeeTrainingDetailResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Training not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
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
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Training completed successfully",
                    content = @Content(schema = @Schema(example = "{\"success\": true, \"message\": \"Training completed successfully\"}"))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Training assignment not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
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
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Action center retrieved successfully",
                    content = @Content(schema = @Schema(implementation = EmployeeActionCenterResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
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
