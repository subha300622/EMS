package com.example.ems.organization.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.employee.entity.Department;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.DepartmentRepository;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.organization.dto.*;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.entity.OrganizationAuditLog;
import com.example.ems.organization.repository.OrganizationRepository;
import com.example.ems.organization.service.OrganizationCacheService;
import com.example.ems.organization.service.OrganizationExportService;
import com.example.ems.organization.service.OrganizationService;
import com.example.ems.security.service.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/platform-admin")
@CrossOrigin("*")
@Tag(name = "Platform Administration Organizations", description = "Endpoints for managing multi-tenant Organizations")
public class PlatformAdminOrganizationController {

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private OrganizationCacheService organizationCacheService;

    @Autowired
    private OrganizationExportService exportService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private RoleService roleService;

    @Autowired
    private JwtService jwtService;

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

    private ResponseEntity<?> validateAccess(String authHeader, String requiredPermission) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!roleService.hasPermission(user.getWorkEmail(), requiredPermission)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires platform admin permission.", "AUTH_002"));
        }
        return null;
    }

    @Operation(summary = "Get All Organizations")
    @GetMapping("/organizations")
    public ResponseEntity<?> getAllOrganizations(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String plan,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {

        ResponseEntity<?> accessError = validateAccess(authHeader, "organization.read");
        if (accessError != null) return accessError;

        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<OrganizationListItemResponse> results = organizationCacheService.searchOrganizations(search, status, plan, pageable);
        return ResponseEntity.ok(ApiResponse.success("Organizations retrieved successfully", results));
    }

    @Operation(summary = "Organization Details")
    @GetMapping("/organizations/{id}")
    public ResponseEntity<?> getOrganizationDetails(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {

        ResponseEntity<?> accessError = validateAccess(authHeader, "organization.read");
        if (accessError != null) return accessError;

        try {
            OrganizationDetailResponse response = organizationCacheService.getOrganizationDetails(id);
            return ResponseEntity.ok(ApiResponse.success("Organization details retrieved", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.error(e.getMessage(), "ORG_001"));
        }
    }

    @Operation(summary = "Create Organization")
    @PostMapping("/organizations")
    public ResponseEntity<?> createOrganization(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody @Valid CreateOrganizationRequest request) {

        ResponseEntity<?> accessError = validateAccess(authHeader, "organization.create");
        if (accessError != null) return accessError;

        User user = resolveUser(authHeader);
        OrganizationDetailResponse response = organizationService.createOrganization(request, user.getWorkEmail());
        organizationCacheService.evictCache(response.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Organization created successfully", response));
    }

    @Operation(summary = "Update Organization")
    @PutMapping("/organizations/{id}")
    public ResponseEntity<?> updateOrganization(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestBody UpdateOrganizationRequest request) {

        ResponseEntity<?> accessError = validateAccess(authHeader, "organization.update");
        if (accessError != null) return accessError;

        User user = resolveUser(authHeader);
        try {
            OrganizationDetailResponse response = organizationService.updateOrganization(id, request, user.getWorkEmail());
            organizationCacheService.evictCache(id);
            return ResponseEntity.ok(ApiResponse.success("Organization updated successfully", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.error(e.getMessage(), "ORG_001"));
        }
    }

    @Operation(summary = "Delete Organization")
    @DeleteMapping("/organizations/{id}")
    public ResponseEntity<?> deleteOrganization(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {

        ResponseEntity<?> accessError = validateAccess(authHeader, "organization.delete");
        if (accessError != null) return accessError;

        User user = resolveUser(authHeader);
        try {
            organizationService.deleteOrganization(id, user.getWorkEmail());
            organizationCacheService.evictCache(id);
            return ResponseEntity.ok(ApiResponse.success("Organization deleted successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.error(e.getMessage(), "ORG_001"));
        }
    }

    @Operation(summary = "Suspend or Activate Organization")
    @PatchMapping("/organizations/{id}/status")
    public ResponseEntity<?> updateStatus(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {

        ResponseEntity<?> accessError = validateAccess(authHeader, "organization.update");
        if (accessError != null) return accessError;

        String status = body.get("status");
        if (status == null || status.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(ErrorResponse.error("Status is required", "ORG_002"));
        }

        User user = resolveUser(authHeader);
        try {
            if ("SUSPENDED".equalsIgnoreCase(status)) {
                String reason = body.getOrDefault("reason", "No reason provided");
                organizationService.suspendOrganization(id, reason, user.getWorkEmail());
            } else if ("ACTIVE".equalsIgnoreCase(status)) {
                organizationService.activateOrganization(id, user.getWorkEmail());
            } else {
                return ResponseEntity.badRequest().body(ErrorResponse.error("Unsupported status change: " + status, "ORG_003"));
            }
            organizationCacheService.evictCache(id);
            return ResponseEntity.ok(ApiResponse.success("Organization status updated successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.error(e.getMessage(), "ORG_001"));
        }
    }

    @Operation(summary = "Update Subscription")
    @PatchMapping("/organizations/{id}/subscription")
    public ResponseEntity<?> updateSubscription(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestBody @Valid UpdateSubscriptionRequest request) {

        ResponseEntity<?> accessError = validateAccess(authHeader, "organization.subscription");
        if (accessError != null) return accessError;

        User user = resolveUser(authHeader);
        try {
            organizationService.updateSubscription(id, request, user.getWorkEmail());
            organizationCacheService.evictCache(id);
            return ResponseEntity.ok(ApiResponse.success("Subscription updated successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.error(e.getMessage(), "ORG_001"));
        }
    }

    @Operation(summary = "Organization Statistics")
    @GetMapping("/organizations/{id}/statistics")
    public ResponseEntity<?> getStatistics(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {

        ResponseEntity<?> accessError = validateAccess(authHeader, "organization.read");
        if (accessError != null) return accessError;

        try {
            OrganizationStatisticsResponse response = organizationCacheService.getStatistics(id);
            return ResponseEntity.ok(ApiResponse.success("Organization statistics retrieved", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.error(e.getMessage(), "ORG_001"));
        }
    }

    @Operation(summary = "Organization Summary")
    @GetMapping("/organizations/summary")
    public ResponseEntity<?> getSummary(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        ResponseEntity<?> accessError = validateAccess(authHeader, "organization.read");
        if (accessError != null) return accessError;

        OrganizationSummaryResponse response = organizationCacheService.getSummary();
        return ResponseEntity.ok(ApiResponse.success("Organizations summary retrieved", response));
    }

    @Operation(summary = "Get Organization Employees")
    @GetMapping("/organizations/{id}/employees")
    public ResponseEntity<?> getEmployees(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        ResponseEntity<?> accessError = validateAccess(authHeader, "organization.read");
        if (accessError != null) return accessError;

        Page<Employee> emps = organizationService.getEmployees(id, PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.success("Organization employees retrieved", emps));
    }

    @Operation(summary = "Get Organization Admins")
    @GetMapping("/organizations/{id}/admins")
    public ResponseEntity<?> getAdmins(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {

        ResponseEntity<?> accessError = validateAccess(authHeader, "organization.read");
        if (accessError != null) return accessError;

        List<User> admins = organizationService.getAdmins(id);
        return ResponseEntity.ok(ApiResponse.success("Organization admins retrieved", admins));
    }

    @Operation(summary = "Get Organization Audit Logs")
    @GetMapping("/organizations/{id}/audit-logs")
    public ResponseEntity<?> getAuditLogs(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        ResponseEntity<?> accessError = validateAccess(authHeader, "organization.audit.read");
        if (accessError != null) return accessError;

        Page<OrganizationAuditLog> logs = organizationService.getAuditLogs(id, PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.success("Organization audit logs retrieved", logs));
    }

    @Operation(summary = "Export Organizations")
    @GetMapping("/organizations/export")
    public ResponseEntity<?> exportOrganizations(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(defaultValue = "csv") String format) {

        ResponseEntity<?> accessError = validateAccess(authHeader, "organization.export");
        if (accessError != null) return accessError;

        List<Organization> orgs = organizationService.getAllForExport();
        byte[] fileBytes;
        String filename;
        MediaType mediaType;

        if ("xlsx".equalsIgnoreCase(format)) {
            fileBytes = exportService.exportToExcel(orgs);
            filename = "organizations.xlsx";
            mediaType = MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        } else if ("pdf".equalsIgnoreCase(format)) {
            fileBytes = exportService.exportToPdf(orgs);
            filename = "organizations.pdf";
            mediaType = MediaType.APPLICATION_PDF;
        } else {
            fileBytes = exportService.exportToCsv(orgs);
            filename = "organizations.csv";
            mediaType = MediaType.TEXT_PLAIN;
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(mediaType)
                .body(fileBytes);
    }

    @Operation(summary = "Global Dashboard Search")
    @GetMapping("/search")
    public ResponseEntity<?> globalSearch(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam String q) {

        ResponseEntity<?> accessError = validateAccess(authHeader, "organization.read");
        if (accessError != null) return accessError;

        String searchPattern = "%" + q.trim().toLowerCase() + "%";

        List<Organization> orgs = organizationRepository.findAll((root, query, cb) ->
                cb.and(
                        cb.equal(root.get("isDeleted"), false),
                        cb.or(
                                cb.like(cb.lower(root.get("name")), searchPattern),
                                cb.like(cb.lower(root.get("organizationCode")), searchPattern)
                        )
                )
        );

        List<Employee> emps = employeeRepository.findAll((root, query, cb) ->
                cb.or(
                        cb.like(cb.lower(root.get("fullName")), searchPattern),
                        cb.like(cb.lower(root.get("email")), searchPattern)
                )
        );

        List<Department> depts = departmentRepository.findAll((root, query, cb) ->
                cb.or(
                        cb.like(cb.lower(root.get("name")), searchPattern),
                        cb.like(cb.lower(root.get("code")), searchPattern)
                )
        );

        List<User> users = userRepository.findAll((root, query, cb) ->
                cb.or(
                        cb.like(cb.lower(root.get("fullName")), searchPattern),
                        cb.like(cb.lower(root.get("workEmail")), searchPattern)
                )
        );

        Map<String, Object> response = new HashMap<>();
        response.put("organizations", orgs);
        response.put("employees", emps);
        response.put("departments", depts);
        response.put("users", users);

        return ResponseEntity.ok(ApiResponse.success("Search results retrieved", response));
    }
}
