package com.example.ems.onboarding.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.onboarding.entity.Onboarding;
import com.example.ems.employee.entity.Employee;
import com.example.ems.onboarding.dto.*;
import com.example.ems.onboarding.service.OnboardingService;
import com.example.ems.security.service.JwtService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin("*")
@Tag(name = "Onboarding Management")
public class OnboardingController {

    @Autowired
    private OnboardingService onboardingService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private com.example.ems.employee.repository.EmployeeRepository employeeRepository;

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

    private com.example.ems.employee.entity.Employee resolveEmployee(User currentUser) {
        if (currentUser == null) return null;
        return employeeRepository.findByEmail(currentUser.getWorkEmail()).orElse(null);
    }

    // ── 0. SELF-SERVICE ONBOARDING ──────────────────────────────────────────
    @GetMapping("/onboarding/me")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getMyOnboardingDetails(
            @RequestHeader(value = "Authorization", required = false) String authHeader){

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        Employee employee = resolveEmployee(currentUser);
        if (employee == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.error("Employee profile not found for user", "EMP_002"));
        }

        Onboarding onboarding = onboardingService.getOrCreateOnboardingForEmployee(employee);
        List<OnboardingTaskResponse> taskResponses = onboardingService.getTasks(onboarding.getId());
        int totalSteps = taskResponses.size();
        int completedSteps = (int) taskResponses.stream()
                .filter(t -> "COMPLETED".equalsIgnoreCase(t.getStatus()))
                .count();

        Map<String, Object> response = new HashMap<>();
        response.put("employeeId", employee.getEmployeeId());
        response.put("fullName", employee.getFullName());
        response.put("department", employee.getDepartment() != null ? employee.getDepartment() : "Engineering");
        response.put("joiningDate", employee.getJoiningDate() != null ? employee.getJoiningDate().toString() : "2026-06-10");
        response.put("onboardingStatus", onboarding.getStatus());
        response.put("completedSteps", completedSteps);
        response.put("totalSteps", totalSteps);

        return ResponseEntity.ok(ApiResponse.success("My onboarding details retrieved", response));
    }

    @PutMapping("/onboarding/me")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> updateMyOnboardingProfile(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, Object> body){

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        Employee employee = resolveEmployee(currentUser);
        if (employee == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.error("Employee profile not found for user", "EMP_002"));
        }

        Onboarding onboarding = onboardingService.getOrCreateOnboardingForEmployee(employee);
        onboardingService.updateOnboardingProfile(onboarding.getId(), body);
        return ResponseEntity.ok(ApiResponse.success("Onboarding profile updated successfully"));
    }

    @PostMapping(value = "/onboarding/me/documents", consumes = "multipart/form-data")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> uploadMyOnboardingDocument(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam("file") MultipartFile file){

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (file.isEmpty()) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error("Document file is empty", "VAL_001"));
        }

        Employee employee = resolveEmployee(currentUser);
        if (employee == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.error("Employee profile not found for user", "EMP_002"));
        }

        Onboarding onboarding = onboardingService.getOrCreateOnboardingForEmployee(employee);
        String downloadUrl = "http://localhost:8080/api/documents/download/" + System.currentTimeMillis();

        try {
            OnboardingDocumentResponse doc = onboardingService.addDocument(
                    onboarding.getId(), file.getOriginalFilename(), file.getContentType(), downloadUrl);
            return (ResponseEntity) ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Document uploaded successfully", doc));
        } catch (Exception e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "VAL_002"));
        }
    }

    @GetMapping("/onboarding/me/documents")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getMyOnboardingDocuments(
            @RequestHeader(value = "Authorization", required = false) String authHeader){

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        Employee employee = resolveEmployee(currentUser);
        if (employee == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.error("Employee profile not found for user", "EMP_002"));
        }

        Onboarding onboarding = onboardingService.getOrCreateOnboardingForEmployee(employee);
        List<OnboardingDocumentResponse> docs = onboardingService.getDocuments(onboarding.getId());
        return ResponseEntity.ok(ApiResponse.success("My onboarding documents retrieved", docs));
    }

    @PostMapping("/onboarding/me/submit")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> submitMyOnboarding(
            @RequestHeader(value = "Authorization", required = false) String authHeader){

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        Employee employee = resolveEmployee(currentUser);
        if (employee == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.error("Employee profile not found for user", "EMP_002"));
        }

        Onboarding onboarding = onboardingService.getOrCreateOnboardingForEmployee(employee);
        onboardingService.submitOnboarding(onboarding.getId());

        return (ResponseEntity) ResponseEntity.ok(ApiResponse.success("Onboarding submitted successfully", Map.of("status", "UNDER_REVIEW")));
    }

    // ── 1. GET DASHBOARD ────────────────────────────────────────────────────
    @GetMapping("/onboarding-records/dashboard")
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

        OnboardingDashboardResponse stats = onboardingService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.success("Onboarding dashboard statistics retrieved successfully", stats));
    }

    // ── 2. CREATE ONBOARDING ────────────────────────────────────────────────
    @PostMapping("/onboarding-records")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> createOnboarding(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody OnboardingRequest request){
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
            OnboardingResponse response = onboardingService.createOnboarding(request);
            return (ResponseEntity) ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Employee onboarding process initialized successfully", response));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "ONB_001"));
        }
    }

    // ── 3. LIST ONBOARDINGS ─────────────────────────────────────────────────
    @GetMapping("/onboarding-records")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> listOnboardings(
            @RequestHeader(value = "Authorization", required = false) String authHeader){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
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
                return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ErrorResponse.error("No active onboarding record found for your account.", "ONB_002"));
            }
            return ResponseEntity
                    .ok(ApiResponse.success("Onboarding record retrieved successfully", List.of(selfRecord)));
        }
    }

    // ── 4. GET ONBOARDING BY ID ─────────────────────────────────────────────
    @GetMapping("/onboarding-records/{id}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getOnboarding(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        OnboardingResponse response = onboardingService.getOnboardingById(id).orElse(null);
        if (response == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Onboarding record not found with ID: " + id, "ONB_002"));
        }

        boolean isSelf = currentUser.getWorkEmail().equalsIgnoreCase(response.getEmployeeEmail());
        if (!isSelf && !checkManagerPermission(currentUser)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: You cannot access this onboarding record.", "AUTH_002"));
        }

        return ResponseEntity.ok(ApiResponse.success("Onboarding record details retrieved", response));
    }

    // ── 5. UPDATE PROFILE ───────────────────────────────────────────────────
    @PutMapping("/onboarding-records/{id}/profile")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> updateProfile(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestBody Map<String, Object> body){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        OnboardingResponse response = onboardingService.getOnboardingById(id).orElse(null);
        if (response == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Onboarding record not found with ID: " + id, "ONB_002"));
        }

        boolean isSelf = currentUser.getWorkEmail().equalsIgnoreCase(response.getEmployeeEmail());
        if (!isSelf && !checkManagerPermission(currentUser)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: You cannot update this onboarding profile.", "AUTH_002"));
        }

        try {
            OnboardingResponse updated = onboardingService.updateOnboardingProfile(id, body);
            return ResponseEntity
                    .ok(ApiResponse.success("Onboarding employee profile details updated successfully", updated));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "ONB_003"));
        }
    }

    // ── 6. DOCUMENTS ENDPOINTS ──────────────────────────────────────────────
    @PostMapping("/onboarding-records/{id}/documents")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> uploadDocument(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        OnboardingResponse response = onboardingService.getOnboardingById(id).orElse(null);
        if (response == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Onboarding record not found with ID: " + id, "ONB_002"));
        }

        boolean isSelf = currentUser.getWorkEmail().equalsIgnoreCase(response.getEmployeeEmail());
        if (!isSelf && !checkManagerPermission(currentUser)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: You cannot upload files for this onboarding.",
                            "AUTH_002"));
        }

        if (file.isEmpty()) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error("Document file is empty", "VAL_001"));
        }

        String downloadUrl = "http://localhost:8080/api/documents/download/" + System.currentTimeMillis();
        OnboardingDocumentResponse doc = onboardingService.addDocument(
                id, file.getOriginalFilename(), file.getContentType(), downloadUrl);
        return ResponseEntity
                .ok(ApiResponse.success("Onboarding document uploaded successfully (Verification Pending)", doc));
    }

    @GetMapping("/onboarding-records/{id}/documents")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getDocuments(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        OnboardingResponse response = onboardingService.getOnboardingById(id).orElse(null);
        if (response == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Onboarding record not found with ID: " + id, "ONB_002"));
        }

        boolean isSelf = currentUser.getWorkEmail().equalsIgnoreCase(response.getEmployeeEmail());
        if (!isSelf && !checkManagerPermission(currentUser)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: You cannot view these documents.", "AUTH_002"));
        }

        List<OnboardingDocumentResponse> docs = onboardingService.getDocuments(id);
        return ResponseEntity.ok(ApiResponse.success("Onboarding documents list retrieved", docs));
    }

    // ── 7. TASKS ENDPOINTS ──────────────────────────────────────────────────
    @GetMapping("/onboarding-records/{id}/tasks")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getTasks(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        OnboardingResponse response = onboardingService.getOnboardingById(id).orElse(null);
        if (response == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Onboarding record not found with ID: " + id, "ONB_002"));
        }

        boolean isSelf = currentUser.getWorkEmail().equalsIgnoreCase(response.getEmployeeEmail());
        if (!isSelf && !checkManagerPermission(currentUser)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: You cannot view tasks.", "AUTH_002"));
        }

        List<OnboardingTaskResponse> tasks = onboardingService.getTasks(id);
        return ResponseEntity.ok(ApiResponse.success("Onboarding checklist tasks retrieved", tasks));
    }

    @PatchMapping("/onboarding-records/tasks/{taskId}/status")
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

        OnboardingTaskResponse updated = onboardingService.updateTaskStatus(taskId, status).orElse(null);
        if (updated == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Onboarding task not found with ID: " + taskId, "ONB_004"));
        }
        return ResponseEntity
                .ok(ApiResponse.success("Onboarding task status updated to " + status.toUpperCase(), updated));
    }

    // ── 8. COMPLETE & APPROVE ENDPOINTS ─────────────────────────────────────
    @PatchMapping("/onboarding-records/{id}/complete")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> completeOnboarding(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        OnboardingResponse response = onboardingService.getOnboardingById(id).orElse(null);
        if (response == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Onboarding record not found with ID: " + id, "ONB_002"));
        }

        boolean isSelf = currentUser.getWorkEmail().equalsIgnoreCase(response.getEmployeeEmail());
        if (!isSelf && !checkManagerPermission(currentUser)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: You cannot complete this onboarding.", "AUTH_002"));
        }

        OnboardingResponse completed = onboardingService.completeOnboarding(id).orElse(null);
        return ResponseEntity
                .ok(ApiResponse.success("Employee onboarding marked as completed (Pending HR approval)", completed));
    }

    @PatchMapping("/onboarding-records/{id}/approve")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> approveOnboarding(
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

        OnboardingResponse approved = onboardingService.approveOnboarding(id).orElse(null);
        if (approved == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Onboarding record not found with ID: " + id, "ONB_002"));
        }
        return ResponseEntity
                .ok(ApiResponse.success("Onboarding profile successfully approved by HR manager", approved));
    }

    // ── 9. VERIFICATION ENDPOINT ────────────────────────────────────────────
    @PatchMapping("/onboarding-records/documents/{documentId}/verification")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> verifyDocument(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long documentId,
            @RequestBody Map<String, String> body){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkManagerPermission(currentUser)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires HR/Manager permissions.", "AUTH_002"));
        }

        String status = body.get("status");
        String notes = body.get("notes");
        if (status == null || status.isBlank()) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error("Status field is required", "VAL_001"));
        }

        OnboardingDocumentResponse doc = onboardingService.verifyDocument(documentId, status, notes).orElse(null);
        if (doc == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Document record not found with ID: " + documentId, "ONB_005"));
        }
        return ResponseEntity
                .ok(ApiResponse.success("Document verification status updated to " + status.toUpperCase(), doc));
    }

    // ── 10. ASSETS & TRAININGS REQUESTS ──────────────────────────────────────
    @PostMapping("/onboarding-records/assets")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> requestAsset(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody OnboardingAssetRequest request){
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
            OnboardingResponse response = onboardingService.requestAsset(request);
            return ResponseEntity.ok(ApiResponse.success("Asset allocation request submitted successfully", response));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "ONB_006"));
        }
    }

    @PostMapping("/onboarding-records/trainings")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> assignTraining(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody OnboardingTrainingRequest request){
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
            OnboardingResponse response = onboardingService.assignTraining(request);
            return ResponseEntity
                    .ok(ApiResponse.success("Training course assigned successfully to employee", response));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "ONB_007"));
        }
    }

    // ── 11. PROVISION ACCESS ────────────────────────────────────────────────
    @PostMapping("/onboarding-records/{id}/provision-access")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> provisionAccess(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestBody Map<String, Object> body){
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
            Map<String, Object> response = onboardingService.provisionAccess(id, body);
            return ResponseEntity
                    .ok(ApiResponse.success("Accounts and workspace access provisioned successfully", response));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "ONB_002"));
        }
    }

    // ── 12. REPORTS ENDPOINT ────────────────────────────────────────────────
    @GetMapping("/onboarding-records/reports/{reportType}")
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

        Map<String, Object> data = onboardingService.getReportData(reportType);
        return ResponseEntity.ok(ApiResponse.success("Onboarding reports analytics data compiled successfully", data));
    }

    // ── 13. NOTIFICATIONS ENDPOINT ──────────────────────────────────────────
    @PostMapping("/onboarding-records/notifications")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> triggerNotification(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, Object> body){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkManagerPermission(currentUser)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires HR/Manager permissions.", "AUTH_002"));
        }

        Map<String, Object> response = onboardingService.triggerNotification(body);
        return ResponseEntity.ok(ApiResponse.success("Onboarding reminder alert triggered", response));
    }

    @Operation(summary = "Get Onboarding Record Timeline", description = "Retrieves the onboarding lifecycle events and task completions.")
    @GetMapping("/onboarding-records/{id}/timeline")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getOnboardingTimeline(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        OnboardingResponse onboarding = onboardingService.getOnboardingById(id).orElse(null);
        if (onboarding == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Onboarding record not found with ID: " + id, "ONB_002"));
        }

        boolean isSelf = currentUser.getWorkEmail().equalsIgnoreCase(onboarding.getEmployeeEmail());
        if (!isSelf && !checkManagerPermission(currentUser)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: You cannot view this onboarding record timeline.", "AUTH_002"));
        }

        return ResponseEntity.ok(ApiResponse.success("Onboarding timeline retrieved successfully",
                onboardingService.getOnboardingTimeline(id)));
    }
}
