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
import com.example.ems.onboarding.service.TeamOnboardingService;
import com.example.ems.security.service.JwtService;

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
@RequestMapping("/api/v1/onboarding")
@CrossOrigin("*")
@Tag(name = "Onboarding Management")
public class OnboardingController {

    @Autowired
    private OnboardingService onboardingService;

    @Autowired
    private com.example.ems.onboarding.repository.OnboardingEventLogRepository onboardingEventLogRepository;

    @Autowired
    private TeamOnboardingService teamOnboardingService;

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

    private Employee resolveEmployee(User currentUser) {
        if (currentUser == null) return null;
        return employeeRepository.findByEmail(currentUser.getWorkEmail()).orElse(null);
    }

    // ── 1. GET ONBOARDING LIST / SCOPES ──────────────────────────────────────
    @GetMapping
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getOnboardings(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(required = false) String scope,
            @RequestParam(required = false) Long managerId) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        Employee employee = resolveEmployee(currentUser);

        if ("me".equalsIgnoreCase(scope)) {
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

        if ("team".equalsIgnoreCase(scope) || managerId != null) {
            Long mgrId = managerId;
            if (mgrId == null) {
                mgrId = employee != null ? employee.getId() : 1L;
            }
            List<Map<String, Object>> teamList = teamOnboardingService.getTeamOnboardingList(mgrId);
            return ResponseEntity.ok(ApiResponse.success("Team onboarding list retrieved", teamList));
        }

        // Default: List all or self-list
        if (checkManagerPermission(currentUser)) {
            List<OnboardingResponse> list = onboardingService.getOnboardings();
            return ResponseEntity.ok(ApiResponse.success("Onboarding records retrieved successfully", list));
        } else {
            OnboardingResponse selfRecord = onboardingService.getOnboardingByEmployeeEmail(currentUser.getWorkEmail())
                    .orElse(null);
            if (selfRecord == null) {
                return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ErrorResponse.error("No active onboarding record found for your account.", "ONB_002"));
            }
            return ResponseEntity.ok(ApiResponse.success("Onboarding record retrieved successfully", List.of(selfRecord)));
        }
    }

    // ── 2. CREATE ONBOARDING ────────────────────────────────────────────────
    @PostMapping("/create")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> createOnboardingManual(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody TeamOnboardingCreateRequest request) {
        try {
            TeamOnboardingCreateResponse resp = teamOnboardingService.createOnboardingManual(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Onboarding workflow initiated successfully", resp));
        } catch (Exception e) {
            return (ResponseEntity) ResponseEntity.badRequest()
                    .body(ErrorResponse.error(e.getMessage(), "ONB_001"));
        }
    }

    @PostMapping
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> createOnboarding(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody OnboardingRequest request) {
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

    // ── 3. UPDATE ONBOARDING PROFILE ────────────────────────────────────────
    @PutMapping
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> updateMyOnboardingProfile(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(required = false) String scope,
            @RequestBody Map<String, Object> body) {

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

    @PutMapping("/{id}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> updateOnboardingProfile(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {

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
            return ResponseEntity.ok(ApiResponse.success("Onboarding employee profile details updated successfully", updated));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "ONB_003"));
        }
    }

    // ── 4. GET ONBOARDING BY ID ─────────────────────────────────────────────
    @GetMapping("/{id:\\d+}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getOnboarding(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
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

    // ── 5. BUDDY ASSIGNMENT ─────────────────────────────────────────────────
    @PostMapping("/{id}/buddy")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> assignBuddy(
            @PathVariable Long id,
            @RequestBody Map<String, Long> body) {
        try {
            Long buddyEmployeeId = body.get("buddyEmployeeId");
            if (buddyEmployeeId == null) {
                return (ResponseEntity) ResponseEntity.badRequest()
                        .body(ErrorResponse.error("buddyEmployeeId field is required", "VAL_001"));
            }
            Map<String, String> res = teamOnboardingService.assignBuddy(id, buddyEmployeeId);
            return ResponseEntity.ok(ApiResponse.success("Buddy assigned successfully", res));
        } catch (Exception e) {
            return (ResponseEntity) ResponseEntity.badRequest()
                    .body(ErrorResponse.error(e.getMessage(), "ONB_002"));
        }
    }

    // ── 6. GET TASKS ────────────────────────────────────────────────────────
    @GetMapping("/{id}/tasks")
    public ResponseEntity<ApiResponse<Object>> getTasks(
            @PathVariable Long id,
            @RequestParam(required = false) String phase) {
        if (phase != null && !phase.isBlank()) {
            List<TeamOnboardingTaskResponse> tasks = teamOnboardingService.getTasksByPhase(id, phase);
            return ResponseEntity.ok(ApiResponse.success("Tasks retrieved for phase: " + phase, tasks));
        } else {
            List<OnboardingTaskResponse> tasks = onboardingService.getTasks(id);
            return ResponseEntity.ok(ApiResponse.success("Onboarding checklist tasks retrieved", tasks));
        }
    }

    // ── 7. DOCUMENTS ENDPOINTS ──────────────────────────────────────────────
    @GetMapping("/{id}/documents")
    public ResponseEntity<ApiResponse<Object>> getDocuments(@PathVariable Long id) {
        try {
            // Attempt teamOnboardingService getDocuments first (which ensures PAN and AADHAR slots)
            List<Map<String, Object>> docs = teamOnboardingService.getDocuments(id);
            return ResponseEntity.ok(ApiResponse.success("Documents checklist retrieved", docs));
        } catch (Exception e) {
            List<OnboardingDocumentResponse> docs = onboardingService.getDocuments(id);
            return ResponseEntity.ok(ApiResponse.success("Onboarding documents list retrieved", docs));
        }
    }

    @PostMapping(value = "/{id}/documents", consumes = "multipart/form-data")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> uploadDocument(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "documentType", required = false, defaultValue = "AADHAR") String documentType) {
        try {
            if (file.isEmpty()) {
                return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error("File is empty", "VAL_001"));
            }
            String downloadUrl = "http://localhost:8080/api/documents/download/" + System.currentTimeMillis();
            Map<String, Object> resp = teamOnboardingService.addDocument(id, documentType, file.getOriginalFilename(), file.getContentType(), downloadUrl);
            return ResponseEntity.ok(ApiResponse.success("Document uploaded successfully", resp));
        } catch (Exception e) {
            return (ResponseEntity) ResponseEntity.badRequest()
                    .body(ErrorResponse.error(e.getMessage(), "ONB_005"));
        }
    }

    // ── 8. GET PROGRESS METRICS ─────────────────────────────────────────────
    @GetMapping("/{id}/progress")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getProgress(@PathVariable Long id) {
        try {
            TeamOnboardingDetailResponse details = teamOnboardingService.getOnboardingDetails(id);
            Map<String, Object> progressMap = new HashMap<>();
            progressMap.put("overall", details.getProgress().getOverall());
            for (TeamOnboardingDetailResponse.PhaseInfo phase : details.getPhases()) {
                String nameKey = phase.getName().toLowerCase().replace("_", "");
                progressMap.put(nameKey, phase.getTotal() > 0 ? (int) Math.round((double) phase.getCompleted() / phase.getTotal() * 100) : 100);
            }
            return ResponseEntity.ok(ApiResponse.success("Progress metrics compiled", progressMap));
        } catch (Exception e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "ONB_003"));
        }
    }

    // ── 9. PAUSE / RESUME / RETRY CONTROLS ──────────────────────────────────
    @PostMapping("/{id}/pause")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> pauseOnboarding(@PathVariable Long id) {
        try {
            teamOnboardingService.pauseOnboarding(id);
            return ResponseEntity.ok(ApiResponse.success("Onboarding paused successfully (State set to ON_HOLD)"));
        } catch (Exception e) {
            return (ResponseEntity) ResponseEntity.badRequest()
                    .body(ErrorResponse.error(e.getMessage(), "ONB_006"));
        }
    }

    @PostMapping("/{id}/resume")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> resumeOnboarding(@PathVariable Long id) {
        try {
            teamOnboardingService.resumeOnboarding(id);
            return ResponseEntity.ok(ApiResponse.success("Onboarding resumed successfully (State restored to IN_PROGRESS)"));
        } catch (Exception e) {
            return (ResponseEntity) ResponseEntity.badRequest()
                    .body(ErrorResponse.error(e.getMessage(), "ONB_006"));
        }
    }

    @PostMapping("/{id}/retry")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> retryOnboarding(@PathVariable Long id) {
        try {
            teamOnboardingService.retryOnboarding(id);
            return ResponseEntity.ok(ApiResponse.success("Onboarding initialization retried successfully"));
        } catch (Exception e) {
            return (ResponseEntity) ResponseEntity.badRequest()
                    .body(ErrorResponse.error(e.getMessage(), "ONB_006"));
        }
    }

    // ── 10. SUBMIT ONBOARDING ────────────────────────────────────────────────
    @PostMapping("/{id}/submit")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> submitOnboarding(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        onboardingService.submitOnboarding(id);
        return ResponseEntity.ok(ApiResponse.success("Onboarding submitted successfully", Map.of("status", "UNDER_REVIEW")));
    }

    @PostMapping("/me/submit")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> submitMyOnboarding(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
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

        return ResponseEntity.ok(ApiResponse.success("Onboarding submitted successfully", Map.of("status", "UNDER_REVIEW")));
    }

    // ── 11. EVENT LOG ───────────────────────────────────────────────────────
    @GetMapping("/{id}/event-log")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getOnboardingEventLog(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status) {
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
                    .body(ErrorResponse.error("Access Denied: You cannot view this onboarding record event log.", "AUTH_002"));
        }

        List<com.example.ems.onboarding.entity.OnboardingEventLog> logs = onboardingEventLogRepository.findByOnboardingIdOrderByTimestampDesc(id);
        if (type != null && !type.isBlank()) {
            logs = logs.stream().filter(l -> type.equalsIgnoreCase(l.getEventType())).toList();
        }
        if (status != null && !status.isBlank()) {
            logs = logs.stream().filter(l -> status.equalsIgnoreCase(l.getStatus())).toList();
        }

        return ResponseEntity.ok(ApiResponse.success("Onboarding event log retrieved successfully", logs));
    }

    @GetMapping("/event-log")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getGlobalEventLog(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(required = false) String status) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkManagerPermission(currentUser)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires manager privileges.", "AUTH_002"));
        }

        List<com.example.ems.onboarding.entity.OnboardingEventLog> logs;
        if (status != null && !status.isBlank()) {
            logs = onboardingEventLogRepository.findByStatusOrderByTimestampDesc(status.toUpperCase());
        } else {
            logs = onboardingEventLogRepository.findAll();
        }

        return ResponseEntity.ok(ApiResponse.success("Global onboarding event log retrieved successfully", logs));
    }

    @PostMapping("/{id}/event-log/replay-failed")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> replayFailedEvent(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestParam Long eventId) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        // Role validation: HR or FINANCE role required
        boolean hasAccess = roleService.hasRoleOrGreater(currentUser, "HR") || roleService.hasRoleOrGreater(currentUser, "FINANCE");
        if (!hasAccess) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires HR or Finance privileges.", "AUTH_002"));
        }

        try {
            com.example.ems.onboarding.entity.OnboardingEventLog log = onboardingEventLogRepository.findById(eventId).orElse(null);
            if (log == null) {
                return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ErrorResponse.error("Event log not found with ID: " + eventId, "ONB_002"));
            }
            if (!log.getOnboardingId().equals(id)) {
                return (ResponseEntity) ResponseEntity.badRequest()
                        .body(ErrorResponse.error("Event log does not belong to the specified onboarding session.", "ONB_ERR"));
            }

            onboardingService.replayFailedEvent(eventId);
            return ResponseEntity.ok(ApiResponse.success("Event replay triggered successfully"));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "ONB_ERR"));
        } catch (Exception e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.error(e.getMessage(), "ONB_ERR"));
        }
    }
}
