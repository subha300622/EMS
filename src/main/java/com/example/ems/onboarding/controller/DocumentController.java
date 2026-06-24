package com.example.ems.onboarding.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.onboarding.entity.OnboardingDocument;
import com.example.ems.onboarding.service.TeamOnboardingService;
import com.example.ems.security.service.JwtService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/onboarding")
@CrossOrigin("*")
@Tag(name = "Canonical Documents Service")
public class DocumentController {

    @Autowired
    private TeamOnboardingService teamOnboardingService;

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

    private boolean checkManagerPermission(User user) {
        if (user == null) return false;
        return roleService.hasPermission(user.getWorkEmail(), "employee.create")
                || roleService.hasPermission(user.getWorkEmail(), "employee.update")
                || roleService.hasPermission(user.getWorkEmail(), "recruitment.manage");
    }

    @GetMapping("/documents")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getDocumentsQueue(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(required = false) String status) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkManagerPermission(currentUser)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires manager privileges.", "AUTH_002"));
        }

        List<OnboardingDocument> list = teamOnboardingService.getPendingVerifications();
        if (status != null && !status.isBlank()) {
            list = list.stream().filter(d -> status.equalsIgnoreCase(d.getVerificationStatus())).toList();
        }
        return ResponseEntity.ok(ApiResponse.success("Pending verification document queue retrieved", list));
    }

    @PatchMapping("/{id}/documents/{docId}/verify")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> verifyDocument(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @PathVariable Long docId,
            @RequestBody Map<String, String> body) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        String status = body.get("status");
        String remarks = body.containsKey("remarks") ? body.get("remarks") : body.get("notes");

        if (status == null || status.isBlank()) {
            return (ResponseEntity) ResponseEntity.badRequest()
                    .body(ErrorResponse.error("status field is required", "VAL_001"));
        }

        try {
            Map<String, String> result = teamOnboardingService.verifyDocument(docId, status, remarks);
            return ResponseEntity.ok(ApiResponse.success("Document verification status updated successfully", result));
        } catch (Exception e) {
            return (ResponseEntity) ResponseEntity.badRequest()
                    .body(ErrorResponse.error(e.getMessage(), "ONB_005"));
        }
    }
}
