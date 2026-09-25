package com.example.ems.common.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.DmsDashboardResponse;
import com.example.ems.common.dto.DmsDocumentAuditLogResponse;
import com.example.ems.common.dto.DmsDocumentRequest;
import com.example.ems.common.dto.DmsDocumentResponse;
import com.example.ems.common.dto.DmsDocumentShareRequest;
import com.example.ems.common.dto.DmsDocumentShareResponse;
import com.example.ems.common.dto.DmsDocumentVersionRequest;
import com.example.ems.common.dto.DmsDocumentVersionResponse;
import com.example.ems.common.dto.DmsSignatureCompleteRequest;
import com.example.ems.common.dto.DmsSignatureRequest;
import com.example.ems.common.dto.DmsSignatureResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.common.dto.DmsReportResponse;
import com.example.ems.common.service.DmsService;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.security.service.JwtService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.example.ems.common.dto.DocumentSignatureRequest;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin("*")
@Tag(name = "Document Management")
public class DmsController {

    @Autowired
    private DmsService dmsService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private RoleService roleService;

    // ── Auth helpers ─────────────────────────────────────────────────────────
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

    private boolean isManager(User user) {
        return roleService.hasPermission(user.getWorkEmail(), "employee.update")
                || roleService.hasPermission(user.getWorkEmail(), "employee.delete")
                || roleService.hasPermission(user.getWorkEmail(), "recruitment.manage");
    }

    private boolean isDocumentOwner(User user, DmsDocumentResponse doc) {
        if (user == null || doc == null)
            return false;
        return user.getEmployeeId() != null && user.getEmployeeId().equals(String.valueOf(doc.getEmployeeId()));
    }

    private boolean isDocumentSharedWith(User user, Long documentId) {
        if (user == null)
            return false;
        Employee emp = employeeRepository.findByEmail(user.getWorkEmail()).orElse(null);
        if (emp == null)
            return false;

        return dmsService.getSharesByDocument(documentId).stream()
                .anyMatch(s -> s.getSharedWithEmployeeId().equals(emp.getId()));
    }

    // ── 1. DASHBOARD ─────────────────────────────────────────────────────────
    @Operation(summary = "Get DMS Dashboard", description = "Retrieves aggregated DMS statistics.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Document dashboard statistics retrieved successfully",
            content = @Content(schema = @Schema(implementation = DmsDashboardResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Requires HR/Manager permissions")
    })
    @GetMapping("/documents/dashboard")
    public ResponseEntity<?> getDashboard(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        if (!isManager(currentUser))
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires HR/Manager permissions.", "AUTH_002"));

        DmsDashboardResponse stats = dmsService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.success("Document dashboard statistics retrieved successfully", stats));
    }

    // ── 2. DOCUMENTS CRUD / LIST ─────────────────────────────────────────────
    @Operation(summary = "Create Document", description = "Uploads a new document.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Document uploaded successfully",
            content = @Content(schema = @Schema(implementation = DmsDocumentResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PostMapping("/documents")
    public ResponseEntity<?> createDocument(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody DmsDocumentRequest request) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        // Scoping: non-managers can only upload documents owned by themselves
        if (!isManager(currentUser)) {
            if (currentUser.getEmployeeId() == null
                    || !currentUser.getEmployeeId().equals(String.valueOf(request.getEmployeeId()))) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ErrorResponse.error("Access Denied: You cannot upload documents for another employee.",
                                "AUTH_002"));
            }
        }

        try {
            DmsDocumentResponse response = dmsService.createDocument(request, currentUser.getWorkEmail());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Document uploaded successfully", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "DMS_001"));
        }
    }

    @Operation(summary = "List Documents", description = "Lists documents scoped to current user or all documents for managers.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Documents list retrieved successfully",
            content = @Content(array = @io.swagger.v3.oas.annotations.media.ArraySchema(schema = @Schema(implementation = DmsDocumentResponse.class)))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/documents")
    public ResponseEntity<?> getDocuments(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        // If manager, return all. Otherwise, return only employee's own/shared
        // documents
        if (isManager(currentUser)) {
            List<DmsDocumentResponse> docs = dmsService.getDocuments();
            return ResponseEntity.ok(ApiResponse.success("Documents list retrieved successfully", docs));
        } else {
            List<DmsDocumentResponse> docs = dmsService.getMyDocuments(currentUser.getWorkEmail());
            return ResponseEntity.ok(ApiResponse.success("My documents list retrieved successfully", docs));
        }
    }

    @Operation(summary = "Get Document by ID", description = "Retrieves document metadata and details.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Document details retrieved successfully",
            content = @Content(schema = @Schema(implementation = DmsDocumentResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Document not found")
    })
    @GetMapping("/documents/{id}")
    public ResponseEntity<?> getDocumentById(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        Optional<DmsDocumentResponse> docOpt = dmsService.getDocumentById(id);
        if (docOpt.isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Document not found with ID: " + id, "DMS_002"));

        DmsDocumentResponse doc = docOpt.get();

        // Check permission: manager, owner, or recipient
        if (!isManager(currentUser) && !isDocumentOwner(currentUser, doc) && !isDocumentSharedWith(currentUser, id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: You do not have permissions to view this document.",
                            "AUTH_002"));
        }

        return ResponseEntity.ok(ApiResponse.success("Document details retrieved successfully", doc));
    }

    @Operation(summary = "Download Document", description = "Downloads document content and metadata.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Document downloaded successfully",
            content = @Content(schema = @Schema(implementation = DmsDocumentResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Document not found")
    })
    @GetMapping("/documents/{id}/download")
    public ResponseEntity<?> downloadDocument(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        Optional<DmsDocumentResponse> docOpt = dmsService.getDocumentById(id);
        if (docOpt.isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Document not found with ID: " + id, "DMS_002"));

        DmsDocumentResponse doc = docOpt.get();

        // Check permission: manager, owner, or recipient
        if (!isManager(currentUser) && !isDocumentOwner(currentUser, doc) && !isDocumentSharedWith(currentUser, id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: You do not have permissions to download this document.",
                            "AUTH_002"));
        }

        Optional<DmsDocumentResponse> downloaded = dmsService.downloadDocument(id, currentUser.getWorkEmail());
        return ResponseEntity.ok(ApiResponse.success("Document downloaded successfully", downloaded.get()));
    }

    @Operation(summary = "Update Document", description = "Updates document metadata.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Document updated successfully",
            content = @Content(schema = @Schema(implementation = DmsDocumentResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Document not found")
    })
    @PutMapping("/documents/{id}")
    public ResponseEntity<?> updateDocument(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @Valid @RequestBody DmsDocumentRequest request) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        Optional<DmsDocumentResponse> docOpt = dmsService.getDocumentById(id);
        if (docOpt.isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Document not found with ID: " + id, "DMS_002"));

        DmsDocumentResponse doc = docOpt.get();

        // Check permission: manager or owner
        if (!isManager(currentUser) && !isDocumentOwner(currentUser, doc)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: You do not have permissions to update this document.",
                            "AUTH_002"));
        }

        try {
            DmsDocumentResponse response = dmsService.updateDocument(id, request, currentUser.getWorkEmail());
            return ResponseEntity.ok(ApiResponse.success("Document updated successfully", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "DMS_001"));
        }
    }

    @Operation(summary = "Delete Document", description = "Deletes a document.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Document deleted successfully",
            content = @Content(schema = @Schema(example = "{\"success\": true, \"message\": \"Document deleted successfully\"}"))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Document not found")
    })
    @DeleteMapping("/documents/{id}")
    public ResponseEntity<?> deleteDocument(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        Optional<DmsDocumentResponse> docOpt = dmsService.getDocumentById(id);
        if (docOpt.isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Document not found with ID: " + id, "DMS_002"));

        DmsDocumentResponse doc = docOpt.get();

        // Check permission: manager or owner
        if (!isManager(currentUser) && !isDocumentOwner(currentUser, doc)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: You do not have permissions to delete this document.",
                            "AUTH_002"));
        }

        boolean deleted = dmsService.deleteDocument(id, currentUser.getWorkEmail());
        if (deleted) {
            return ResponseEntity.ok(ApiResponse.success("Document deleted successfully", null));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Document not found with ID: " + id, "DMS_002"));
        }
    }

    // ── 4. APPROVAL / REJECTION ──────────────────────────────────────────────
    @Operation(summary = "Approve Document", description = "Approves a document status.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Document approved successfully",
            content = @Content(schema = @Schema(implementation = DmsDocumentResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Document not found")
    })
    @PatchMapping("/documents/{id}/approve")
    public ResponseEntity<?> approveDocument(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        if (!isManager(currentUser))
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires HR/Manager permissions.", "AUTH_002"));

        Optional<DmsDocumentResponse> approved = dmsService.approveDocument(id, currentUser.getWorkEmail());
        if (approved.isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Document not found with ID: " + id, "DMS_002"));

        return ResponseEntity.ok(ApiResponse.success("Document approved successfully", approved.get()));
    }

    @Operation(summary = "Reject Document", description = "Rejects a document status.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Document rejected successfully",
            content = @Content(schema = @Schema(implementation = DmsDocumentResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Document not found")
    })
    @PatchMapping("/documents/{id}/reject")
    public ResponseEntity<?> rejectDocument(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        if (!isManager(currentUser))
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires HR/Manager permissions.", "AUTH_002"));

        Optional<DmsDocumentResponse> rejected = dmsService.rejectDocument(id, currentUser.getWorkEmail());
        if (rejected.isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Document not found with ID: " + id, "DMS_002"));

        return ResponseEntity.ok(ApiResponse.success("Document rejected successfully", rejected.get()));
    }

    // ── 5. VERSIONS ──────────────────────────────────────────────────────────
    @Operation(summary = "Add Document Version", description = "Uploads a new version of the document.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "New version uploaded successfully",
            content = @Content(schema = @Schema(implementation = DmsDocumentVersionResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Document not found")
    })
    @PostMapping("/documents/{id}/versions")
    public ResponseEntity<?> addVersion(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @Valid @RequestBody DmsDocumentVersionRequest request) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        Optional<DmsDocumentResponse> docOpt = dmsService.getDocumentById(id);
        if (docOpt.isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Document not found with ID: " + id, "DMS_002"));

        DmsDocumentResponse doc = docOpt.get();

        // Check permission: manager or owner
        if (!isManager(currentUser) && !isDocumentOwner(currentUser, doc)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: You cannot upload versions for this document.",
                            "AUTH_002"));
        }

        try {
            DmsDocumentVersionResponse version = dmsService.addVersion(id, request, currentUser.getWorkEmail());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("New version uploaded successfully", version));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "DMS_003"));
        }
    }

    // ── 6. SHARES ────────────────────────────────────────────────────────────
    @Operation(summary = "Share Document", description = "Shares document with other employees.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Document shared successfully",
            content = @Content(schema = @Schema(implementation = DmsDocumentShareResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Document not found")
    })
    @PostMapping("/documents/{id}/shares")
    public ResponseEntity<?> shareDocument(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @Valid @RequestBody DmsDocumentShareRequest request) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        Optional<DmsDocumentResponse> docOpt = dmsService.getDocumentById(id);
        if (docOpt.isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Document not found with ID: " + id, "DMS_002"));

        DmsDocumentResponse doc = docOpt.get();

        // Check permission: manager or owner
        if (!isManager(currentUser) && !isDocumentOwner(currentUser, doc)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: You cannot share this document.", "AUTH_002"));
        }

        try {
            DmsDocumentShareResponse share = dmsService.shareDocument(id, request, currentUser.getWorkEmail());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Document shared successfully", share));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "DMS_004"));
        }
    }

    // ── 7. AUDIT LOGS ────────────────────────────────────────────────────────
    @Operation(summary = "Get Document Audit Logs", description = "Retrieves all audit history entries for a document.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Document audit logs retrieved successfully",
            content = @Content(array = @io.swagger.v3.oas.annotations.media.ArraySchema(schema = @Schema(implementation = DmsDocumentAuditLogResponse.class)))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Document not found")
    })
    @GetMapping("/documents/{id}/audit-logs")
    public ResponseEntity<?> getAuditLogs(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        Optional<DmsDocumentResponse> docOpt = dmsService.getDocumentById(id);
        if (docOpt.isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Document not found with ID: " + id, "DMS_002"));

        DmsDocumentResponse doc = docOpt.get();

        // Check permission: manager or owner
        if (!isManager(currentUser) && !isDocumentOwner(currentUser, doc)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error(
                            "Access Denied: You do not have permission to view audit logs for this document.",
                            "AUTH_002"));
        }

        List<DmsDocumentAuditLogResponse> logs = dmsService.getAuditLogs(id);
        return ResponseEntity.ok(ApiResponse.success("Document audit logs retrieved successfully", logs));
    }

    // ── 8. EXPIRING DOCUMENTS ────────────────────────────────────────────────
    @Operation(summary = "Get Expiring Documents", description = "Retrieves documents nearing expiration.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Expiring documents retrieved successfully",
            content = @Content(array = @io.swagger.v3.oas.annotations.media.ArraySchema(schema = @Schema(implementation = DmsDocumentResponse.class)))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/documents/expiring")
    public ResponseEntity<?> getExpiringDocuments(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        if (isManager(currentUser)) {
            List<DmsDocumentResponse> docs = dmsService.getExpiringDocuments();
            return ResponseEntity.ok(ApiResponse.success("Expiring documents retrieved successfully", docs));
        } else {
            Employee emp = employeeRepository.findByEmail(currentUser.getWorkEmail()).orElse(null);
            if (emp == null)
                return ResponseEntity.ok(ApiResponse.success("Expiring documents retrieved successfully", List.of()));

            List<DmsDocumentResponse> docs = dmsService.getExpiringDocumentsByEmployee(emp.getId());
            return ResponseEntity.ok(ApiResponse.success("My expiring documents retrieved successfully", docs));
        }
    }

    // ── 9. SIGNATURE REQUESTS ────────────────────────────────────────────────
    @Operation(summary = "Submit or Sign Signature Request", description = "Requests signature or signs document.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Document signature requested successfully",
            content = @Content(schema = @Schema(implementation = DmsSignatureResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Document signature logged successfully",
            content = @Content(schema = @Schema(implementation = DmsSignatureResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad Request"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/documents/{id}/signature-request")
    public ResponseEntity<?> signatureRequest(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestBody @Valid DocumentSignatureRequest body) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        // Dynamically routing:
        // A. If user is manager and requesting a signature
        if (isManager(currentUser) && body != null && body.employeeId() != null) {
            DmsSignatureRequest request = new DmsSignatureRequest();
            request.setEmployeeId(body.employeeId());
            request.setComments(body.comments());

            try {
                DmsSignatureResponse response = dmsService.submitSignatureRequest(id, request,
                        currentUser.getWorkEmail());
                return ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success("Document signature requested successfully", response));
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "DMS_005"));
            }
        }

        // B. If employee is completing/signing the signature request
        if (body != null && body.status() != null && !body.status().isBlank()) {
            DmsSignatureCompleteRequest request = new DmsSignatureCompleteRequest();
            request.setStatus(body.status());
            request.setComments(body.comments());

            try {
                DmsSignatureResponse response = dmsService.completeSignature(id, request, currentUser.getWorkEmail());
                return ResponseEntity.ok(ApiResponse.success("Document signature logged successfully", response));
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "DMS_006"));
            }
        }

        return ResponseEntity.badRequest()
                .body(ErrorResponse.error(
                        "Invalid request payload. Include 'employeeId' to request signature or 'status' to sign document.",
                        "VAL_001"));
    }

    // ── 10. REPORTS ──────────────────────────────────────────────────────────
    @Operation(summary = "Get Document Reports", description = "Generates document metrics reports by type.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Document report generated successfully",
            content = @Content(schema = @Schema(implementation = DmsReportResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Requires HR/Manager permissions")
    })
    @GetMapping("/documents/reports/{reportType}")
    public ResponseEntity<?> getReports(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String reportType) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        if (!isManager(currentUser))
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires HR/Manager permissions.", "AUTH_002"));

        Map<String, Object> data = dmsService.getReports(reportType);
        return ResponseEntity.ok(ApiResponse.success("Document report generated successfully", data));
    }
}
