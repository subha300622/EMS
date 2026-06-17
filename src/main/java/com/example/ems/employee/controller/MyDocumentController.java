package com.example.ems.employee.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.employee.dto.*;
import com.example.ems.employee.entity.MyEmployeeDocument;
import com.example.ems.employee.service.MyDocumentService;
import com.example.ems.security.service.JwtService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/my-documents")
@CrossOrigin("*")
@Tag(name = "My Documents")
public class MyDocumentController {

    @Autowired
    private MyDocumentService documentService;

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
        if (user == null) return false;
        return roleService.hasPermission(user.getWorkEmail(), permission)
                || roleService.hasPermission(user.getWorkEmail(), "document.employee.manage")
                || roleService.hasPermission(user.getWorkEmail(), "document.employee.read")
                || roleService.isSuperAdmin(user.getWorkEmail());
    }

    private ResponseEntity<?> unauthorizedResponse() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
    }

    private ResponseEntity<?> forbiddenResponse(String permission) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.error("Access Denied: Requires '" + permission + "' permission.", "AUTH_002"));
    }

    // 1. Get My Documents Dashboard
    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return unauthorizedResponse();
        }
        if (!checkPermission(currentUser, "document.self.read")) {
            return forbiddenResponse("document.self.read");
        }

        try {
            MyDocumentsDashboardResponse response = documentService.getDocumentDashboard(currentUser.getWorkEmail());
            return ResponseEntity.ok(ApiResponse.success("Dashboard retrieved successfully", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "DOC_001"));
        }
    }

    // 2. Get Document Categories
    @GetMapping("/categories")
    public ResponseEntity<?> getCategories(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return unauthorizedResponse();
        }
        if (!checkPermission(currentUser, "document.self.read")) {
            return forbiddenResponse("document.self.read");
        }

        try {
            MyDocumentCategoriesResponse response = documentService.getDocumentCategories(currentUser.getWorkEmail());
            return ResponseEntity.ok(ApiResponse.success("Categories retrieved successfully", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "DOC_001"));
        }
    }

    // 3. Get Documents by Category
    @GetMapping("/categories/{categoryId}/documents")
    public ResponseEntity<?> getDocumentsByCategory(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable("categoryId") Long categoryId) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return unauthorizedResponse();
        }
        if (!checkPermission(currentUser, "document.self.read")) {
            return forbiddenResponse("document.self.read");
        }

        try {
            MyCategoryDocumentsResponse response = documentService.getDocumentsByCategory(currentUser.getWorkEmail(), categoryId);
            return ResponseEntity.ok(ApiResponse.success("Category documents retrieved successfully", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "DOC_001"));
        }
    }

    // 4. Upload Document
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadDocument(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @RequestParam("file") MultipartFile file,
            @RequestParam("categoryId") Long categoryId,
            @RequestParam("documentType") String documentType,
            @RequestParam(value = "documentNumber", required = false) String documentNumber,
            @RequestParam(value = "issuedDate", required = false) String issuedDateStr,
            @RequestParam(value = "expiryDate", required = false) String expiryDateStr,
            @RequestParam(value = "remarks", required = false) String remarks) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return unauthorizedResponse();
        }
        if (!checkPermission(currentUser, "document.self.upload")) {
            return forbiddenResponse("document.self.upload");
        }

        try {
            LocalDate issuedDate = (issuedDateStr != null && !issuedDateStr.trim().isEmpty()) ? LocalDate.parse(issuedDateStr.trim()) : null;
            LocalDate expiryDate = (expiryDateStr != null && !expiryDateStr.trim().isEmpty()) ? LocalDate.parse(expiryDateStr.trim()) : null;

            MyDocumentUploadResponse response = documentService.uploadDocument(
                    currentUser.getWorkEmail(), categoryId, documentType, file, documentNumber, issuedDate, expiryDate, remarks
            );
            return ResponseEntity.ok(ApiResponse.success("Document uploaded successfully", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.error(e.getMessage(), "DOC_002"));
        }
    }

    // 5. Replace Existing Document
    @PutMapping(value = "/{documentId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> replaceDocument(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable("documentId") Long documentId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "remarks", required = false) String remarks) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return unauthorizedResponse();
        }
        if (!checkPermission(currentUser, "document.self.update")) {
            return forbiddenResponse("document.self.update");
        }

        try {
            MyDocumentReplaceResponse response = documentService.replaceDocument(
                    currentUser.getWorkEmail(), documentId, file, remarks
            );
            return ResponseEntity.ok(ApiResponse.success("Document replaced successfully", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.error(e.getMessage(), "DOC_002"));
        }
    }

    // 6. Get Document Details
    @GetMapping("/{documentId}")
    public ResponseEntity<?> getDocumentDetails(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable("documentId") Long documentId) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return unauthorizedResponse();
        }
        if (!checkPermission(currentUser, "document.self.read")) {
            return forbiddenResponse("document.self.read");
        }

        try {
            MyDocumentDetailsResponse response = documentService.getDocumentDetails(currentUser.getWorkEmail(), documentId);
            return ResponseEntity.ok(ApiResponse.success("Document details retrieved successfully", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "DOC_001"));
        }
    }

    // 7. Preview/View Document
    @GetMapping("/{documentId}/preview")
    public ResponseEntity<?> previewDocument(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable("documentId") Long documentId) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return unauthorizedResponse();
        }
        if (!checkPermission(currentUser, "document.self.preview")) {
            return forbiddenResponse("document.self.preview");
        }

        try {
            MyDocumentPreviewResponse response = documentService.previewDocument(currentUser.getWorkEmail(), documentId);
            return ResponseEntity.ok(ApiResponse.success("Document preview generated successfully", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "DOC_001"));
        }
    }

    // 8. Download Document
    @GetMapping("/{documentId}/download")
    public ResponseEntity<?> downloadDocument(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable("documentId") Long documentId) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return unauthorizedResponse();
        }
        if (!checkPermission(currentUser, "document.self.download")) {
            return forbiddenResponse("document.self.download");
        }

        try {
            MyEmployeeDocument doc = documentService.downloadDocument(currentUser.getWorkEmail(), documentId);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(doc.getFileType()));
            headers.setContentDispositionFormData("attachment", doc.getFileName());
            headers.setContentLength(doc.getFileData().length);
            return new ResponseEntity<>(doc.getFileData(), headers, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "DOC_001"));
        }
    }

    // 9. Get Expiry Notifications
    @GetMapping("/notifications")
    public ResponseEntity<?> getExpiryNotifications(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return unauthorizedResponse();
        }
        if (!checkPermission(currentUser, "document.self.read")) {
            return forbiddenResponse("document.self.read");
        }

        try {
            MyDocumentNotificationsResponse response = documentService.getExpiryNotifications(currentUser.getWorkEmail());
            return ResponseEntity.ok(ApiResponse.success("Expiry notifications retrieved successfully", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "DOC_001"));
        }
    }

    // 10. Get Document Activity History
    @GetMapping("/history")
    public ResponseEntity<?> getDocumentHistory(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return unauthorizedResponse();
        }
        if (!checkPermission(currentUser, "document.self.history.read")) {
            return forbiddenResponse("document.self.history.read");
        }

        try {
            MyDocumentHistoryResponse response = documentService.getDocumentActivityHistory(currentUser.getWorkEmail(), page, size);
            return ResponseEntity.ok(ApiResponse.success("Document activity history retrieved successfully", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "DOC_001"));
        }
    }

    // 11. Get Allowed Document Types
    @GetMapping("/document-types")
    public ResponseEntity<?> getAllowedDocumentTypes(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return unauthorizedResponse();
        }
        if (!checkPermission(currentUser, "document.self.read")) {
            return forbiddenResponse("document.self.read");
        }

        try {
            MyDocumentTypesResponse response = documentService.getAllowedDocumentTypes();
            return ResponseEntity.ok(ApiResponse.success("Allowed document types retrieved successfully", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "DOC_001"));
        }
    }
}
