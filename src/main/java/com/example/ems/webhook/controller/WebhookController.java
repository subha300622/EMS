package com.example.ems.webhook.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.security.service.JwtService;
import com.example.ems.webhook.entity.WebhookSubscription;
import com.example.ems.webhook.repository.WebhookSubscriptionRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/webhooks")
@CrossOrigin("*")
@Tag(name = "Webhook Integration")
public class WebhookController {

    @Autowired
    private WebhookSubscriptionRepository subscriptionRepository;

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

    private boolean isAdmin(User user) {
        if (user == null) return false;
        return roleService.isSuperAdmin(user.getWorkEmail())
                || roleService.hasRoleOrGreater(user, "ADMIN")
                || roleService.hasPermission(user.getWorkEmail(), "settings.manage");
    }

    @Operation(summary = "Get all webhook subscriptions")
    @GetMapping
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getSubscriptions(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(required = false) String status) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!isAdmin(currentUser)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires Admin privileges.", "AUTH_002"));
        }

        List<WebhookSubscription> subscriptions;
        if (status != null && !status.trim().isEmpty()) {
            subscriptions = subscriptionRepository.findByStatus(status.trim().toUpperCase());
        } else {
            subscriptions = subscriptionRepository.findAll();
        }
        return ResponseEntity.ok(ApiResponse.success("Webhook subscriptions retrieved successfully", subscriptions));
    }

    @Operation(summary = "Create a new webhook subscription")
    @PostMapping
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> createSubscription(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody WebhookSubscription subscription) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!isAdmin(currentUser)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires Admin privileges.", "AUTH_002"));
        }

        subscription.setCreatedAt(LocalDateTime.now());
        subscription.setUpdatedAt(LocalDateTime.now());
        if (subscription.getStatus() == null) {
            subscription.setStatus("ACTIVE");
        }
        WebhookSubscription saved = subscriptionRepository.save(subscription);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Webhook subscription created successfully", saved));
    }

    @Operation(summary = "Update an existing webhook subscription")
    @PutMapping("/{id}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> updateSubscription(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestBody WebhookSubscription details) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!isAdmin(currentUser)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires Admin privileges.", "AUTH_002"));
        }

        Optional<WebhookSubscription> subOpt = subscriptionRepository.findById(id);
        if (subOpt.isEmpty()) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Webhook subscription not found with ID: " + id, "WEB_001"));
        }

        WebhookSubscription sub = subOpt.get();
        sub.setUrl(details.getUrl());
        sub.setSecretKey(details.getSecretKey());
        sub.setEvents(details.getEvents());
        sub.setStatus(details.getStatus());
        sub.setUpdatedAt(LocalDateTime.now());
        WebhookSubscription saved = subscriptionRepository.save(sub);

        return ResponseEntity.ok(ApiResponse.success("Webhook subscription updated successfully", saved));
    }

    @Operation(summary = "Delete an existing webhook subscription")
    @DeleteMapping("/{id}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> deleteSubscription(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!isAdmin(currentUser)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires Admin privileges.", "AUTH_002"));
        }

        if (!subscriptionRepository.existsById(id)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Webhook subscription not found with ID: " + id, "WEB_001"));
        }

        subscriptionRepository.deleteById(id);
        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("message", "Webhook subscription deleted successfully");
        return ResponseEntity.ok(ApiResponse.success("Webhook subscription deleted successfully", res));
    }
}
