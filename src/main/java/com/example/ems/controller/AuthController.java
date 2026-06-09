package com.example.ems.controller;

import com.example.ems.dto.ForgotPasswordRequest;
import com.example.ems.dto.ResetPasswordRequest;
import com.example.ems.dto.VerifyOtpRequest;
import com.example.ems.service.OtpService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin("*")
public class AuthController {

    @Autowired
    private OtpService otpService;

    /**
     * Step 1 — User submits their email.
     * Generates a 6-digit OTP, hashes it, stores it, and sends to email.
     *
     * POST /api/auth/forgot-password
     * Body: { "email": "employee@company.com" }
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(
            @RequestBody @Valid ForgotPasswordRequest request) {
        return ResponseEntity.ok(otpService.forgotPassword(request.getEmail()));
    }

    /**
     * Step 2 — User enters the 6-digit OTP from their email.
     * Returns a short-lived resetToken (5 min) on success.
     *
     * POST /api/auth/verify-otp
     * Body: { "email": "employee@company.com", "otp": "123456" }
     */
    @PostMapping("/verify-otp")
    public ResponseEntity<Map<String, Object>> verifyOtp(
            @RequestBody @Valid VerifyOtpRequest request) {
        Map<String, Object> result = otpService.verifyOtp(request.getEmail(), request.getOtp());
        boolean verified = Boolean.TRUE.equals(result.get("verified"));
        return verified
                ? ResponseEntity.ok(result)
                : ResponseEntity.badRequest().body(result);
    }

    /**
     * Resend OTP — subject to 60-second cooldown.
     *
     * POST /api/auth/resend-otp
     * Body: { "email": "employee@company.com" }
     */
    @PostMapping("/resend-otp")
    public ResponseEntity<Map<String, String>> resendOtp(
            @RequestBody @Valid ForgotPasswordRequest request) {
        return ResponseEntity.ok(otpService.resendOtp(request.getEmail()));
    }

    /**
     * Step 3 — User sets their new password using the resetToken from Step 2.
     * Password is BCrypt-hashed before saving.
     *
     * POST /api/auth/reset-password
     * Body: { "resetToken": "uuid-here", "newPassword": "NewPass@123" }
     */
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(
            @RequestBody @Valid ResetPasswordRequest request) {
        Map<String, String> result = otpService.resetPassword(
                request.getResetToken(), request.getNewPassword());
        boolean success = result.getOrDefault("message", "")
                .startsWith("Password reset successfully");
        return success
                ? ResponseEntity.ok(result)
                : ResponseEntity.badRequest().body(result);
    }
}
