package com.example.ems.offboarding.controller;

import com.example.ems.asset.dto.AssetReturnRequest;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.offboarding.dto.ExitInterviewFeedbackRequest;
import com.example.ems.offboarding.dto.ExitInterviewRequest;
import com.example.ems.offboarding.dto.HandoverRequest;
import com.example.ems.offboarding.dto.OffboardingDashboardResponse;
import com.example.ems.offboarding.dto.OffboardingRequest;
import com.example.ems.offboarding.dto.OffboardingResponse;
import com.example.ems.offboarding.dto.OffboardingTaskResponse;
import com.example.ems.offboarding.dto.SettlementRequest;
import com.example.ems.offboarding.service.OffboardingService;
import com.example.ems.security.service.JwtService;

import io.swagger.v3.oas.annotations.Operation;
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
@Tag(name = "Offboarding Management")
public class OffboardingController {

    @Autowired
    private OffboardingService offboardingService;

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

    private boolean checkManagerPermission(User user) {
        return roleService.hasPermission(user.getWorkEmail(), "employee.delete")
                || roleService.hasPermission(user.getWorkEmail(), "employee.update")
                || roleService.hasPermission(user.getWorkEmail(), "recruitment.manage");
    }

    // ── 1. GET DASHBOARD ────────────────────────────────────────────────────
    @GetMapping("/offboarding-records/dashboard")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getDashboard(
            @RequestHeader(value = "Authorization", required = false) String authHeader){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkManagerPermission(currentUser)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires HR/Manager permissions.", "AUTH_002"));
        }

        OffboardingDashboardResponse stats = offboardingService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.success("Offboarding dashboard statistics retrieved successfully", stats));
    }

    // ── 2. CREATE OFFBOARDING ────────────────────────────────────────────────
    @PostMapping("/offboarding-records")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> createOffboarding(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody OffboardingRequest request){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkManagerPermission(currentUser)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires HR/Manager permissions.", "AUTH_002"));
        }

        try {
            OffboardingResponse response = offboardingService.createOffboarding(request);
            return (ResponseEntity) ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Employee offboarding process initiated successfully", response));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "OFB_001"));
        }
    }

    // ── 3. LIST OFFBOARDINGS ─────────────────────────────────────────────────
    @GetMapping("/offboarding-records")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> listOffboardings(
            @RequestHeader(value = "Authorization", required = false) String authHeader){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (checkManagerPermission(currentUser)) {
            List<OffboardingResponse> list = offboardingService.getOffboardings();
            return ResponseEntity.ok(ApiResponse.success("Offboarding records list retrieved successfully", list));
        } else {
            // For standard employees, return only their own offboarding record
            OffboardingResponse selfRecord = offboardingService
                    .getOffboardingByEmployeeEmail(currentUser.getWorkEmail()).orElse(null);
            if (selfRecord == null) {
                return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ErrorResponse.error("No active offboarding record found for your account.", "OFB_002"));
            }
            return ResponseEntity
                    .ok(ApiResponse.success("Offboarding record retrieved successfully", List.of(selfRecord)));
        }
    }

    // ── 4. GET OFFBOARDING BY ID ─────────────────────────────────────────────
    @GetMapping("/offboarding-records/{id}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getOffboarding(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        OffboardingResponse response = offboardingService.getOffboardingById(id).orElse(null);
        if (response == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Offboarding record not found with ID: " + id, "OFB_002"));
        }

        boolean isSelf = currentUser.getWorkEmail().equalsIgnoreCase(response.getEmployeeEmail());
        if (!isSelf && !checkManagerPermission(currentUser)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: You cannot access this offboarding record.", "AUTH_002"));
        }

        return ResponseEntity.ok(ApiResponse.success("Offboarding record details retrieved", response));
    }

    // ── 5. APPROVE OFFBOARDING ───────────────────────────────────────────────
    @PatchMapping("/offboarding-records/{id}/approve")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> approveOffboarding(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkManagerPermission(currentUser)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires HR/Manager permissions.", "AUTH_002"));
        }

        OffboardingResponse response = offboardingService.approveOffboarding(id).orElse(null);
        if (response == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Offboarding record not found with ID: " + id, "OFB_002"));
        }
        return ResponseEntity
                .ok(ApiResponse.success("Offboarding profile successfully approved by HR manager", response));
    }

    // ── 6. GET TASKS ────────────────────────────────────────────────────────
    @GetMapping("/offboarding-records/{id}/tasks")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getTasks(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        OffboardingResponse response = offboardingService.getOffboardingById(id).orElse(null);
        if (response == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Offboarding record not found with ID: " + id, "OFB_002"));
        }

        boolean isSelf = currentUser.getWorkEmail().equalsIgnoreCase(response.getEmployeeEmail());
        if (!isSelf && !checkManagerPermission(currentUser)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: You cannot view tasks.", "AUTH_002"));
        }

        List<OffboardingTaskResponse> tasks = offboardingService.getTasks(id);
        return ResponseEntity.ok(ApiResponse.success("Offboarding clearance checklist tasks retrieved", tasks));
    }

    @PatchMapping("/offboarding-records/tasks/{taskId}/status")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> updateTaskStatus(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long taskId,
            @RequestBody Map<String, String> body){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        String status = body.get("status");
        if (status == null || status.isBlank()) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error("Status field is required", "VAL_001"));
        }

        OffboardingTaskResponse updated = offboardingService.updateTaskStatus(taskId, status).orElse(null);
        if (updated == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Offboarding task not found with ID: " + taskId, "OFB_004"));
        }
        return ResponseEntity
                .ok(ApiResponse.success("Offboarding task status updated to " + status.toUpperCase(), updated));
    }

    // ── 7. RETURN ASSET ─────────────────────────────────────────────────────
    @PostMapping("/offboarding-records/assets/return")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> recordAssetReturn(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody AssetReturnRequest request){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkManagerPermission(currentUser)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires HR/Manager permissions.", "AUTH_002"));
        }

        try {
            OffboardingResponse response = offboardingService.recordAssetReturn(request);
            return ResponseEntity.ok(ApiResponse.success("Asset return verification logged successfully", response));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "OFB_006"));
        }
    }

    // ── 8. SETTLEMENT ────────────────────────────────────────────────────────
    @PostMapping("/offboarding-records/{id}/settlement")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> processSettlement(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @Valid @RequestBody SettlementRequest request){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkManagerPermission(currentUser)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires HR/Manager permissions.", "AUTH_002"));
        }

        try {
            OffboardingResponse response = offboardingService.processSettlement(request);
            return ResponseEntity
                    .ok(ApiResponse.success("Final settlement statements calculated and cleared", response));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "OFB_007"));
        }
    }

    // ── 9. COMPLETE & REJECT ENDPOINTS ──────────────────────────────────────
    @PatchMapping("/offboarding-records/{id}/complete")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> completeOffboarding(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        OffboardingResponse response = offboardingService.getOffboardingById(id).orElse(null);
        if (response == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Offboarding record not found with ID: " + id, "OFB_002"));
        }

        boolean isSelf = currentUser.getWorkEmail().equalsIgnoreCase(response.getEmployeeEmail());
        if (!isSelf && !checkManagerPermission(currentUser)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: You cannot complete this offboarding.", "AUTH_002"));
        }

        OffboardingResponse completed = offboardingService.completeOffboarding(id).orElse(null);
        return ResponseEntity
                .ok(ApiResponse.success("Employee offboarding marked as completed (Pending HR approval)", completed));
    }

    @PatchMapping("/offboarding-records/{id}/reject")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> rejectOffboarding(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkManagerPermission(currentUser)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires HR/Manager permissions.", "AUTH_002"));
        }

        OffboardingResponse response = offboardingService.rejectOffboarding(id).orElse(null);
        if (response == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Offboarding record not found with ID: " + id, "OFB_002"));
        }
        return ResponseEntity.ok(ApiResponse.success("Offboarding request rejected by HR", response));
    }

    // ── 10. HANDOVER ────────────────────────────────────────────────────────
    @PostMapping("/offboarding-records/handover")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> recordHandover(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody HandoverRequest request){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkManagerPermission(currentUser)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires HR/Manager permissions.", "AUTH_002"));
        }

        try {
            OffboardingResponse response = offboardingService.recordHandover(request);
            return ResponseEntity.ok(ApiResponse.success("Task handover assignment logged successfully", response));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "OFB_008"));
        }
    }

    // ── 11. EXIT INTERVIEWS ─────────────────────────────────────────────────
    @PostMapping("/offboarding-records/exit-interviews")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> scheduleExitInterview(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody ExitInterviewRequest request){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkManagerPermission(currentUser)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires HR/Manager permissions.", "AUTH_002"));
        }

        try {
            OffboardingResponse response = offboardingService.scheduleExitInterview(request);
            return ResponseEntity.ok(ApiResponse.success("Exit interview session scheduled successfully", response));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "OFB_009"));
        }
    }

    @PostMapping("/offboarding-records/exit-interviews/{id}/feedback")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> addExitFeedback(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @Valid @RequestBody ExitInterviewFeedbackRequest request){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkManagerPermission(currentUser)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires HR/Manager permissions.", "AUTH_002"));
        }

        OffboardingResponse response = offboardingService.addExitFeedback(id, request).orElse(null);
        if (response == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Exit interview schedule not found with ID: " + id, "OFB_010"));
        }
        return ResponseEntity
                .ok(ApiResponse.success("Exit interview feedback and questionnaire answers saved", response));
    }

    // ── 12. REVOKE ACCESS ───────────────────────────────────────────────────
    @PostMapping("/offboarding-records/{id}/revoke-access")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> revokeAccess(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkManagerPermission(currentUser)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires HR/Manager permissions.", "AUTH_002"));
        }

        try {
            Map<String, Object> response = offboardingService.revokeAccess(id);
            return ResponseEntity
                    .ok(ApiResponse.success("Employee system accounts access successfully deactivated", response));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "OFB_002"));
        }
    }

    // ── 13. REPORTS & ANALYTICS ──────────────────────────────────────────────
    @GetMapping("/offboarding-records/reports/{reportType}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getReport(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String reportType){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkManagerPermission(currentUser)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires HR/Manager permissions.", "AUTH_002"));
        }

        Map<String, Object> data = offboardingService.getReportData(reportType);
        return ResponseEntity
                .ok(ApiResponse.success("Offboarding exit statistics reports generated successfully", data));
    }

    @GetMapping("/offboarding-records/analytics")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getAnalytics(
            @RequestHeader(value = "Authorization", required = false) String authHeader){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkManagerPermission(currentUser)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires HR/Manager permissions.", "AUTH_002"));
        }

        Map<String, Object> data = offboardingService.getAnalyticsData();
        return ResponseEntity
                .ok(ApiResponse.success("Offboarding exit satisfaction and analytics indices compiled", data));
    }

    @Operation(summary = "Get Offboarding clearance status", description = "Retrieves the clearance checklist completion status grouped by department.")
    @GetMapping("/offboarding-records/{id}/clearance-status")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getClearanceStatus(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        OffboardingResponse response = offboardingService.getOffboardingById(id).orElse(null);
        if (response == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Offboarding record not found with ID: " + id, "OFB_002"));
        }

        boolean isSelf = currentUser.getWorkEmail().equalsIgnoreCase(response.getEmployeeEmail());
        if (!isSelf && !checkManagerPermission(currentUser)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: You cannot view clearance status for this exit record.", "AUTH_002"));
        }

        return ResponseEntity.ok(ApiResponse.success("Offboarding clearance status retrieved successfully",
                offboardingService.getClearanceStatus(id)));
    }
}
