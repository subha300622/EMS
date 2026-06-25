package com.example.ems.offboarding.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.offboarding.dto.*;
import com.example.ems.offboarding.entity.*;
import com.example.ems.offboarding.service.ExitKtService;
import com.example.ems.offboarding.service.ExitRecommendationService;
import com.example.ems.security.service.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/exit")
@CrossOrigin("*")
@Tag(name = "Exit - KT & Manager Recommendation Management")
public class ExitController {

    @Autowired
    private ExitKtService exitKtService;

    @Autowired
    private ExitRecommendationService exitRecommendationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

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

    private enum AccessLevel {
        MANAGER, HR, DENIED
    }

    private AccessLevel getAccessLevel(User user, Employee exitingEmployee) {
        if (user == null) return AccessLevel.DENIED;
        if (roleService.isSuperAdmin(user.getWorkEmail())) return AccessLevel.MANAGER;

        Employee currentEmployee = employeeRepository.findByEmail(user.getWorkEmail()).orElse(null);
        if (currentEmployee == null) return AccessLevel.DENIED;

        // Check if direct manager
        if (exitingEmployee.getManager() != null && exitingEmployee.getManager().getId().equals(currentEmployee.getId())) {
            return AccessLevel.MANAGER;
        }

        // Check if HR
        if (roleService.hasPermission(user.getWorkEmail(), "employee.delete")
                || roleService.hasPermission(user.getWorkEmail(), "employee.update")
                || roleService.hasPermission(user.getWorkEmail(), "recruitment.manage")) {
            return AccessLevel.HR;
        }

        return AccessLevel.DENIED;
    }

    // ── 1. GET KT PLAN (LAZY INIT) ───────────────────────────────────────────
    @Operation(summary = "Get KT Plan Details", description = "Retrieves the clearance checklist and KT plan details for offboarding employee.")
    @GetMapping("/kt-plan/{employeeId}")
    public ResponseEntity<Object> getKTPlan(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable Long employeeId) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        Employee exitingEmployee = employeeRepository.findById(employeeId).orElse(null);
        if (exitingEmployee == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Exiting employee not found with ID: " + employeeId, "EMP_001"));
        }

        AccessLevel level = getAccessLevel(currentUser, exitingEmployee);
        if (level == AccessLevel.DENIED) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: You cannot view KT plan details for this employee.", "AUTH_002"));
        }

        try {
            ExitKtPlan plan = exitKtService.getOrCreateKTPlan(employeeId);
            ExitKtPlanResponse response = exitKtService.mapToResponse(plan);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.error(e.getMessage(), "OFB_011"));
        }
    }

    // ── 2. UPDATE PROJECT ────────────────────────────────────────────────────
    @Operation(summary = "Update KT Project Details")
    @PatchMapping("/kt-plan/{employeeId}/projects/{projectId}")
    public ResponseEntity<Object> updateProject(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable Long employeeId,
            @PathVariable Long projectId,
            @RequestBody Map<String, String> body) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        Employee exitingEmployee = employeeRepository.findById(employeeId).orElse(null);
        if (exitingEmployee == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Exiting employee not found", "EMP_001"));
        }

        AccessLevel level = getAccessLevel(currentUser, exitingEmployee);
        if (level != AccessLevel.MANAGER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires Manager privileges.", "AUTH_002"));
        }

        try {
            exitKtService.updateProject(
                    employeeId,
                    projectId,
                    body.get("handoverNotes"),
                    body.get("status"),
                    body.get("riskLevel")
            );

            Map<String, Object> resp = new HashMap<>();
            resp.put("message", "Project updated successfully");
            resp.put("projectId", projectId);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.error(e.getMessage(), "OFB_011"));
        }
    }

    // ── 3. UPDATE CONTACT ────────────────────────────────────────────────────
    @Operation(summary = "Update Key Contact")
    @PatchMapping("/kt-plan/{employeeId}/contacts/{contactId}")
    public ResponseEntity<Object> updateContact(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable Long employeeId,
            @PathVariable Long contactId,
            @RequestBody Map<String, String> body) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        Employee exitingEmployee = employeeRepository.findById(employeeId).orElse(null);
        if (exitingEmployee == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Exiting employee not found", "EMP_001"));
        }

        AccessLevel level = getAccessLevel(currentUser, exitingEmployee);
        if (level != AccessLevel.MANAGER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires Manager privileges.", "AUTH_002"));
        }

        try {
            exitKtService.updateContact(employeeId, contactId, body.get("responsibility"));

            Map<String, Object> resp = new HashMap<>();
            resp.put("message", "Contact updated successfully");
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.error(e.getMessage(), "OFB_011"));
        }
    }

    // ── 4. UPDATE SYSTEM ACCESS ──────────────────────────────────────────────
    @Operation(summary = "Update System Credential Handover")
    @PatchMapping("/kt-plan/{employeeId}/systems/{systemId}")
    public ResponseEntity<Object> updateSystemAccess(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable Long employeeId,
            @PathVariable Long systemId,
            @RequestBody Map<String, String> body) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        Employee exitingEmployee = employeeRepository.findById(employeeId).orElse(null);
        if (exitingEmployee == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Exiting employee not found", "EMP_001"));
        }

        AccessLevel level = getAccessLevel(currentUser, exitingEmployee);
        if (level != AccessLevel.MANAGER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires Manager privileges.", "AUTH_002"));
        }

        try {
            exitKtService.updateSystemAccess(
                    employeeId,
                    systemId,
                    body.get("status"),
                    body.get("handoverStatus")
            );

            Map<String, Object> resp = new HashMap<>();
            resp.put("message", "System access updated successfully");
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.error(e.getMessage(), "OFB_011"));
        }
    }

    // ── 5. ASSIGN HANDOVER PERSON ────────────────────────────────────────────
    @Operation(summary = "Assign Handover Person")
    @PostMapping("/kt-plan/{employeeId}/handover")
    public ResponseEntity<Object> assignHandover(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable Long employeeId,
            @Valid @RequestBody AssignHandoverRequest request) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        Employee exitingEmployee = employeeRepository.findById(employeeId).orElse(null);
        if (exitingEmployee == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Exiting employee not found", "EMP_001"));
        }

        AccessLevel level = getAccessLevel(currentUser, exitingEmployee);
        if (level != AccessLevel.MANAGER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires Manager privileges.", "AUTH_002"));
        }

        try {
            exitKtService.assignHandover(employeeId, request.getHandoverPersonId());

            Map<String, Object> resp = new HashMap<>();
            resp.put("message", "Handover person assigned successfully");
            resp.put("employeeId", employeeId);
            resp.put("handoverPersonId", request.getHandoverPersonId());
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.error(e.getMessage(), "OFB_011"));
        }
    }

    // ── 6. COMPLETE KT SECTION ───────────────────────────────────────────────
    @Operation(summary = "Complete KT Section")
    @PatchMapping("/kt-plan/{employeeId}/complete-section")
    public ResponseEntity<Object> completeSection(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable Long employeeId,
            @Valid @RequestBody CompleteSectionRequest request) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        Employee exitingEmployee = employeeRepository.findById(employeeId).orElse(null);
        if (exitingEmployee == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Exiting employee not found", "EMP_001"));
        }

        AccessLevel level = getAccessLevel(currentUser, exitingEmployee);
        if (level != AccessLevel.MANAGER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires Manager privileges.", "AUTH_002"));
        }

        try {
            exitKtService.completeSection(employeeId, request.getSection());

            Map<String, Object> resp = new HashMap<>();
            resp.put("message", "Section marked as completed");
            resp.put("section", request.getSection());
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.error(e.getMessage(), "OFB_011"));
        }
    }

    // ── 7. CREATE RECOMMENDATION ─────────────────────────────────────────────
    @Operation(summary = "Create Recommendation")
    @PostMapping("/recommendation/{employeeId}")
    public ResponseEntity<Object> createRecommendation(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable Long employeeId,
            @Valid @RequestBody RecommendationRequest request) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        Employee exitingEmployee = employeeRepository.findById(employeeId).orElse(null);
        if (exitingEmployee == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Exiting employee not found", "EMP_001"));
        }

        AccessLevel level = getAccessLevel(currentUser, exitingEmployee);
        if (level != AccessLevel.MANAGER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires direct Manager privileges.", "AUTH_002"));
        }

        Employee currentEmployee = employeeRepository.findByEmail(currentUser.getWorkEmail()).orElse(null);
        if (currentEmployee == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Manager profile not found", "EMP_001"));
        }

        try {
            exitRecommendationService.submitRecommendation(
                    employeeId,
                    request.getRating(),
                    request.getRecommendation(),
                    currentEmployee.getId()
            );

            Map<String, Object> resp = new HashMap<>();
            resp.put("message", "Recommendation submitted successfully");
            resp.put("employeeId", employeeId);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.error(e.getMessage(), "OFB_012"));
        }
    }

    // ── 8. GET RECOMMENDATION ────────────────────────────────────────────────
    @Operation(summary = "Get Recommendation Details")
    @GetMapping("/recommendation/{employeeId}")
    public ResponseEntity<Object> getRecommendation(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable Long employeeId) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        Employee exitingEmployee = employeeRepository.findById(employeeId).orElse(null);
        if (exitingEmployee == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Exiting employee not found", "EMP_001"));
        }

        AccessLevel level = getAccessLevel(currentUser, exitingEmployee);
        if (level == AccessLevel.DENIED) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: You cannot view recommendation details for this employee.", "AUTH_002"));
        }

        try {
            RecommendationResponse response = exitRecommendationService.getRecommendation(employeeId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "OFB_012"));
        }
    }

    // ── 9. UPDATE RECOMMENDATION ─────────────────────────────────────────────
    @Operation(summary = "Update Recommendation Details")
    @PatchMapping("/recommendation/{employeeId}")
    public ResponseEntity<Object> updateRecommendation(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable Long employeeId,
            @RequestBody Map<String, Object> body) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        Employee exitingEmployee = employeeRepository.findById(employeeId).orElse(null);
        if (exitingEmployee == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Exiting employee not found", "EMP_001"));
        }

        AccessLevel level = getAccessLevel(currentUser, exitingEmployee);
        if (level != AccessLevel.MANAGER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires direct Manager privileges.", "AUTH_002"));
        }

        try {
            Double rating = null;
            if (body.containsKey("rating") && body.get("rating") != null) {
                rating = Double.valueOf(body.get("rating").toString());
            }
            String recommendation = (String) body.get("recommendation");

            exitRecommendationService.updateRecommendation(employeeId, rating, recommendation);

            Map<String, Object> resp = new HashMap<>();
            resp.put("message", "Recommendation updated successfully");
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.error(e.getMessage(), "OFB_012"));
        }
    }
}
