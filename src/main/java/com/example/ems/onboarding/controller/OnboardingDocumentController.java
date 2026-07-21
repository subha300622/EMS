package com.example.ems.onboarding.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.security.context.SecurityContextFacade;
import com.example.ems.onboarding.dto.OnboardingDocumentResponse;
import com.example.ems.onboarding.dto.OnboardingDocumentVerifyRequest;
import com.example.ems.onboarding.service.OnboardingDocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

@RestController
@RequestMapping("/api/v1/onboarding")
@CrossOrigin("*")
@Tag(name = "Onboarding Document Management")
public class OnboardingDocumentController {

    @Autowired
    private OnboardingDocumentService documentService;

    @Autowired
    private SecurityContextFacade securityContextFacade;

    @Autowired
    private UserRepository userRepository;

    private User getAuthenticatedUser() {
        String email = securityContextFacade.getEmail();
        if (email == null) return null;
        return userRepository.findByWorkEmail(email).orElse(null);
    }

    private Long parseOnboardingId(String onbIdStr) {
        if (onbIdStr == null) {
            throw new IllegalArgumentException("Onboarding ID is required");
        }
        String clean = onbIdStr.trim();
        if (clean.toLowerCase().startsWith("onb-")) {
            clean = clean.substring(4);
        }
        try {
            return Long.parseLong(clean);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid Onboarding ID format: " + onbIdStr);
        }
    }

    private Long parseDocumentId(String docIdStr) {
        if (docIdStr == null) {
            throw new IllegalArgumentException("Document ID is required");
        }
        String clean = docIdStr.trim();
        if (clean.toLowerCase().startsWith("doc-")) {
            clean = clean.substring(4);
        }
        try {
            return Long.parseLong(clean);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid Document ID format: " + docIdStr);
        }
    }

    @GetMapping("/{onboardingId}/documents")
    @Operation(summary = "Get Onboarding Documents List")
    public ResponseEntity<ApiResponse<List<OnboardingDocumentResponse>>> getDocuments(
            @PathVariable String onboardingId) {
        Long parsedOnbId = parseOnboardingId(onboardingId);
        List<OnboardingDocumentResponse> response = documentService.getDocuments(parsedOnbId);
        return ResponseEntity.ok(ApiResponse.success("Documents retrieved successfully", response));
    }

    @PostMapping(value = "/{onboardingId}/documents/{documentId}/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload Onboarding Document File")
    public ResponseEntity<Object> uploadDocument(
            @PathVariable String onboardingId,
            @PathVariable String documentId,
            @RequestParam("file") MultipartFile file) {
        User user = getAuthenticatedUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        try {
            Long parsedOnbId = parseOnboardingId(onboardingId);
            Long parsedDocId = parseDocumentId(documentId);
            OnboardingDocumentResponse response = documentService.uploadDocument(parsedOnbId, parsedDocId, file, user);
            return ResponseEntity.ok(ApiResponse.success("Document uploaded successfully", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "VAL_004"));
        } catch (org.springframework.security.access.AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error(e.getMessage(), "AUTH_002"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.error("Failed to upload document: " + e.getMessage(), "STO_001"));
        }
    }

    @GetMapping("/{onboardingId}/documents/{documentId}/download")
    @Operation(summary = "Download Onboarding Document File")
    public ResponseEntity<Object> downloadDocument(
            @PathVariable String onboardingId,
            @PathVariable String documentId) {
        User user = getAuthenticatedUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        try {
            Long parsedOnbId = parseOnboardingId(onboardingId);
            Long parsedDocId = parseDocumentId(documentId);
            InputStream stream = documentService.downloadDocument(parsedOnbId, parsedDocId, user);
            InputStreamResource resource = new InputStreamResource(stream);

            // Fetch document to obtain original filename
            List<OnboardingDocumentResponse> docs = documentService.getDocuments(parsedOnbId);
            String fileName = docs.stream()
                    .filter(d -> d.getId().equals(documentId))
                    .map(OnboardingDocumentResponse::getFileName)
                    .findFirst()
                    .orElse("document.pdf");

            MediaType mediaType = MediaTypeFactory.getMediaType(fileName).orElse(MediaType.APPLICATION_OCTET_STREAM);
            String contentDisposition = String.format("attachment; filename=\"%s\"", fileName);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                    .contentType(mediaType)
                    .body(resource);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "VAL_004"));
        } catch (org.springframework.security.access.AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error(e.getMessage(), "AUTH_002"));
        } catch (com.example.ems.common.exception.ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "STO_003"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.error("Failed to download document: " + e.getMessage(), "STO_004"));
        }
    }

    @PatchMapping("/{onboardingId}/documents/{documentId}/verify")
    @Operation(summary = "Verify or Reject Onboarding Document")
    public ResponseEntity<Object> verifyDocument(
            @PathVariable String onboardingId,
            @PathVariable String documentId,
            @Valid @RequestBody OnboardingDocumentVerifyRequest request) {
        User user = getAuthenticatedUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        String role = user.getRole() != null ? user.getRole().getName() : "EMPLOYEE";
        if (!"HR".equalsIgnoreCase(role) && !"ADMIN".equalsIgnoreCase(role) && !"SUPER_ADMIN".equalsIgnoreCase(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Only HR or Admin is authorized to verify documents", "AUTH_002"));
        }

        try {
            Long parsedOnbId = parseOnboardingId(onboardingId);
            Long parsedDocId = parseDocumentId(documentId);
            OnboardingDocumentResponse response = documentService.verifyDocument(parsedOnbId, parsedDocId, request);
            return ResponseEntity.ok(ApiResponse.success("Document verification updated successfully", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "VAL_004"));
        } catch (com.example.ems.common.exception.ConflictException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ErrorResponse.error(e.getMessage(), "CON_409"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.error("Failed to verify document: " + e.getMessage(), "STO_002"));
        }
    }

    @DeleteMapping("/{onboardingId}/documents/{documentId}")
    @Operation(summary = "Delete or Reset Onboarding Document")
    public ResponseEntity<Object> deleteDocument(
            @PathVariable String onboardingId,
            @PathVariable String documentId) {
        User user = getAuthenticatedUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        try {
            Long parsedOnbId = parseOnboardingId(onboardingId);
            Long parsedDocId = parseDocumentId(documentId);
            documentService.deleteDocument(parsedOnbId, parsedDocId, user);
            return ResponseEntity.ok(ApiResponse.success("Document deleted successfully", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "VAL_004"));
        } catch (org.springframework.security.access.AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error(e.getMessage(), "AUTH_002"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.error("Failed to delete document: " + e.getMessage(), "STO_002"));
        }
    }
}
