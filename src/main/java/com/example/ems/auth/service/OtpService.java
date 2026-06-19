package com.example.ems.auth.service;

import com.example.ems.auth.entity.OtpRedisToken;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.common.service.EmailService;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
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
    @Autowired private EmailService emailService;
    @Autowired private BCryptPasswordEncoder passwordEncoder;
    @Autowired private StringRedisTemplate redisTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Helper: Redis Key formatters
    private String getOtpKey(String email) {
        return "otp:" + email.trim().toLowerCase();
    }

    private String getResetTokenKey(String token) {
        return "reset:" + token.trim();
    }

    // ──────────────────────────────────────────────────────────────────────
    //  STEP 1: Forgot Password — generate & send OTP (Redis-backed)
    // ──────────────────────────────────────────────────────────────────────

    public Map<String, String> forgotPassword(String email) {
        Map<String, String> response = new LinkedHashMap<>();
        String safeMessage = "If the email exists, a reset code has been sent.";

        Optional<User> optUser = userRepository.findByWorkEmail(email);
        if (optUser.isEmpty()) {
            response.put("message", safeMessage);
            return response;
        }

        String otpKey = getOtpKey(email);

        // Check 60-second cooldown from Redis
        String existingJson = redisTemplate.opsForValue().get(otpKey);
        if (existingJson != null) {
            try {
                OtpRedisToken existingToken = objectMapper.readValue(existingJson, OtpRedisToken.class);
                LocalDateTime createdAt = LocalDateTime.parse(existingToken.getCreatedAt());
                long secondsSinceCreated = Duration.between(createdAt, LocalDateTime.now()).getSeconds();
                if (secondsSinceCreated < RESEND_COOLDOWN_SECONDS) {
                    long wait = RESEND_COOLDOWN_SECONDS - secondsSinceCreated;
                    response.put("message", "Please wait " + wait + " seconds before requesting another OTP.");
                    return response;
                }
            } catch (Exception e) {
                log.warn("Failed to parse existing OTP token from Redis: {}", e.getMessage());
            }
        }

        // Generate 6-digit OTP using SecureRandom
        String otp = String.format("%06d", new SecureRandom().nextInt(1_000_000));
        log.info("====================================");
        log.info("GENERATED OTP FOR {}: {}", email, otp);
        log.info("====================================");
        String otpHash = passwordEncoder.encode(otp);

        // Save OtpRedisToken JSON in Redis
        OtpRedisToken token = new OtpRedisToken(otpHash, 0, false, LocalDateTime.now().toString());
        try {
            String json = objectMapper.writeValueAsString(token);
            redisTemplate.opsForValue().set(otpKey, json, Duration.ofMinutes(OTP_EXPIRY_MINUTES));
        } catch (Exception e) {
            log.error("Failed to write OTP token to Redis: {}", e.getMessage());
            response.put("message", "Failed to initialize OTP. Please try again.");
            return response;
        }

        try {
            log.info("=== Attempting to send OTP email to: {} ===", email);
            emailService.sendOtpEmail(email, otp);
            log.info("=== OTP email sent successfully to: {} ===", email);
        } catch (Exception e) {
            log.warn("=== FAILED to send OTP email to: {} (keeping token in Redis for local testing) ===", email);
            log.warn("Exception message: {}", e.getMessage());
        }

        response.put("otp", otp);
        response.put("message", safeMessage);
        return response;
    }

    // ──────────────────────────────────────────────────────────────────────
    //  STEP 2: Verify OTP — returns short-lived reset token (Redis-backed)
    // ──────────────────────────────────────────────────────────────────────

    public Map<String, Object> verifyOtp(String email, String otp) {
        Map<String, Object> response = new LinkedHashMap<>();

        Optional<User> optUser = userRepository.findByWorkEmail(email);
        if (optUser.isEmpty()) {
            response.put("verified", false);
            response.put("message", "Invalid email or OTP.");
            return response;
        }

        String otpKey = getOtpKey(email);
        String json = redisTemplate.opsForValue().get(otpKey);
        if (json == null) {
            response.put("verified", false);
            response.put("message", "No active OTP found. Please request a new one.");
            return response;
        }

        OtpRedisToken token;
        try {
            token = objectMapper.readValue(json, OtpRedisToken.class);
        } catch (Exception e) {
            log.error("Failed to parse OTP token from Redis during verification: {}", e.getMessage());
            response.put("verified", false);
            response.put("message", "Error verifying OTP. Please request a new one.");
            return response;
        }

        // Check attempts
        if (token.getAttemptCount() >= MAX_OTP_ATTEMPTS) {
            redisTemplate.delete(otpKey);
            response.put("verified", false);
            response.put("message", "Too many incorrect attempts. Please request a new OTP.");
            return response;
        }

        // Match OTP hash
        if (!passwordEncoder.matches(otp, token.getOtpHash())) {
            token.setAttemptCount(token.getAttemptCount() + 1);
            int remaining = MAX_OTP_ATTEMPTS - token.getAttemptCount();
            if (remaining <= 0) {
                redisTemplate.delete(otpKey);
                response.put("verified", false);
                response.put("message", "Too many incorrect attempts. Please request a new OTP.");
            } else {
                try {
                    String updatedJson = objectMapper.writeValueAsString(token);
                    // Keep the remaining TTL
                    Long ttl = redisTemplate.getExpire(otpKey);
                    if (ttl != null && ttl > 0) {
                        redisTemplate.opsForValue().set(otpKey, updatedJson, Duration.ofSeconds(ttl));
                    } else {
                        redisTemplate.opsForValue().set(otpKey, updatedJson, Duration.ofMinutes(OTP_EXPIRY_MINUTES));
                    }
                } catch (Exception e) {
                    log.error("Failed to update attempts in Redis: {}", e.getMessage());
                }
                response.put("verified", false);
                response.put("message", "Incorrect OTP. " + remaining + " attempt(s) remaining.");
            }
            return response;
        }

        // Correct OTP -> issue short-lived reset token (stored in Redis)
        String resetToken = UUID.randomUUID().toString();
        String resetTokenKey = getResetTokenKey(resetToken);

        // Store email mapping to token, expires in 5 minutes
        redisTemplate.opsForValue().set(resetTokenKey, email, Duration.ofMinutes(RESET_TOKEN_EXPIRY_MINUTES));
        
        // Remove OTP from Redis
        redisTemplate.delete(otpKey);

        log.info("OTP verified for: {}. Reset token issued.", email);

        response.put("verified", true);
        response.put("resetToken", resetToken);
        return response;
    }

    // ──────────────────────────────────────────────────────────────────────
    //  RESEND OTP
    // ──────────────────────────────────────────────────────────────────────

    public Map<String, String> resendOtp(String email) {
        return forgotPassword(email);
    }

    // ──────────────────────────────────────────────────────────────────────
    //  STEP 3: Reset Password — validate reset token (Redis-backed)
    // ──────────────────────────────────────────────────────────────────────

    public Map<String, String> resetPassword(String resetToken, String newPassword) {
        Map<String, String> response = new LinkedHashMap<>();

        String resetTokenKey = getResetTokenKey(resetToken);
        String email = redisTemplate.opsForValue().get(resetTokenKey);

        if (email == null) {
            response.put("message", "Invalid or expired reset token.");
            return response;
        }

        Optional<User> optUser = userRepository.findByWorkEmail(email);
        if (optUser.isEmpty()) {
            redisTemplate.delete(resetTokenKey);
            response.put("message", "User account not found.");
            return response;
        }

        // BCrypt hash and save user
        User user = optUser.get();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Remove token from Redis
        redisTemplate.delete(resetTokenKey);

        log.info("Password reset successfully for user: {}", email);

        response.put("message", "Password reset successfully. Please login with your new password.");
        return response;
    }
}
