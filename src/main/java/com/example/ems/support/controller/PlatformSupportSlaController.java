package com.example.ems.support.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.security.service.JwtService;
import com.example.ems.support.dto.*;
import com.example.ems.support.entity.SupportSla;
import com.example.ems.support.service.PlatformSupportSlaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/platform/support/slas")
@CrossOrigin("*")
@Tag(name = "Platform Admin - Support SLAs")
public class PlatformSupportSlaController {

    @Autowired
    private PlatformSupportSlaService slaService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private RoleService roleService;

    // ── Auth & Permission Helpers ─────────────────────────────────────────────

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
        return roleService.hasPermission(user.getWorkEmail(), permission) || roleService.isSuperAdmin(user.getWorkEmail());
    }

    private ResponseEntity<?> unauthorizedResponse() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
    }

    private ResponseEntity<?> forbiddenResponse(String permission) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.error("Access Denied: Requires '" + permission + "' permission.", "AUTH_002"));
    }

    // ── Endpoints ─────────────────────────────────────────────────────────────

    @GetMapping
    @Operation(summary = "Get SLA Policies (Paginated/Filtered Table)")
    public ResponseEntity<?> getSlas(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Boolean isDefault,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        User user = resolveUser(authHeader);
        if (user == null) return unauthorizedResponse();
        if (!checkPermission(user, "support.view")) return forbiddenResponse("support.view");

        try {
            Page<SlaResponse> slas = slaService.getSlas(search, priority, status, isDefault, sortBy, sortDirection, page, size);
            
            SlaPaginatedResponse response = new SlaPaginatedResponse(
                    slas.getContent(),
                    page,
                    size,
                    slas.getTotalElements(),
                    slas.getTotalPages(),
                    slas.hasNext(),
                    slas.hasPrevious()
            );

            return ResponseEntity.ok(ApiResponse.success("SLA policies retrieved successfully.", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "SLA_001"));
        }
    }

    @GetMapping("/{slaId}")
    @Operation(summary = "Get SLA Details")
    public ResponseEntity<?> getSlaDetails(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable Long slaId) {

        User user = resolveUser(authHeader);
        if (user == null) return unauthorizedResponse();
        if (!checkPermission(user, "support.view")) return forbiddenResponse("support.view");

        try {
            SlaResponse details = slaService.getSlaDetails(slaId);
            return ResponseEntity.ok(ApiResponse.success("SLA policy retrieved successfully.", details));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "SLA_002"));
        }
    }

    @PostMapping
    @Operation(summary = "Create SLA Policy")
    public ResponseEntity<?> createSla(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @Valid @RequestBody SupportSlaRequest req) {

        User user = resolveUser(authHeader);
        if (user == null) return unauthorizedResponse();
        if (!checkPermission(user, "support.manage")) return forbiddenResponse("support.manage");

        try {
            SupportSla created = slaService.createSla(req, user);
            Map<String, Object> data = Map.of(
                    "id", created.getId(),
                    "name", created.getName(),
                    "status", created.isEnabled() ? "ACTIVE" : "INACTIVE"
            );
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("SLA policy created successfully.", data));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "SLA_003"));
        }
    }

    @PutMapping("/{slaId}")
    @Operation(summary = "Update SLA Policy")
    public ResponseEntity<?> updateSla(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable Long slaId,
            @Valid @RequestBody SupportSlaRequest req) {

        User user = resolveUser(authHeader);
        if (user == null) return unauthorizedResponse();
        if (!checkPermission(user, "support.manage")) return forbiddenResponse("support.manage");

        try {
            SupportSla updated = slaService.updateSla(slaId, req, user);
            Map<String, Object> data = Map.of(
                    "id", updated.getId(),
                    "updatedAt", updated.getUpdatedAt().toString()
            );
            return ResponseEntity.ok(ApiResponse.success("SLA policy updated successfully.", data));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "SLA_004"));
        }
    }

    @DeleteMapping("/{slaId}")
    @Operation(summary = "Delete SLA Policy")
    public ResponseEntity<?> deleteSla(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable Long slaId) {

        User user = resolveUser(authHeader);
        if (user == null) return unauthorizedResponse();
        if (!checkPermission(user, "support.manage")) return forbiddenResponse("support.manage");

        try {
            slaService.deleteSla(slaId, user);
            return ResponseEntity.ok(ApiResponse.success("SLA policy deleted successfully."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "SLA_005"));
        }
    }

    @PatchMapping("/{slaId}/status")
    @Operation(summary = "Change SLA Status")
    public ResponseEntity<?> changeStatus(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable Long slaId,
            @RequestBody Map<String, String> body) {

        User user = resolveUser(authHeader);
        if (user == null) return unauthorizedResponse();
        if (!checkPermission(user, "support.manage")) return forbiddenResponse("support.manage");

        String status = body.get("status");
        if (status == null || (!"ACTIVE".equalsIgnoreCase(status) && !"INACTIVE".equalsIgnoreCase(status))) {
            return ResponseEntity.badRequest().body(ErrorResponse.error("Status must be ACTIVE or INACTIVE", "SLA_006"));
        }

        try {
            SupportSla updated = slaService.updateStatus(slaId, status, user);
            return ResponseEntity.ok(ApiResponse.success("SLA status updated successfully.", Map.of(
                    "id", updated.getId(),
                    "status", updated.isEnabled() ? "ACTIVE" : "INACTIVE"
            )));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "SLA_007"));
        }
    }

    @PatchMapping("/{slaId}/default")
    @Operation(summary = "Set Default SLA")
    public ResponseEntity<?> setDefault(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable Long slaId) {

        User user = resolveUser(authHeader);
        if (user == null) return unauthorizedResponse();
        if (!checkPermission(user, "support.manage")) return forbiddenResponse("support.manage");

        try {
            SupportSla updated = slaService.setDefaultSla(slaId, user);
            return ResponseEntity.ok(ApiResponse.success("Default SLA policy updated successfully.", Map.of(
                    "id", updated.getId(),
                    "isDefault", updated.isDefault()
            )));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "SLA_008"));
        }
    }

    @PostMapping("/{slaId}/duplicate")
    @Operation(summary = "Duplicate SLA Policy")
    public ResponseEntity<?> duplicate(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable Long slaId) {

        User user = resolveUser(authHeader);
        if (user == null) return unauthorizedResponse();
        if (!checkPermission(user, "support.manage")) return forbiddenResponse("support.manage");

        try {
            SupportSla duplicated = slaService.duplicateSla(slaId, user);
            Map<String, Object> data = Map.of(
                    "id", duplicated.getId(),
                    "name", duplicated.getName(),
                    "status", duplicated.isEnabled() ? "ACTIVE" : "INACTIVE"
            );
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("SLA policy duplicated successfully.", data));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "SLA_009"));
        }
    }

    @GetMapping("/priorities")
    @Operation(summary = "Get SLA Priorities Dropdown")
    public ResponseEntity<?> getPriorities(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {

        User user = resolveUser(authHeader);
        if (user == null) return unauthorizedResponse();
        if (!checkPermission(user, "support.view")) return forbiddenResponse("support.view");

        try {
            return ResponseEntity.ok(ApiResponse.success("Priorities retrieved successfully.", slaService.getPriorities()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "SLA_010"));
        }
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Get SLA Dashboard Summary")
    public ResponseEntity<?> getDashboardSummary(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {

        User user = resolveUser(authHeader);
        if (user == null) return unauthorizedResponse();
        if (!checkPermission(user, "support.view")) return forbiddenResponse("support.view");

        try {
            return ResponseEntity.ok(ApiResponse.success("SLA dashboard retrieved successfully.", slaService.getDashboard()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "SLA_011"));
        }
    }

    @GetMapping("/by-priority/{priority}")
    @Operation(summary = "Get SLA Policy by Priority (Internal API)")
    public ResponseEntity<?> getByPriority(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable String priority) {

        User user = resolveUser(authHeader);
        if (user == null) return unauthorizedResponse();
        if (!checkPermission(user, "support.view")) return forbiddenResponse("support.view");

        try {
            return ResponseEntity.ok(ApiResponse.success("SLA policy retrieved successfully.", slaService.getByPriority(priority)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "SLA_012"));
        }
    }
}
