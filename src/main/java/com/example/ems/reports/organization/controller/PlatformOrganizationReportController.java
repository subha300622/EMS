package com.example.ems.reports.organization.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.PermissionRegistry;
import com.example.ems.auth.service.RoleService;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.reports.common.ExportFormat;
import com.example.ems.reports.common.ReportExportStatus;
import com.example.ems.reports.export.ReportExportHistory;
import com.example.ems.reports.organization.ReportFacade;
import com.example.ems.reports.organization.dto.ExportHistoryResponse;
import com.example.ems.reports.organization.dto.OrganizationReportDetail;
import com.example.ems.reports.organization.dto.OrganizationReportListItem;
import com.example.ems.security.service.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/platform/reports/organizations")
@CrossOrigin("*")
@Tag(name = "Platform Organization Reports", description = "Detailed reports and export tools for platform admins")
public class PlatformOrganizationReportController {

    @Autowired
    private ReportFacade reportFacade;

    @Autowired
    private UserRepository userRepository;

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
                    .body(ErrorResponse.error("Access Denied: Requires platform reports view permission.", "AUTH_002"));
        }
        return null;
    }

    @Operation(summary = "Get paginated organization list report")
    @GetMapping("/list")
    public ResponseEntity<?> getList(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String plan,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        ResponseEntity<?> accessCheck = validateAccess(authHeader, PermissionRegistry.PLATFORM_REPORTS_VIEW);
        if (accessCheck != null) return accessCheck;

        Sort.Direction dir = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(dir, sortBy));

        Page<OrganizationReportListItem> data = reportFacade.getOrganizationList(search, status, plan, pageable);
        return ResponseEntity.ok(ApiResponse.success("Organization list loaded successfully", data));
    }

    @Operation(summary = "Get top organizations list")
    @GetMapping("/top")
    public ResponseEntity<?> getTop(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(defaultValue = "employees") String sortBy,
            @RequestParam(defaultValue = "10") int limit) {

        ResponseEntity<?> accessCheck = validateAccess(authHeader, PermissionRegistry.PLATFORM_REPORTS_VIEW);
        if (accessCheck != null) return accessCheck;

        List<OrganizationReportListItem> data = reportFacade.getTopOrganizations(sortBy, limit);
        return ResponseEntity.ok(ApiResponse.success("Top organizations loaded successfully", data));
    }

    @Operation(summary = "Get inactive organizations list")
    @GetMapping("/inactive")
    public ResponseEntity<?> getInactive(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(defaultValue = "30") int days) {

        ResponseEntity<?> accessCheck = validateAccess(authHeader, PermissionRegistry.PLATFORM_REPORTS_VIEW);
        if (accessCheck != null) return accessCheck;

        List<OrganizationReportListItem> data = reportFacade.getInactiveOrganizations(days);
        return ResponseEntity.ok(ApiResponse.success("Inactive organizations loaded successfully", data));
    }

    @Operation(summary = "Get recently registered organizations list")
    @GetMapping("/recent")
    public ResponseEntity<?> getRecent(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(defaultValue = "30") int days) {

        ResponseEntity<?> accessCheck = validateAccess(authHeader, PermissionRegistry.PLATFORM_REPORTS_VIEW);
        if (accessCheck != null) return accessCheck;

        List<OrganizationReportListItem> data = reportFacade.getRecentlyRegistered(days);
        return ResponseEntity.ok(ApiResponse.success("Recently registered organizations loaded successfully", data));
    }

    @Operation(summary = "Get expiring organizations list")
    @GetMapping("/expiring")
    public ResponseEntity<?> getExpiring(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(defaultValue = "30") int days) {

        ResponseEntity<?> accessCheck = validateAccess(authHeader, PermissionRegistry.PLATFORM_REPORTS_VIEW);
        if (accessCheck != null) return accessCheck;

        List<OrganizationReportListItem> data = reportFacade.getExpiringOrganizations(days);
        return ResponseEntity.ok(ApiResponse.success("Expiring organizations loaded successfully", data));
    }

    @Operation(summary = "Get detailed report for single organization")
    @GetMapping("/{organizationId}")
    public ResponseEntity<?> getDetails(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long organizationId) {

        ResponseEntity<?> accessCheck = validateAccess(authHeader, PermissionRegistry.PLATFORM_REPORTS_VIEW);
        if (accessCheck != null) return accessCheck;

        OrganizationReportDetail data = reportFacade.getOrganizationDetails(organizationId);
        return ResponseEntity.ok(ApiResponse.success("Organization details loaded successfully", data));
    }

    @Operation(summary = "Trigger async export of organization report")
    @PostMapping("/export")
    public ResponseEntity<?> export(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, String> body) {

        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!roleService.hasPermission(user.getWorkEmail(), PermissionRegistry.PLATFORM_REPORTS_VIEW)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires platform reports view permission.", "AUTH_002"));
        }

        String formatStr = body.getOrDefault("format", "CSV").toUpperCase();
        ExportFormat format;
        try {
            format = ExportFormat.valueOf(formatStr);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.error("Invalid export format. Must be CSV, EXCEL, or PDF.", "REP_001"));
        }

        String search = body.get("search");
        String status = body.get("status");
        String plan = body.get("plan");

        ReportExportHistory history = reportFacade.exportReport(format, search, status, plan, user.getWorkEmail());
        return ResponseEntity.ok(ApiResponse.success("Report export started successfully", Map.of(
                "exportId", history.getId(),
                "status", history.getStatus().name(),
                "downloadUrl", "/api/v1/platform/reports/organizations/export/download/" + history.getId()
        )));
    }

    @Operation(summary = "Get history of report exports")
    @GetMapping("/exports")
    public ResponseEntity<?> getExports(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!roleService.hasPermission(user.getWorkEmail(), PermissionRegistry.PLATFORM_REPORTS_VIEW)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires platform reports view permission.", "AUTH_002"));
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ExportHistoryResponse> data = reportFacade.getExportHistory(user.getWorkEmail(), pageable);
        return ResponseEntity.ok(ApiResponse.success("Export history loaded successfully", data));
    }

    @Operation(summary = "Download exported report file")
    @GetMapping("/export/download/{exportId}")
    public ResponseEntity<?> download(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long exportId) {

        ResponseEntity<?> accessCheck = validateAccess(authHeader, PermissionRegistry.PLATFORM_REPORTS_VIEW);
        if (accessCheck != null) return accessCheck;

        ReportExportHistory exportRecord;
        try {
            exportRecord = reportFacade.getExportById(exportId);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Export file not found or failed.", "REP_003"));
        }

        if (exportRecord.getStatus() != ReportExportStatus.COMPLETED) {
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body(ErrorResponse.error("Export file is not ready yet or generation failed. Current status: " + exportRecord.getStatus(), "REP_002"));
        }

        InputStream fileStream;
        try {
            fileStream = reportFacade.getExportFile(exportId);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Export file not found or failed to load from storage.", "REP_003"));
        }

        String extension = exportRecord.getExportFormat() == ExportFormat.CSV ? ".csv" :
                (exportRecord.getExportFormat() == ExportFormat.EXCEL ? ".xlsx" : ".pdf");
        String filename = exportRecord.getReportName() + extension;

        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (exportRecord.getExportFormat() == ExportFormat.CSV) {
            mediaType = MediaType.parseMediaType("text/csv");
        } else if (exportRecord.getExportFormat() == ExportFormat.PDF) {
            mediaType = MediaType.APPLICATION_PDF;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mediaType);
        headers.setContentDispositionFormData("attachment", filename);

        return new ResponseEntity<>(new InputStreamResource(fileStream), headers, HttpStatus.OK);
    }
}
