package com.example.ems.audit.controller;

import com.example.ems.audit.entity.AuditLog;
import com.example.ems.audit.entity.Severity;
import com.example.ems.audit.service.AuditLogService;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.security.service.JwtService;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/audit-logs")
@CrossOrigin("*")
@Tag(name = "Audit & Compliance")
public class AuditLogController {

    @Autowired
    private AuditLogService auditLogService;

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
        if (user == null)
            return false;
        return roleService.hasPermission(user.getWorkEmail(), permission)
                || roleService.isSuperAdmin(user.getWorkEmail());
    }

    private Collection<String> getAllowedModulesForUser(User user) {
        if (user == null || user.getRole() == null) {
            return List.of();
        }
        String roleName = user.getRole().getName();
        if ("SUPER_ADMIN".equalsIgnoreCase(roleName) || "ADMIN".equalsIgnoreCase(roleName)) {
            return null; // unrestricted
        }
        if ("FINANCE".equalsIgnoreCase(roleName)) {
            return List.of("Payroll", "Expenses", "Finance Reports", "Payroll Settings", "Increment", "F&F Settlement");
        }
        if ("HR".equalsIgnoreCase(roleName)) {
            return List.of("Employee", "Recruitment", "Leave", "Onboarding", "Offboarding");
        }
        return List.of();
    }

    @GetMapping
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getAllLogs(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String user,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) Severity severity,
            @RequestParam(required = false) Boolean flagged,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String[] sort) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkPermission(currentUser, "audit.read")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'audit.read' permission.", "AUTH_002"));
        }

        Collection<String> allowedModules = getAllowedModulesForUser(currentUser);

        org.springframework.data.domain.Sort sortObj = org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt");
        if (sort != null) {
            if (sort.length == 1 && sort[0].contains(",")) {
                String[] parts = sort[0].split(",");
                org.springframework.data.domain.Sort.Direction dir = (parts.length > 1 && "asc".equalsIgnoreCase(parts[1])) ? org.springframework.data.domain.Sort.Direction.ASC : org.springframework.data.domain.Sort.Direction.DESC;
                sortObj = org.springframework.data.domain.Sort.by(dir, parts[0]);
            } else if (sort.length >= 2) {
                org.springframework.data.domain.Sort.Direction dir = "asc".equalsIgnoreCase(sort[1]) ? org.springframework.data.domain.Sort.Direction.ASC : org.springframework.data.domain.Sort.Direction.DESC;
                sortObj = org.springframework.data.domain.Sort.by(dir, sort[0]);
            } else if (sort.length == 1) {
                sortObj = org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, sort[0]);
            }
        }
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, sortObj);

        org.springframework.data.domain.Page<AuditLog> pageResult = auditLogService.getFilteredLogs(
                search, module, action, user, date, from, to, severity, flagged, allowedModules, pageable);
        return ResponseEntity.ok(ApiResponse.success("Audit logs retrieved successfully", pageResult));
    }

    @GetMapping("/{id}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getLogById(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkPermission(currentUser, "audit.read")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'audit.read' permission.", "AUTH_002"));
        }
        return (ResponseEntity) auditLogService.getLogById(id)
                .<ResponseEntity<?>>map(
                        log -> ResponseEntity.ok(ApiResponse.success("Audit log details retrieved successfully", log)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ErrorResponse.error("Audit log not found with ID: " + id, "AUD_001")));
    }

    @GetMapping("/export")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<byte[]> exportLogs(@RequestHeader(value = "Authorization", required = false) String authHeader){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkPermission(currentUser, "audit.export")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'audit.export' permission.", "AUTH_002"));
        }
        Collection<String> allowedModules = getAllowedModulesForUser(currentUser);
        byte[] data = auditLogService.exportLogsToCsv(allowedModules);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "audit_logs.csv");
        headers.setContentLength(data.length);
        return (ResponseEntity) new ResponseEntity<>(data, headers, HttpStatus.OK);
    }

    @GetMapping("/dashboard")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getDashboardStats(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkPermission(currentUser, "audit.read")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'audit.read' permission.", "AUTH_002"));
        }
        Collection<String> allowedModules = getAllowedModulesForUser(currentUser);
        return ResponseEntity.ok(ApiResponse.success("Dashboard stats retrieved successfully", 
                auditLogService.getDashboardStats(allowedModules)));
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<Object>> getSummary(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return getDashboardStats(authHeader);
    }

    @PostMapping("/{id}/review")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> reviewLog(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkPermission(currentUser, "audit.read")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'audit.read' permission.", "AUTH_002"));
        }
        String remarks = body != null ? body.get("remarks") : null;
        try {
            AuditLog reviewed = auditLogService.reviewLog(id, currentUser.getFullName(), remarks);
            return ResponseEntity.ok(ApiResponse.success("Audit log reviewed and flag cleared", reviewed));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "AUD_001"));
        }
    }

    @PostMapping("/dismiss-all")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> dismissAllFlags(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkPermission(currentUser, "audit.read")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'audit.read' permission.", "AUTH_002"));
        }
        auditLogService.dismissAllFlags(currentUser.getFullName());
        return ResponseEntity.ok(ApiResponse.success("All flags dismissed successfully", null));
    }

    @GetMapping("/user/{userId}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getLogsByUser(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String userId){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkPermission(currentUser, "audit.read")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'audit.read' permission.", "AUTH_002"));
        }
        List<AuditLog> logs = auditLogService.getLogsByUser(userId);
        return ResponseEntity.ok(ApiResponse.success("Audit logs retrieved successfully for user: " + userId, logs));
    }

    @GetMapping("/entity/{entityType}/{entityId}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<Object> getLogsByEntity(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String entityType,
            @PathVariable String entityId){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkPermission(currentUser, "audit.read")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'audit.read' permission.", "AUTH_002"));
        }
        List<AuditLog> logs = auditLogService.getLogsByEntity(entityType, entityId);
        return ResponseEntity.ok(ApiResponse.success("Audit logs retrieved successfully for entity: " + entityType + " (" + entityId + ")", logs));
    }
}
