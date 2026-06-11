package com.example.ems.onboarding.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.employee.entity.Employee;
import com.example.ems.onboarding.dto.OnboardingAssetRequest;
import com.example.ems.onboarding.dto.OnboardingDashboardResponse;
import com.example.ems.onboarding.dto.OnboardingDocumentResponse;
import com.example.ems.onboarding.dto.OnboardingRequest;
import com.example.ems.onboarding.dto.OnboardingResponse;
import com.example.ems.onboarding.dto.OnboardingTaskResponse;
import com.example.ems.onboarding.dto.OnboardingTrainingRequest;
import com.example.ems.onboarding.entity.Onboarding;
import com.example.ems.onboarding.service.OnboardingService;
import com.example.ems.security.service.JwtService;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin("*")
public class OnboardingController {

    @Autowired
    private OnboardingService onboardingService;

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
        return roleService.hasPermission(user.getWorkEmail(), "employee.create")
                || roleService.hasPermission(user.getWorkEmail(), "employee.update")
                || roleService.hasPermission(user.getWorkEmail(), "recruitment.manage");
    }

    // ── 1. GET DASHBOARD ────────────────────────────────────────────────────
    @GetMapping("/onboarding-records/dashboard")
    public ResponseEntity<?> getDashboard(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkManagerPermission(currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires HR/Manager permissions.", "AUTH_002"));
        }

        OnboardingDashboardResponse stats = onboardingService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.success("Onboarding dashboard statistics retrieved successfully", stats));
    }

    // ── 2. CREATE ONBOARDING ────────────────────────────────────────────────
    @PostMapping("/onboarding-records")
    public ResponseEntity<?> createOnboarding(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody OnboardingRequest request) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkManagerPermission(currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires HR/Manager permissions.", "AUTH_002"));
        }

        try {
            OnboardingResponse response = onboardingService.createOnboarding(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Employee onboarding process initialized successfully", response));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "ONB_001"));
        }
    }

    // ── 3. LIST ONBOARDINGS ─────────────────────────────────────────────────
    @GetMapping("/onboarding-records")
    public ResponseEntity<?> listOnboardings(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (checkManagerPermission(currentUser)) {
            List<OnboardingResponse> list = onboardingService.getOnboardings();
            return ResponseEntity.ok(ApiResponse.success("Onboarding records retrieved successfully", list));
        } else {
            // For standard employees, return only their own onboarding record
            OnboardingResponse selfRecord = onboardingService.getOnboardingByEmployeeEmail(currentUser.getWorkEmail())
                    .orElse(null);
            if (selfRecord == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ErrorResponse.error("No active onboarding record found for your account.", "ONB_002"));
            }
            return ResponseEntity
                    .ok(ApiResponse.success("Onboarding record retrieved successfully", List.of(selfRecord)));
        }
    }

    // ── 4. GET ONBOARDING BY ID ─────────────────────────────────────────────
    @GetMapping("/onboarding-records/{id}")
    public ResponseEntity<?> getOnboarding(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        OnboardingResponse response = onboardingService.getOnboardingById(id).orElse(null);
        if (response == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Onboarding record not found with ID: " + id, "ONB_002"));
        }

        boolean isSelf = currentUser.getWorkEmail().equalsIgnoreCase(response.getEmployeeEmail());
        if (!isSelf && !checkManagerPermission(currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: You cannot access this onboarding record.", "AUTH_002"));
        }

        return ResponseEntity.ok(ApiResponse.success("Onboarding record details retrieved", response));
    }

    // ── 5. UPDATE PROFILE ───────────────────────────────────────────────────
    @PutMapping("/onboarding-records/{id}/profile")
    public ResponseEntity<?> updateProfile(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        OnboardingResponse response = onboardingService.getOnboardingById(id).orElse(null);
        if (response == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Onboarding record not found with ID: " + id, "ONB_002"));
        }

        boolean isSelf = currentUser.getWorkEmail().equalsIgnoreCase(response.getEmployeeEmail());
        if (!isSelf && !checkManagerPermission(currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: You cannot update this onboarding profile.", "AUTH_002"));
        }

        try {
            OnboardingResponse updated = onboardingService.updateOnboardingProfile(id, body);
            return ResponseEntity
                    .ok(ApiResponse.success("Onboarding employee profile details updated successfully", updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "ONB_003"));
        }
    }

    // ── 6. DOCUMENTS ENDPOINTS ──────────────────────────────────────────────
    @PostMapping("/onboarding-records/{id}/documents")
    public ResponseEntity<?> uploadDocument(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        OnboardingResponse response = onboardingService.getOnboardingById(id).orElse(null);
        if (response == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Onboarding record not found with ID: " + id, "ONB_002"));
        }

        boolean isSelf = currentUser.getWorkEmail().equalsIgnoreCase(response.getEmployeeEmail());
        if (!isSelf && !checkManagerPermission(currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: You cannot upload files for this onboarding.",
                            "AUTH_002"));
        }

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(ErrorResponse.error("Document file is empty", "VAL_001"));
        }

        String downloadUrl = "http://localhost:8080/api/documents/download/" + System.currentTimeMillis();
        OnboardingDocumentResponse doc = onboardingService.addDocument(
                id, file.getOriginalFilename(), file.getContentType(), downloadUrl);
        return ResponseEntity
                .ok(ApiResponse.success("Onboarding document uploaded successfully (Verification Pending)", doc));
    }

    @GetMapping("/onboarding-records/{id}/documents")
    public ResponseEntity<?> getDocuments(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        OnboardingResponse response = onboardingService.getOnboardingById(id).orElse(null);
        if (response == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Onboarding record not found with ID: " + id, "ONB_002"));
        }

        boolean isSelf = currentUser.getWorkEmail().equalsIgnoreCase(response.getEmployeeEmail());
        if (!isSelf && !checkManagerPermission(currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: You cannot view these documents.", "AUTH_002"));
        }

        List<OnboardingDocumentResponse> docs = onboardingService.getDocuments(id);
        return ResponseEntity.ok(ApiResponse.success("Onboarding documents list retrieved", docs));
    }

    // ── 7. TASKS ENDPOINTS ──────────────────────────────────────────────────
    @GetMapping("/onboarding-records/{id}/tasks")
    public ResponseEntity<?> getTasks(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        OnboardingResponse response = onboardingService.getOnboardingById(id).orElse(null);
        if (response == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Onboarding record not found with ID: " + id, "ONB_002"));
        }

        boolean isSelf = currentUser.getWorkEmail().equalsIgnoreCase(response.getEmployeeEmail());
        if (!isSelf && !checkManagerPermission(currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: You cannot view tasks.", "AUTH_002"));
        }

        List<OnboardingTaskResponse> tasks = onboardingService.getTasks(id);
        return ResponseEntity.ok(ApiResponse.success("Onboarding checklist tasks retrieved", tasks));
    }

    @PatchMapping("/onboarding-records/tasks/{taskId}/status")
    public ResponseEntity<?> updateTaskStatus(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long taskId,
            @RequestBody Map<String, String> body) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        String status = body.get("status");
        if (status == null || status.isBlank()) {
            return ResponseEntity.badRequest().body(ErrorResponse.error("Status field is required", "VAL_001"));
        }

        OnboardingTaskResponse updated = onboardingService.updateTaskStatus(taskId, status).orElse(null);
        if (updated == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Onboarding task not found with ID: " + taskId, "ONB_004"));
        }
        return ResponseEntity
                .ok(ApiResponse.success("Onboarding task status updated to " + status.toUpperCase(), updated));
    }

    // ── 8. COMPLETE & APPROVE ENDPOINTS ─────────────────────────────────────
    @PatchMapping("/onboarding-records/{id}/complete")
    public ResponseEntity<?> completeOnboarding(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        OnboardingResponse response = onboardingService.getOnboardingById(id).orElse(null);
        if (response == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Onboarding record not found with ID: " + id, "ONB_002"));
        }

        boolean isSelf = currentUser.getWorkEmail().equalsIgnoreCase(response.getEmployeeEmail());
        if (!isSelf && !checkManagerPermission(currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: You cannot complete this onboarding.", "AUTH_002"));
        }

        OnboardingResponse completed = onboardingService.completeOnboarding(id).orElse(null);
        return ResponseEntity
                .ok(ApiResponse.success("Employee onboarding marked as completed (Pending HR approval)", completed));
    }

    @PatchMapping("/onboarding-records/{id}/approve")
    public ResponseEntity<?> approveOnboarding(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkManagerPermission(currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires HR/Manager permissions.", "AUTH_002"));
        }

        OnboardingResponse approved = onboardingService.approveOnboarding(id).orElse(null);
        if (approved == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Onboarding record not found with ID: " + id, "ONB_002"));
        }
        return ResponseEntity
                .ok(ApiResponse.success("Onboarding profile successfully approved by HR manager", approved));
    }

    // ── 9. VERIFICATION ENDPOINT ────────────────────────────────────────────
    @PatchMapping("/onboarding-records/documents/{documentId}/verification")
    public ResponseEntity<?> verifyDocument(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long documentId,
            @RequestBody Map<String, String> body) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkManagerPermission(currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires HR/Manager permissions.", "AUTH_002"));
        }

        String status = body.get("status");
        String notes = body.get("notes");
        if (status == null || status.isBlank()) {
            return ResponseEntity.badRequest().body(ErrorResponse.error("Status field is required", "VAL_001"));
        }

        OnboardingDocumentResponse doc = onboardingService.verifyDocument(documentId, status, notes).orElse(null);
        if (doc == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Document record not found with ID: " + documentId, "ONB_005"));
        }
        return ResponseEntity
                .ok(ApiResponse.success("Document verification status updated to " + status.toUpperCase(), doc));
    }

    // ── 10. ASSETS & TRAININGS REQUESTS ──────────────────────────────────────
    @PostMapping("/onboarding-records/assets")
    public ResponseEntity<?> requestAsset(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody OnboardingAssetRequest request) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkManagerPermission(currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires HR/Manager permissions.", "AUTH_002"));
        }

        try {
            OnboardingResponse response = onboardingService.requestAsset(request);
            return ResponseEntity.ok(ApiResponse.success("Asset allocation request submitted successfully", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "ONB_006"));
        }
    }

    @PostMapping("/onboarding-records/trainings")
    public ResponseEntity<?> assignTraining(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody OnboardingTrainingRequest request) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkManagerPermission(currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires HR/Manager permissions.", "AUTH_002"));
        }

        try {
            OnboardingResponse response = onboardingService.assignTraining(request);
            return ResponseEntity
                    .ok(ApiResponse.success("Training course assigned successfully to employee", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "ONB_007"));
        }
    }

    // ── 11. PROVISION ACCESS ────────────────────────────────────────────────
    @PostMapping("/onboarding-records/{id}/provision-access")
    public ResponseEntity<?> provisionAccess(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkManagerPermission(currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires HR/Manager permissions.", "AUTH_002"));
        }

        try {
            Map<String, Object> response = onboardingService.provisionAccess(id, body);
            return ResponseEntity
                    .ok(ApiResponse.success("Accounts and workspace access provisioned successfully", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "ONB_002"));
        }
    }

    // ── 12. REPORTS ENDPOINT ────────────────────────────────────────────────
    @GetMapping("/onboarding-records/reports/{reportType}")
    public ResponseEntity<?> getReport(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String reportType) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkManagerPermission(currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires HR/Manager permissions.", "AUTH_002"));
        }

        Map<String, Object> data = onboardingService.getReportData(reportType);
        return ResponseEntity.ok(ApiResponse.success("Onboarding reports analytics data compiled successfully", data));
    }

    // ── 13. NOTIFICATIONS ENDPOINT ──────────────────────────────────────────
    @PostMapping("/onboarding-records/notifications")
    public ResponseEntity<?> triggerNotification(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, Object> body) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkManagerPermission(currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires HR/Manager permissions.", "AUTH_002"));
        }

        Map<String, Object> response = onboardingService.triggerNotification(body);
        return ResponseEntity.ok(ApiResponse.success("Onboarding reminder alert triggered", response));
    }
}
