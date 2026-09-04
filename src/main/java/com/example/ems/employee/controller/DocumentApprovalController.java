package com.example.ems.employee.controller;

import com.example.ems.approval.dto.ApprovalActionRequest;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.security.service.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/documents")
@CrossOrigin("*")
@Tag(name = "Document Approvals", description = "Domain APIs for Document Review and Verification")
public class DocumentApprovalController {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

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

    @Operation(summary = "Approve Document Verification", description = "Approves a submitted employee document")
    @PostMapping("/{documentId}/approve")
    @PreAuthorize("hasAuthority('DOCUMENT_APPROVE')")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> approveDocument(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long documentId,
            @RequestBody(required = false) ApprovalActionRequest request) {

        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        String comment = request != null ? request.getComment() : "Approved";
        Map<String, Object> response = Map.of("documentId", documentId, "status", "APPROVED", "comment", comment);
        return ResponseEntity.ok(ApiResponse.success("Document approved successfully", response));
    }

    @Operation(summary = "Reject Document Verification", description = "Rejects a submitted employee document")
    @PostMapping("/{documentId}/reject")
    @PreAuthorize("hasAuthority('DOCUMENT_REJECT')")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> rejectDocument(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long documentId,
            @RequestBody(required = false) ApprovalActionRequest request) {

        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        String comment = request != null ? request.getComment() : "Rejected";
        Map<String, Object> response = Map.of("documentId", documentId, "status", "REJECTED", "comment", comment);
        return ResponseEntity.ok(ApiResponse.success("Document rejected successfully", response));
    }

    @Operation(summary = "Send Back Document Verification", description = "Requests re-upload or revision of a document")
    @PostMapping("/{documentId}/send-back")
    @PreAuthorize("hasAuthority('DOCUMENT_APPROVE')")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> sendBackDocument(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long documentId,
            @RequestBody(required = false) ApprovalActionRequest request) {

        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        String comment = request != null ? request.getComment() : "Sent back";
        Map<String, Object> response = Map.of("documentId", documentId, "status", "NEEDS_REVISION", "comment", comment);
        return ResponseEntity.ok(ApiResponse.success("Document sent back for revision successfully", response));
    }
}
