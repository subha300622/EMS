package com.example.ems.controller;

import com.example.ems.dto.*;
import com.example.ems.entity.Employee;
import com.example.ems.entity.User;
import com.example.ems.repository.EmployeeRepository;
import com.example.ems.repository.UserRepository;
import com.example.ems.service.DmsService;
import com.example.ems.service.JwtService;
import com.example.ems.service.RoleService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin("*")
public class DmsController {

    @Autowired private DmsService dmsService;
    @Autowired private UserRepository userRepository;
    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private JwtService jwtService;
    @Autowired private RoleService roleService;

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
        if (user == null || doc == null) return false;
        return user.getEmployeeId() != null && user.getEmployeeId().equals(String.valueOf(doc.getEmployeeId()));
    }

    private boolean isDocumentSharedWith(User user, Long documentId) {
        if (user == null) return false;
        Employee emp = employeeRepository.findByEmail(user.getWorkEmail()).orElse(null);
        if (emp == null) return false;

        return dmsService.getSharesByDocument(documentId).stream()
                .anyMatch(s -> s.getSharedWithEmployeeId().equals(emp.getId()));
    }

    // ── 1. DASHBOARD ─────────────────────────────────────────────────────────
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
            if (currentUser.getEmployeeId() == null || !currentUser.getEmployeeId().equals(String.valueOf(request.getEmployeeId()))) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ErrorResponse.error("Access Denied: You cannot upload documents for another employee.", "AUTH_002"));
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

    @GetMapping("/documents")
    public ResponseEntity<?> getDocuments(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        // If manager, return all. Otherwise, return only employee's own/shared documents
        if (isManager(currentUser)) {
            List<DmsDocumentResponse> docs = dmsService.getDocuments();
            return ResponseEntity.ok(ApiResponse.success("Documents list retrieved successfully", docs));
        } else {
            List<DmsDocumentResponse> docs = dmsService.getMyDocuments(currentUser.getWorkEmail());
            return ResponseEntity.ok(ApiResponse.success("My documents list retrieved successfully", docs));
        }
    }

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
                    .body(ErrorResponse.error("Access Denied: You do not have permissions to view this document.", "AUTH_002"));
        }

        return ResponseEntity.ok(ApiResponse.success("Document details retrieved successfully", doc));
    }

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
                    .body(ErrorResponse.error("Access Denied: You do not have permissions to download this document.", "AUTH_002"));
        }

        Optional<DmsDocumentResponse> downloaded = dmsService.downloadDocument(id, currentUser.getWorkEmail());
        return ResponseEntity.ok(ApiResponse.success("Document downloaded successfully", downloaded.get()));
    }

    // ── 3. GET MY DOCUMENTS ──────────────────────────────────────────────────
    @GetMapping("/documents/my")
    public ResponseEntity<?> getMyDocuments(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        List<DmsDocumentResponse> docs = dmsService.getMyDocuments(currentUser.getWorkEmail());
        return ResponseEntity.ok(ApiResponse.success("My documents list retrieved successfully", docs));
    }

    // ── 4. APPROVAL / REJECTION ──────────────────────────────────────────────
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
                    .body(ErrorResponse.error("Access Denied: You cannot upload versions for this document.", "AUTH_002"));
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
                    .body(ErrorResponse.error("Access Denied: You do not have permission to view audit logs for this document.", "AUTH_002"));
        }

        List<DmsDocumentAuditLogResponse> logs = dmsService.getAuditLogs(id);
        return ResponseEntity.ok(ApiResponse.success("Document audit logs retrieved successfully", logs));
    }

    // ── 8. EXPIRING DOCUMENTS ────────────────────────────────────────────────
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
            if (emp == null) return ResponseEntity.ok(ApiResponse.success("Expiring documents retrieved successfully", List.of()));

            List<DmsDocumentResponse> docs = dmsService.getExpiringDocumentsByEmployee(emp.getId());
            return ResponseEntity.ok(ApiResponse.success("My expiring documents retrieved successfully", docs));
        }
    }

    // ── 9. SIGNATURE REQUESTS ────────────────────────────────────────────────
    @PostMapping("/documents/{id}/signature-request")
    public ResponseEntity<?> signatureRequest(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        // Dynamically routing:
        // A. If user is manager and requesting a signature
        if (isManager(currentUser) && body.containsKey("employeeId")) {
            DmsSignatureRequest request = new DmsSignatureRequest();
            request.setEmployeeId(((Number) body.get("employeeId")).longValue());
            request.setComments((String) body.get("comments"));

            try {
                DmsSignatureResponse response = dmsService.submitSignatureRequest(id, request, currentUser.getWorkEmail());
                return ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success("Document signature requested successfully", response));
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "DMS_005"));
            }
        }

        // B. If employee is completing/signing the signature request
        if (body.containsKey("status")) {
            DmsSignatureCompleteRequest request = new DmsSignatureCompleteRequest();
            request.setStatus((String) body.get("status"));
            request.setComments((String) body.get("comments"));

            try {
                DmsSignatureResponse response = dmsService.completeSignature(id, request, currentUser.getWorkEmail());
                return ResponseEntity.ok(ApiResponse.success("Document signature logged successfully", response));
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "DMS_006"));
            }
        }

        return ResponseEntity.badRequest()
                .body(ErrorResponse.error("Invalid request payload. Include 'employeeId' to request signature or 'status' to sign document.", "VAL_001"));
    }

    // ── 10. REPORTS ──────────────────────────────────────────────────────────
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
