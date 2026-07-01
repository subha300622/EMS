package com.example.ems.organization.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.organization.dto.PaymentDtos.*;
import com.example.ems.organization.service.PaymentService;
import com.example.ems.security.service.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/platform-admin/payments")
@CrossOrigin("*")
@Tag(name = "Platform Administration Payments", description = "Endpoints for processing subscription order invoices and refunds")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

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
                    .body(ErrorResponse.error("Access Denied: Requires platform admin permission.", "AUTH_002"));
        }
        return null;
    }

    @Operation(summary = "Create Payment Order")
    @PostMapping("/orders")
    public ResponseEntity<?> createOrder(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody @Valid CreateOrderRequest request) {

        ResponseEntity<?> accessError = validateAccess(authHeader, "organization.subscription");
        if (accessError != null) return accessError;

        User user = resolveUser(authHeader);
        try {
            PaymentOrderResponse response = paymentService.createPaymentOrder(request, user.getWorkEmail());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Payment order created successfully.", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "PAY_001"));
        }
    }

    @Operation(summary = "Verify Payment")
    @PostMapping("/verify")
    public ResponseEntity<?> verifyPayment(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody @Valid VerifyPaymentRequest request) {

        ResponseEntity<?> accessError = validateAccess(authHeader, "organization.subscription");
        if (accessError != null) return accessError;

        User user = resolveUser(authHeader);
        try {
            VerifyPaymentResponse response = paymentService.verifyPayment(request, user.getWorkEmail());
            return ResponseEntity.ok(ApiResponse.success("Payment verified successfully.", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "PAY_002"));
        }
    }

    @Operation(summary = "Payment Details")
    @GetMapping("/{paymentId}")
    public ResponseEntity<?> getPaymentDetails(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long paymentId) {

        ResponseEntity<?> accessError = validateAccess(authHeader, "organization.read");
        if (accessError != null) return accessError;

        try {
            PaymentDetailResponse response = paymentService.getPaymentDetails(paymentId);
            return ResponseEntity.ok(ApiResponse.success("Payment details retrieved", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.error(e.getMessage(), "PAY_003"));
        }
    }

    @Operation(summary = "Payment History")
    @GetMapping
    public ResponseEntity<?> getPaymentHistory(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        ResponseEntity<?> accessError = validateAccess(authHeader, "organization.read");
        if (accessError != null) return accessError;

        PaymentHistoryResponse response = paymentService.getPaymentHistory(page, size);
        return ResponseEntity.ok(ApiResponse.success("Payment history retrieved", response));
    }

    @Operation(summary = "Refund Payment")
    @PostMapping("/{paymentId}/refund")
    public ResponseEntity<?> refundPayment(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long paymentId,
            @RequestBody @Valid RefundPaymentRequest request) {

        ResponseEntity<?> accessError = validateAccess(authHeader, "organization.subscription");
        if (accessError != null) return accessError;

        User user = resolveUser(authHeader);
        try {
            RefundResponse response = paymentService.refundPayment(paymentId, request, user.getWorkEmail());
            return ResponseEntity.ok(ApiResponse.success("Refund initiated successfully.", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "PAY_004"));
        }
    }

    @Operation(summary = "Razorpay Webhook Endpoint")
    @PostMapping("/webhook")
    public ResponseEntity<?> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("X-Razorpay-Signature") String signature) {
        try {
            paymentService.processWebhook(payload, signature);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "PAY_WEBHOOK_ERROR"));
        }
    }
}
