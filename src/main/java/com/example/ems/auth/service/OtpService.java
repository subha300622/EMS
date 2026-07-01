package com.example.ems.auth.service;

import com.example.ems.auth.entity.OtpRedisToken;
import com.example.ems.auth.entity.OtpToken;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.OtpTokenRepository;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.common.service.EmailService;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

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
    @Autowired private SafeRedisService safeRedisService;
    @Autowired private OtpTokenRepository otpTokenRepository;
    @Autowired private org.springframework.core.env.Environment environment;
    @Autowired private SessionService sessionService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Map<String, Object> otpLocks = new ConcurrentHashMap<>();
    private final Map<String, Object> resetTokenLocks = new ConcurrentHashMap<>();

    // Helper: Redis Key formatters
    private String getOtpKey(String email) {
        return "otp:" + email.trim().toLowerCase();
    }

    private String getResetTokenKey(String token) {
        return "reset:" + token.trim();
    }

    // ──────────────────────────────────────────────────────────────────────
    //  STEP 1: Forgot Password — generate & send OTP (DB-backed, cache optional)
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
        java.util.List<String> activeProfiles = java.util.Arrays.asList(environment.getActiveProfiles());
        boolean isProduction = activeProfiles.contains("prod") || activeProfiles.contains("production");

        boolean cooldownActive = false;
        long waitSeconds = 0;

        // Try Redis first
        String existingJson = isProduction ? safeRedisService.get(otpKey) : null;
        if (existingJson != null) {
            try {
                OtpRedisToken existingToken = objectMapper.readValue(existingJson, OtpRedisToken.class);
                LocalDateTime createdAt = LocalDateTime.parse(existingToken.getCreatedAt());
                long secondsSinceCreated = Duration.between(createdAt, LocalDateTime.now()).getSeconds();
                if (secondsSinceCreated < RESEND_COOLDOWN_SECONDS) {
                    cooldownActive = true;
                    waitSeconds = RESEND_COOLDOWN_SECONDS - secondsSinceCreated;
                }
            } catch (Exception e) {
                log.warn("Failed to parse existing OTP token from Redis: {}", e.getMessage());
            }
        }

        // If not found or not active in Redis, double-checked locking read from DB
        if (isProduction && !cooldownActive) {
            Object lock = otpLocks.computeIfAbsent(email, k -> new Object());
            synchronized (lock) {
                // Recheck cache inside lock
                existingJson = safeRedisService.get(otpKey);
                if (existingJson != null) {
                    try {
                        OtpRedisToken existingToken = objectMapper.readValue(existingJson, OtpRedisToken.class);
                        LocalDateTime createdAt = LocalDateTime.parse(existingToken.getCreatedAt());
                        long secondsSinceCreated = Duration.between(createdAt, LocalDateTime.now()).getSeconds();
                        if (secondsSinceCreated < RESEND_COOLDOWN_SECONDS) {
                            cooldownActive = true;
                            waitSeconds = RESEND_COOLDOWN_SECONDS - secondsSinceCreated;
                        }
                    } catch (Exception ignored) {}
                }

                if (!cooldownActive) {
                    // Check DB
                    try {
                        Optional<OtpToken> dbOtpOpt = otpTokenRepository.findByEmail(email);
                        if (dbOtpOpt.isPresent()) {
                            OtpToken dbOtp = dbOtpOpt.get();
                            if (dbOtp.getOtpHash() != null) {
                                long secondsSinceCreated = Duration.between(dbOtp.getCreatedAt(), LocalDateTime.now()).getSeconds();
                                if (secondsSinceCreated < RESEND_COOLDOWN_SECONDS) {
                                    cooldownActive = true;
                                    waitSeconds = RESEND_COOLDOWN_SECONDS - secondsSinceCreated;
                                    
                                    // Cache it back to Redis
                                    try {
                                        OtpRedisToken cacheToken = new OtpRedisToken(dbOtp.getOtpHash(), dbOtp.getAttemptCount(), false, dbOtp.getCreatedAt().toString());
                                        String json = objectMapper.writeValueAsString(cacheToken);
                                        long ttl = Duration.between(LocalDateTime.now(), dbOtp.getExpiryTime()).getSeconds();
                                        if (ttl > 0) {
                                            safeRedisService.set(otpKey, json, Duration.ofSeconds(ttl));
                                        }
                                    } catch (Exception ignored) {}
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.warn("Failed to check OTP cooldown in DB: {}", e.getMessage());
                    }
                }
            }
        }

        if (cooldownActive) {
            response.put("message", "Please wait " + waitSeconds + " seconds before requesting another OTP.");
            return response;
        }

        // Generate 6-digit OTP using SecureRandom
        String otp = String.format("%06d", new SecureRandom().nextInt(1_000_000));
        log.info("====================================");
        log.info("GENERATED OTP FOR {}: {}", email, otp);
        log.info("====================================");
        String otpHash = passwordEncoder.encode(otp);

        // Authoritative DB Write/Update
        try {
            Optional<OtpToken> dbOtpOpt = otpTokenRepository.findByEmail(email);
            OtpToken dbOtp;
            if (dbOtpOpt.isPresent()) {
                dbOtp = dbOtpOpt.get();
                dbOtp.setOtpHash(otpHash);
                dbOtp.setAttemptCount(0);
                dbOtp.setCreatedAt(LocalDateTime.now());
                dbOtp.setExpiryTime(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES));
                dbOtp.setResetToken(null);
            } else {
                dbOtp = new OtpToken(
                        email,
                        otpHash,
                        0,
                        LocalDateTime.now(),
                        LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES)
                );
            }
            otpTokenRepository.save(dbOtp);
            log.info("OTP saved in DB for user {}", email);
        } catch (Exception e) {
            log.error("Failed to save OTP to DB", e);
            response.put("message", "Failed to initialize OTP. Please try again.");
            return response;
        }

        // Optional Cache Set
        try {
            OtpRedisToken cacheToken = new OtpRedisToken(otpHash, 0, false, LocalDateTime.now().toString());
            String json = objectMapper.writeValueAsString(cacheToken);
            safeRedisService.set(otpKey, json, Duration.ofMinutes(OTP_EXPIRY_MINUTES));
        } catch (Exception e) {
            log.warn("Failed to cache OTP to Redis: {}", e.getMessage());
        }

        try {
            log.info("=== Attempting to send OTP email to: {} ===", email);
            emailService.sendOtpEmail(email, otp);
            log.info("=== OTP email sent successfully to: {} ===", email);
        } catch (Exception e) {
            log.warn("=== FAILED to send OTP email to: {} (keeping token in DB/Redis for local testing) ===", email);
            log.warn("Exception message: {}", e.getMessage());
        }

        response.put("otp", otp);
        response.put("message", safeMessage);
        return response;
    }

    // ──────────────────────────────────────────────────────────────────────
    //  STEP 2: Verify OTP — returns short-lived reset token (DB-backed)
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
        OtpRedisToken redisToken = null;

        // Try Redis cache first
        String cachedJson = safeRedisService.get(otpKey);
        if (cachedJson != null) {
            try {
                redisToken = objectMapper.readValue(cachedJson, OtpRedisToken.class);
            } catch (Exception e) {
                log.warn("Failed to parse cached OTP: {}", e.getMessage());
            }
        }

        OtpToken dbOtp = null;
        if (redisToken == null) {
            // Double-Checked Locking DB query
            Object lock = otpLocks.computeIfAbsent(email, k -> new Object());
            synchronized (lock) {
                // Re-verify cache inside lock
                cachedJson = safeRedisService.get(otpKey);
                if (cachedJson != null) {
                    try {
                        redisToken = objectMapper.readValue(cachedJson, OtpRedisToken.class);
                    } catch (Exception ignored) {}
                }

                if (redisToken == null) {
                    // Query DB
                    try {
                        Optional<OtpToken> dbOtpOpt = otpTokenRepository.findByEmail(email);
                        if (dbOtpOpt.isPresent()) {
                            OtpToken candidate = dbOtpOpt.get();
                            if (LocalDateTime.now().isAfter(candidate.getExpiryTime())) {
                                // Expired DB entry, cleanup
                                try {
                                    otpTokenRepository.delete(candidate);
                                } catch (Exception ignored) {}
                            } else if (candidate.getOtpHash() == null) {
                                // Already verified
                            } else {
                                dbOtp = candidate;
                                // Cache it back to Redis
                                redisToken = new OtpRedisToken(
                                        dbOtp.getOtpHash(),
                                        dbOtp.getAttemptCount(),
                                        false,
                                        dbOtp.getCreatedAt().toString()
                                );
                                String json = objectMapper.writeValueAsString(redisToken);
                                long ttl = Duration.between(LocalDateTime.now(), dbOtp.getExpiryTime()).getSeconds();
                                if (ttl > 0) {
                                    safeRedisService.set(otpKey, json, Duration.ofSeconds(ttl));
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.warn("Failed to fetch OTP from DB: {}", e.getMessage());
                    }
                }
            }
        }

        // If neither Redis nor DB has a valid unexpired OTP token
        if (redisToken == null && dbOtp == null) {
            response.put("verified", false);
            response.put("message", "No active OTP found. Please request a new one.");
            return response;
        }

        // We have either redisToken or dbOtp (or both). Unify variables.
        String currentOtpHash = redisToken != null ? redisToken.getOtpHash() : dbOtp.getOtpHash();
        int currentAttemptCount = redisToken != null ? redisToken.getAttemptCount() : dbOtp.getAttemptCount();

        // Check attempts
        if (currentAttemptCount >= MAX_OTP_ATTEMPTS) {
            // Delete from DB and Redis
            try {
                Optional<OtpToken> dbOtpOpt = otpTokenRepository.findByEmail(email);
                dbOtpOpt.ifPresent(token -> otpTokenRepository.delete(token));
            } catch (Exception e) {
                log.warn("Failed to delete max-attempts OTP from DB: {}", e.getMessage());
            }
            safeRedisService.delete(otpKey);

            response.put("verified", false);
            response.put("message", "Too many incorrect attempts. Please request a new OTP.");
            return response;
        }

        // Match OTP hash
        if (!passwordEncoder.matches(otp, currentOtpHash)) {
            int newAttemptCount = currentAttemptCount + 1;
            int remaining = MAX_OTP_ATTEMPTS - newAttemptCount;

            // Update in DB (authoritative)
            try {
                Optional<OtpToken> dbOtpOpt = otpTokenRepository.findByEmail(email);
                if (dbOtpOpt.isPresent()) {
                    OtpToken t = dbOtpOpt.get();
                    t.setAttemptCount(newAttemptCount);
                    if (remaining <= 0) {
                        otpTokenRepository.delete(t);
                    } else {
                        otpTokenRepository.save(t);
                    }
                }
            } catch (Exception e) {
                log.error("Failed to update OTP attempt count in DB", e);
            }

            if (remaining <= 0) {
                safeRedisService.delete(otpKey);
                response.put("verified", false);
                response.put("message", "Too many incorrect attempts. Please request a new OTP.");
            } else {
                // Update in Redis
                if (redisToken != null) {
                    redisToken.setAttemptCount(newAttemptCount);
                    try {
                        String updatedJson = objectMapper.writeValueAsString(redisToken);
                        Long ttl = safeRedisService.getExpire(otpKey);
                        if (ttl != null && ttl > 0) {
                            safeRedisService.set(otpKey, updatedJson, Duration.ofSeconds(ttl));
                        } else {
                            safeRedisService.set(otpKey, updatedJson, Duration.ofMinutes(OTP_EXPIRY_MINUTES));
                        }
                    } catch (Exception e) {
                        log.warn("Failed to update OTP attempt count in Redis: {}", e.getMessage());
                    }
                }
                response.put("verified", false);
                response.put("message", "Incorrect OTP. " + remaining + " attempt(s) remaining.");
            }
            return response;
        }

        // Correct OTP -> issue short-lived reset token (stored in DB and optionally cached in Redis)
        String resetToken = UUID.randomUUID().toString();
        String resetTokenKey = getResetTokenKey(resetToken);

        // 1. Authoritative DB Update
        try {
            Optional<OtpToken> dbOtpOpt = otpTokenRepository.findByEmail(email);
            if (dbOtpOpt.isPresent()) {
                OtpToken t = dbOtpOpt.get();
                t.setOtpHash(null); // invalidate OTP
                t.setResetToken(resetToken);
                t.setCreatedAt(LocalDateTime.now());
                t.setExpiryTime(LocalDateTime.now().plusMinutes(RESET_TOKEN_EXPIRY_MINUTES));
                otpTokenRepository.save(t);
            } else {
                OtpToken t = new OtpToken(email, null, 0, LocalDateTime.now(), LocalDateTime.now().plusMinutes(RESET_TOKEN_EXPIRY_MINUTES));
                t.setResetToken(resetToken);
                otpTokenRepository.save(t);
            }
            log.info("Reset token generated and saved in DB for {}", email);
        } catch (Exception e) {
            log.error("Failed to save reset token to DB", e);
            response.put("verified", false);
            response.put("message", "Failed to complete OTP verification. Please try again.");
            return response;
        }

        // 2. Optional Cache updates
        safeRedisService.set(resetTokenKey, email, Duration.ofMinutes(RESET_TOKEN_EXPIRY_MINUTES));
        safeRedisService.delete(otpKey);

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
    //  STEP 3: Reset Password — validate reset token (DB-backed)
    // ──────────────────────────────────────────────────────────────────────

    public Map<String, String> resetPassword(String resetToken, String newPassword) {
        Map<String, String> response = new LinkedHashMap<>();

        String resetTokenKey = getResetTokenKey(resetToken);
        String email = safeRedisService.get(resetTokenKey);

        // If cache miss, search DB using Double-Checked Locking
        if (email == null) {
            Object lock = resetTokenLocks.computeIfAbsent(resetToken, k -> new Object());
            synchronized (lock) {
                // Re-verify cache inside lock
                email = safeRedisService.get(resetTokenKey);
                if (email == null) {
                    try {
                        Optional<OtpToken> dbOtpOpt = otpTokenRepository.findByResetToken(resetToken);
                        if (dbOtpOpt.isPresent()) {
                            OtpToken dbOtp = dbOtpOpt.get();
                            if (LocalDateTime.now().isBefore(dbOtp.getExpiryTime())) {
                                email = dbOtp.getEmail();
                                // Cache it back to Redis
                                safeRedisService.set(resetTokenKey, email, Duration.ofMinutes(RESET_TOKEN_EXPIRY_MINUTES));
                            }
                        }
                    } catch (Exception e) {
                        log.warn("Failed to retrieve reset token from DB: {}", e.getMessage());
                    }
                }
            }
        }

        if (email == null) {
            response.put("message", "Invalid or expired reset token.");
            return response;
        }

        Optional<User> optUser = userRepository.findByWorkEmail(email);
        if (optUser.isEmpty()) {
            // Cleanup
            try {
                Optional<OtpToken> dbOtpOpt = otpTokenRepository.findByResetToken(resetToken);
                dbOtpOpt.ifPresent(token -> otpTokenRepository.delete(token));
            } catch (Exception ignored) {}
            safeRedisService.delete(resetTokenKey);

            response.put("message", "User account not found.");
            return response;
        }

        // BCrypt hash and save user
        User user = optUser.get();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Revoke all sessions immediately on password reset
        sessionService.revokeAllSessions(user.getUserId());

        // Remove token from DB and Redis
        try {
            Optional<OtpToken> dbOtpOpt = otpTokenRepository.findByResetToken(resetToken);
            dbOtpOpt.ifPresent(token -> otpTokenRepository.delete(token));
        } catch (Exception e) {
            log.warn("Failed to delete reset token from DB: {}", e.getMessage());
        }
        safeRedisService.delete(resetTokenKey);

        log.info("Password reset successfully for user: {}", email);

        response.put("message", "Password reset successfully. Please login with your new password.");
        return response;
    }
}
