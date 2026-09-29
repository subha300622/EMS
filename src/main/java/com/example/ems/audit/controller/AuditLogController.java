package com.example.ems.audit.controller;

import com.example.ems.audit.dto.AuditDashboardStatsDto;
import com.example.ems.audit.dto.ReviewAuditLogRequest;
import com.example.ems.audit.entity.AuditLog;
import com.example.ems.audit.entity.Severity;
import com.example.ems.audit.service.AuditLogService;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.security.service.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/api/v1/audit-logs")
@CrossOrigin("*")
@Tag(name = "Audit & Compliance", description = "Audit Log Management and Compliance Monitoring APIs")
public class AuditLogController {

    @Autowired
    private AuditLogService auditLogService;

    @Autowired(required = false)
    private com.example.ems.audit.service.AuditLogQueryService auditLogQueryService;

    @Autowired(required = false)
    private com.example.ems.audit.service.AuditLogExportService auditLogExportService;

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
        if ("SUPER_ADMIN".equalsIgnoreCase(roleName) || "ADMIN".equalsIgnoreCase(roleName) || "PLATFORM_ADMIN".equalsIgnoreCase(roleName)) {
            return null; // unrestricted
        }
        if ("FINANCE".equalsIgnoreCase(roleName)) {
            return List.of("Payroll", "Expenses", "Finance Reports", "Payroll Settings", "Increment", "F&F Settlement", "PAYROLL", "EXPENSE", "FINANCE");
        }
        if ("HR".equalsIgnoreCase(roleName) || "HR_MANAGER".equalsIgnoreCase(roleName)) {
            return List.of("Employee", "Recruitment", "Leave", "Onboarding", "Offboarding", "EMPLOYEE", "LEAVE", "ATTENDANCE", "RECRUITMENT");
        }
        return List.of();
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get Paginated and Filtered Audit Logs")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Audit logs retrieved successfully",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = AuditLog.class)))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Requires audit.read permission",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> getAllLogs(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String user,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) Severity severity,
            @RequestParam(required = false) Boolean flagged,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) String recordId,
            @RequestParam(required = false) String entityType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String[] sort) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkPermission(currentUser, "audit.read")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'audit.read' permission.", "AUTH_002"));
        }

        Collection<String> allowedModules = getAllowedModulesForUser(currentUser);

        Sort sortObj = Sort.by(Sort.Direction.DESC, "createdAt");
        if (sort != null) {
            if (sort.length == 1 && sort[0].contains(",")) {
                String[] parts = sort[0].split(",");
                Sort.Direction dir = (parts.length > 1 && "asc".equalsIgnoreCase(parts[1])) ? Sort.Direction.ASC : Sort.Direction.DESC;
                sortObj = Sort.by(dir, parts[0]);
            } else if (sort.length >= 2) {
                Sort.Direction dir = "asc".equalsIgnoreCase(sort[1]) ? Sort.Direction.ASC : Sort.Direction.DESC;
                sortObj = Sort.by(dir, sort[0]);
            } else if (sort.length == 1) {
                sortObj = Sort.by(Sort.Direction.DESC, sort[0]);
            }
        }
        Pageable pageable = PageRequest.of(page, size, sortObj);

        // If new enterprise filters are provided and queryService is available, use DTO query
        if (auditLogQueryService != null && (status != null || departmentId != null || recordId != null)) {
            com.example.ems.audit.dto.AuditLogFilterRequest filterRequest = new com.example.ems.audit.dto.AuditLogFilterRequest();
            filterRequest.setSearch(search);
            filterRequest.setModule(module);
            filterRequest.setAction(action);
            filterRequest.setUserId(user);
            filterRequest.setStartDate(from);
            filterRequest.setEndDate(to);
            filterRequest.setStatus(status);
            filterRequest.setDepartmentId(departmentId);
            filterRequest.setRecordId(recordId);
            filterRequest.setEntityType(entityType);
            Page<com.example.ems.audit.dto.AuditLogResponse> responsePage = auditLogQueryService.getLogs(filterRequest, currentUser, pageable);
            return ResponseEntity.ok(ApiResponse.success("Audit logs retrieved successfully", responsePage));
        }

        Page<AuditLog> pageResult = auditLogService.getFilteredLogs(
                search, module, action, user, date, from, to, severity, flagged, allowedModules, pageable);
        return ResponseEntity.ok(ApiResponse.success("Audit logs retrieved successfully", pageResult));
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get Audit Log Details By ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Audit log details retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuditLog.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Requires audit.read permission",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Audit log not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> getLogById(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkPermission(currentUser, "audit.read")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'audit.read' permission.", "AUTH_002"));
        }

        if (auditLogQueryService != null) {
            try {
                com.example.ems.audit.dto.AuditLogDetailResponse detail = auditLogQueryService.getLogById(id, currentUser);
                return ResponseEntity.ok(ApiResponse.success("Audit log details retrieved successfully", detail));
            } catch (java.util.NoSuchElementException e) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ErrorResponse.error("Audit log not found with ID: " + id, "AUD_001"));
            }
        }

        return auditLogService.getLogById(id)
                .<ResponseEntity<?>>map(
                        log -> ResponseEntity.ok(ApiResponse.success("Audit log details retrieved successfully", log)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ErrorResponse.error("Audit log not found with ID: " + id, "AUD_001")));
    }

    @GetMapping(value = "/employees/{employeeId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get Audit History for a Specific Employee")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Employee audit history retrieved successfully",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = com.example.ems.audit.dto.AuditLogResponse.class)))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Requires audit.read permission",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> getEmployeeAuditHistory(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String employeeId) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkPermission(currentUser, "audit.read")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'audit.read' permission.", "AUTH_002"));
        }

        if (auditLogQueryService != null) {
            List<com.example.ems.audit.dto.AuditLogResponse> history = auditLogQueryService.getEmployeeAuditHistory(employeeId, currentUser);
            return ResponseEntity.ok(ApiResponse.success("Employee audit history retrieved successfully", history));
        } else {
            List<AuditLog> history = auditLogService.getLogsByEntity("Employee", employeeId);
            return ResponseEntity.ok(ApiResponse.success("Employee audit history retrieved successfully", history));
        }
    }

    @GetMapping(value = "/my-activity", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get Current User Activity Audit Logs")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "My activity retrieved successfully",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = com.example.ems.audit.dto.AuditLogResponse.class)))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> getMyActivity(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        if (auditLogQueryService != null) {
            Page<com.example.ems.audit.dto.AuditLogResponse> myLogs = auditLogQueryService.getMyActivity(currentUser, pageable);
            return ResponseEntity.ok(ApiResponse.success("My activity retrieved successfully", myLogs));
        } else {
            String uid = currentUser.getUserId() != null ? currentUser.getUserId() : currentUser.getWorkEmail();
            List<AuditLog> logs = auditLogService.getLogsByUser(uid);
            return ResponseEntity.ok(ApiResponse.success("My activity retrieved successfully", logs));
        }
    }

    @GetMapping(value = "/export", produces = { "text/csv", MediaType.APPLICATION_OCTET_STREAM_VALUE })
    @Operation(summary = "Export Audit Logs as CSV")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "CSV export stream",
                    content = @Content(mediaType = "text/csv", schema = @Schema(type = "string", format = "binary"))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Requires audit.export permission",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> exportLogs(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) String search) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkPermission(currentUser, "audit.export")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'audit.export' permission.", "AUTH_002"));
        }

        byte[] data;
        if (auditLogExportService != null) {
            com.example.ems.audit.dto.AuditLogFilterRequest filterRequest = new com.example.ems.audit.dto.AuditLogFilterRequest();
            filterRequest.setModule(module);
            filterRequest.setAction(action);
            filterRequest.setStatus(status);
            filterRequest.setDepartmentId(departmentId);
            filterRequest.setSearch(search);
            data = auditLogExportService.exportAuditLogsCsv(filterRequest, currentUser);
        } else {
            Collection<String> allowedModules = getAllowedModulesForUser(currentUser);
            data = auditLogService.exportLogsToCsv(allowedModules);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "audit_logs.csv");
        headers.setContentLength(data.length);
        return new ResponseEntity<>(data, headers, HttpStatus.OK);
    }

    @GetMapping(value = "/dashboard", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get Audit Log Dashboard Summary Statistics")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Dashboard stats retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuditDashboardStatsDto.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Requires audit.read permission",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> getDashboardStats(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkPermission(currentUser, "audit.read")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'audit.read' permission.", "AUTH_002"));
        }
        Collection<String> allowedModules = getAllowedModulesForUser(currentUser);
        return ResponseEntity.ok(ApiResponse.success("Dashboard stats retrieved successfully", 
                auditLogService.getDashboardStats(allowedModules)));
    }

    @GetMapping(value = "/summary", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get Audit Log Dashboard Summary (alias)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Dashboard stats retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuditDashboardStatsDto.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Requires audit.read permission",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> getSummary(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return getDashboardStats(authHeader);
    }

    @PostMapping(value = "/{id}/review", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Review Flagged Audit Log and Clear Flag")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Audit log reviewed and flag cleared",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuditLog.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Requires audit.read permission",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Audit log not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> reviewLog(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestBody(required = false) @Valid ReviewAuditLogRequest body) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkPermission(currentUser, "audit.read")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'audit.read' permission.", "AUTH_002"));
        }
        String remarks = body != null ? body.remarks() : null;
        try {
            AuditLog reviewed = auditLogService.reviewLog(id, currentUser.getFullName(), remarks);
            return ResponseEntity.ok(ApiResponse.success("Audit log reviewed and flag cleared", reviewed));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "AUD_001"));
        }
    }

    @PostMapping(value = "/dismiss-all", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Dismiss All Flagged Audit Logs")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "All flags dismissed successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Requires audit.read permission",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> dismissAllFlags(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkPermission(currentUser, "audit.read")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'audit.read' permission.", "AUTH_002"));
        }
        auditLogService.dismissAllFlags(currentUser.getFullName());
        return ResponseEntity.ok(ApiResponse.success("All flags dismissed successfully", null));
    }

    @GetMapping(value = "/user/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get Audit Logs By User ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Audit logs retrieved successfully for user",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = AuditLog.class)))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Requires audit.read permission",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> getLogsByUser(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String userId){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkPermission(currentUser, "audit.read")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'audit.read' permission.", "AUTH_002"));
        }
        List<AuditLog> logs = auditLogService.getLogsByUser(userId);
        return ResponseEntity.ok(ApiResponse.success("Audit logs retrieved successfully for user: " + userId, logs));
    }

    @GetMapping(value = "/entity/{entityType}/{entityId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get Audit Logs By Entity Type and ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Audit logs retrieved successfully for entity",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = AuditLog.class)))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Requires audit.read permission",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> getLogsByEntity(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String entityType,
            @PathVariable String entityId){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkPermission(currentUser, "audit.read")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'audit.read' permission.", "AUTH_002"));
        }
        List<AuditLog> logs = auditLogService.getLogsByEntity(entityType, entityId);
        return ResponseEntity.ok(ApiResponse.success("Audit logs retrieved successfully for entity: " + entityType + " (" + entityId + ")", logs));
    }
}

