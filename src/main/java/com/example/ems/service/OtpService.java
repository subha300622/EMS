package com.example.ems.service;

import com.example.ems.entity.PasswordResetToken;
import com.example.ems.entity.User;
import com.example.ems.repository.PasswordResetTokenRepository;
import com.example.ems.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class OtpService {

    private static final Logger log = LoggerFactory.getLogger(OtpService.class);

    private static final int OTP_EXPIRY_MINUTES        = 10;
    private static final int RESET_TOKEN_EXPIRY_MINUTES = 5;
    private static final int MAX_OTP_ATTEMPTS          = 5;
    private static final int RESEND_COOLDOWN_SECONDS   = 60;

    @Autowired private UserRepository userRepository;
    @Autowired private PasswordResetTokenRepository tokenRepository;
    @Autowired private EmailService emailService;
    @Autowired private BCryptPasswordEncoder passwordEncoder;

    // ──────────────────────────────────────────────────────────────────────
    //  STEP 1: Forgot Password — generate & send OTP
    // ──────────────────────────────────────────────────────────────────────

    public Map<String, String> forgotPassword(String email) {
        Map<String, String> response = new LinkedHashMap<>();
        // Security: always return same message to prevent email enumeration
        String safeMessage = "If the email exists, a reset code has been sent.";

        Optional<User> optUser = userRepository.findByWorkEmail(email);
        if (optUser.isEmpty()) {
            response.put("message", safeMessage);
            return response;
        }

        User user = optUser.get();

        // Check 60-second resend cooldown
        Optional<PasswordResetToken> existing = tokenRepository.findByUser(user);
        if (existing.isPresent()) {
            PasswordResetToken old = existing.get();
            long secondsSinceCreated = java.time.Duration.between(
                    old.getCreatedAt(), LocalDateTime.now()).getSeconds();
            if (secondsSinceCreated < RESEND_COOLDOWN_SECONDS) {
                long wait = RESEND_COOLDOWN_SECONDS - secondsSinceCreated;
                response.put("message", "Please wait " + wait + " seconds before requesting another OTP.");
                return response;
            }
            tokenRepository.delete(old);
        }

        // Generate 6-digit OTP using SecureRandom
        String otp = String.format("%06d", new SecureRandom().nextInt(1_000_000));
        log.info("====================================");
        log.info("GENERATED OTP FOR {}: {}", email, otp);
        log.info("====================================");
        String otpHash = passwordEncoder.encode(otp);

        PasswordResetToken token = new PasswordResetToken();
        token.setUser(user);
        token.setOtpHash(otpHash);
        token.setExpiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES));
        token.setCreatedAt(LocalDateTime.now());
        tokenRepository.save(token);

        try {
            log.info("=== Attempting to send OTP email to: {} ===", email);
            emailService.sendOtpEmail(email, otp);
            log.info("=== OTP email sent successfully to: {} ===", email);
        } catch (Exception e) {
            tokenRepository.delete(token);
            log.error("=== FAILED to send OTP to: {} ===", email);
            log.error("Exception type: {}", e.getClass().getName());
            log.error("Exception message: {}", e.getMessage());
            if (e.getCause() != null) {
                log.error("Caused by: {}", e.getCause().getMessage());
            }
            response.put("message", "Failed to send OTP. Error: " + e.getMessage());
            return response;
        }

        response.put("message", safeMessage);
        return response;
    }

    // ──────────────────────────────────────────────────────────────────────
    //  STEP 2: Verify OTP — returns short-lived reset token
    // ──────────────────────────────────────────────────────────────────────

    public Map<String, Object> verifyOtp(String email, String otp) {
        Map<String, Object> response = new LinkedHashMap<>();

        Optional<User> optUser = userRepository.findByWorkEmail(email);
        if (optUser.isEmpty()) {
            response.put("verified", false);
            response.put("message", "Invalid email or OTP.");
            return response;
        }

        User user = optUser.get();
        Optional<PasswordResetToken> optToken = tokenRepository.findByUser(user);

        if (optToken.isEmpty()) {
            response.put("verified", false);
            response.put("message", "No active OTP found. Please request a new one.");
            return response;
        }

        PasswordResetToken token = optToken.get();

        // Check OTP expiry
        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            tokenRepository.delete(token);
            response.put("verified", false);
            response.put("message", "OTP has expired. Please request a new one.");
            return response;
        }

        // Check max attempts
        if (token.getAttemptCount() >= MAX_OTP_ATTEMPTS) {
            tokenRepository.delete(token);
            response.put("verified", false);
            response.put("message", "Too many incorrect attempts. Please request a new OTP.");
            return response;
        }

        // Verify OTP hash
        if (!passwordEncoder.matches(otp, token.getOtpHash())) {
            token.setAttemptCount(token.getAttemptCount() + 1);
            tokenRepository.save(token);
            int remaining = MAX_OTP_ATTEMPTS - token.getAttemptCount();
            response.put("verified", false);
            response.put("message", "Incorrect OTP. " + remaining + " attempt(s) remaining.");
            return response;
        }

        // OTP correct — generate short-lived reset token (5 min)
        String resetToken = UUID.randomUUID().toString();
        token.setVerified(true);
        token.setResetToken(resetToken);
        token.setResetTokenExpiresAt(LocalDateTime.now().plusMinutes(RESET_TOKEN_EXPIRY_MINUTES));
        tokenRepository.save(token);

        log.info("OTP verified for: {}. Reset token issued.", email);

        response.put("verified", true);
        response.put("resetToken", resetToken);
        return response;
    }

    // ──────────────────────────────────────────────────────────────────────
    //  RESEND OTP
    // ──────────────────────────────────────────────────────────────────────

    public Map<String, String> resendOtp(String email) {
        // Resend uses same logic as forgotPassword (cooldown is enforced inside)
        return forgotPassword(email);
    }

    // ──────────────────────────────────────────────────────────────────────
    //  STEP 3: Reset Password — validate reset token, BCrypt new password
    // ──────────────────────────────────────────────────────────────────────

    public Map<String, String> resetPassword(String resetToken, String newPassword) {
        Map<String, String> response = new LinkedHashMap<>();

        Optional<PasswordResetToken> optToken = tokenRepository.findByResetToken(resetToken);

        if (optToken.isEmpty()) {
            response.put("message", "Invalid or expired reset token.");
            return response;
        }

        PasswordResetToken token = optToken.get();

        if (!token.isVerified()) {
            response.put("message", "OTP not verified. Please verify your OTP first.");
            return response;
        }

        if (token.getResetTokenExpiresAt().isBefore(LocalDateTime.now())) {
            tokenRepository.delete(token);
            response.put("message", "Reset token has expired. Please start the process again.");
            return response;
        }

        // BCrypt hash the new password and update the user
        User user = token.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Delete the token — single use, logout all sessions
        tokenRepository.delete(token);

        log.info("Password reset successfully for user: {}", user.getWorkEmail());

        response.put("message", "Password reset successfully. Please login with your new password.");
        return response;
    }
}
