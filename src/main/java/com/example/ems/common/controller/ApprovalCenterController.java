package com.example.ems.common.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.common.service.ApprovalCenterService;
import com.example.ems.security.service.JwtService;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/approvals")
@CrossOrigin("*")
@Tag(name = "Approvals")
public class ApprovalCenterController {

    @Autowired
    private ApprovalCenterService approvalCenterService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleService roleService;

    @Autowired
    private JwtService jwtService;

    @GetMapping
    public ResponseEntity<?> getPendingApprovals(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "team.read")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'team.read' permission.", "AUTH_002"));
        }

        return ResponseEntity.ok(ApiResponse.success("Pending approvals retrieved successfully",
                approvalCenterService.getPendingApprovals()));
    }

    @GetMapping("/pending")
    public ResponseEntity<?> getPendingApprovalsAlias(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return getPendingApprovals(authHeader);
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<?> approveItem(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String id) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "team.read")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'team.read' permission.", "AUTH_002"));
        }

        try {
            approvalCenterService.approveItem(id, currentUser.getWorkEmail());
            return ResponseEntity.ok(ApiResponse.success("Item approved successfully", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "APP_001"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.error("Failed to approve item: " + e.getMessage(), "APP_002"));
        }
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<?> rejectItem(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String id) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "team.read")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'team.read' permission.", "AUTH_002"));
        }

        try {
            approvalCenterService.rejectItem(id, currentUser.getWorkEmail());
            return ResponseEntity.ok(ApiResponse.success("Item rejected successfully", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "APP_001"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.error("Failed to reject item: " + e.getMessage(), "APP_002"));
        }
    }

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
}
