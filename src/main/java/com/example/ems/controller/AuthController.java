package com.example.ems.controller;

import com.example.ems.dto.*;
import com.example.ems.entity.Invitation;
import com.example.ems.entity.Role;
import com.example.ems.entity.User;
import com.example.ems.entity.Employee;
import com.example.ems.repository.InvitationRepository;
import com.example.ems.repository.RoleRepository;
import com.example.ems.repository.UserRepository;
import com.example.ems.repository.EmployeeRepository;
import com.example.ems.service.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin("*")
public class AuthController {

    @Autowired
    private OtpService otpService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private InvitationRepository invitationRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    // Helper: Resolve currently authenticated User via JWT only
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

    // Helper: Extract IP and User-Agent
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    // ── 1. LOGIN ─────────────────────────────────────────────────────────────
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequest request, HttpServletRequest httpRequest) {
        Optional<User> optUser = userRepository.findByWorkEmail(request.getEmail());
        if (optUser.isEmpty() || !passwordEncoder.matches(request.getPassword(), optUser.get().getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Invalid credentials", "AUTH_001"));
        }

        User user = optUser.get();
        String roleName = user.getRole() != null ? user.getRole().getName() : user.getRequestedRole();
        
        // Generate Tokens
        String accessToken = jwtService.generateAccessToken(user.getUserId(), user.getWorkEmail(), roleName);
        
        // Create session in Redis
        SessionService.SessionMetadata session = sessionService.createSession(
                user.getUserId(),
                user.getWorkEmail(),
                httpRequest.getHeader("User-Agent"),
                getClientIp(httpRequest)
        );

        LoginResponse.TokenData tokenData = new LoginResponse.TokenData(
                accessToken,
                session.getRefreshToken(),
                "Bearer",
                900,
                604800
        );

        LoginResponse.RoleData roleData = null;
        List<String> permissions = new java.util.ArrayList<>();
        if (user.getRole() != null) {
            roleData = new LoginResponse.RoleData(user.getRole().getId(), user.getRole().getName());
            permissions = user.getRole().getPermissions().stream()
                    .map(com.example.ems.entity.Permission::getName)
                    .collect(Collectors.toList());
        }

        LoginResponse.UserData userData = new LoginResponse.UserData(
                user.getId(),
                user.getUserId(),
                user.getFullName(),
                user.getWorkEmail(),
                roleData,
                permissions,
                "ACTIVE",
                Instant.now().toString()
        );

        LoginResponse.LoginData loginData = new LoginResponse.LoginData(tokenData, userData);
        LoginResponse responseBody = new LoginResponse(true, "Login successful", Instant.now().toString(), loginData);

        return ResponseEntity.ok(responseBody);
    }

    // ── 2. LOGOUT ────────────────────────────────────────────────────────────
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody @Valid LogoutRequest request) {
        sessionService.revokeSession(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully"));
    }

    // ── 3. REFRESH TOKEN ─────────────────────────────────────────────────────
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody @Valid RefreshTokenRequest request) {
        SessionService.SessionMetadata session = sessionService.rotateRefreshToken(request.getRefreshToken());
        if (session == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Invalid or expired refresh token", "AUTH_012"));
        }

        Optional<User> optUser = userRepository.findByWorkEmail(session.getEmail());
        if (optUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("User not found", "AUTH_013"));
        }

        User user = optUser.get();
        String roleName = user.getRole() != null ? user.getRole().getName() : user.getRequestedRole();
        
        String newAccessToken = jwtService.generateAccessToken(user.getUserId(), user.getWorkEmail(), roleName);
        
        return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", Map.of(
                "accessToken", newAccessToken,
                "refreshToken", session.getRefreshToken(),
                "expiresIn", 900
        )));
    }

    // ── 4. FORGOT PASSWORD ───────────────────────────────────────────────────
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(
            @RequestBody @Valid ForgotPasswordRequest request) {
        try {
            otpService.forgotPassword(request.getEmail());
        } catch (Exception e) {
            // Suppress error to prevent user enumeration
        }
        return ResponseEntity.ok(ApiResponse.success("If the account exists, an OTP has been sent."));
    }

    // ── 5. VERIFY OTP ────────────────────────────────────────────────────────
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(
            @RequestBody @Valid VerifyOtpRequest request) {
        Map<String, Object> result = otpService.verifyOtp(request.getEmail(), request.getOtp());
        boolean verified = Boolean.TRUE.equals(result.get("verified"));
        if (!verified) {
            return ResponseEntity.badRequest().body(ErrorResponse.error("Invalid or expired OTP", "AUTH_003"));
        }
        return ResponseEntity.ok(ApiResponse.success("OTP verified successfully", Map.of(
                "resetToken", result.get("resetToken"),
                "expiresIn", 600
        )));
    }

    // ── 6. RESET PASSWORD ───────────────────────────────────────────
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            return ResponseEntity.badRequest().body(ErrorResponse.error("Passwords do not match", "AUTH_004"));
        }
        try {
            otpService.resetPassword(request.getResetToken(), request.getNewPassword());
            return ResponseEntity.ok(ApiResponse.success("Password reset successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "AUTH_005"));
        }
    }

    // ── 7. CHANGE PASSWORD ───────────────────────────────────────────────────
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody @Valid ChangePasswordRequest request) {

        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            return ResponseEntity.badRequest().body(ErrorResponse.error("Current password does not match", "AUTH_015"));
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            return ResponseEntity.badRequest().body(ErrorResponse.error("Confirm password does not match", "AUTH_004"));
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        
        sessionService.revokeAllSessions(user.getUserId());

        return ResponseEntity.ok(ApiResponse.success("Password changed successfully"));
    }

    // ── 8. GET CURRENT USER ──────────────────────────────────────────────────
    @GetMapping("/me")
    public ResponseEntity<?> getMe(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        Map<String, Object> roleMap = null;
        List<String> permissions = new java.util.ArrayList<>();
        if (user.getRole() != null) {
            roleMap = Map.of("id", user.getRole().getId(), "name", user.getRole().getName());
            permissions = user.getRole().getPermissions().stream()
                    .map(com.example.ems.entity.Permission::getName)
                    .collect(Collectors.toList());
        }

        Map<String, Object> userData = new java.util.HashMap<>();
        userData.put("id", user.getId());
        userData.put("employeeId", user.getUserId());
        userData.put("name", user.getFullName());
        userData.put("email", user.getWorkEmail());
        userData.put("role", roleMap);
        userData.put("permissions", permissions);

        return ResponseEntity.ok(ApiResponse.success("User profile retrieved successfully", userData));
    }

    // ── 8b. VERIFY TOKEN ─────────────────────────────────────────────────────
    @GetMapping("/verify")
    public ResponseEntity<?> verifyToken(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        return ResponseEntity.ok(ApiResponse.success("Token is valid", Map.of(
                "valid", true,
                "employeeId", user.getUserId(),
                "email", user.getWorkEmail()
        )));
    }

    // ── 9. RESEND OTP ───────────────────────────────────────────────────────
    @PostMapping("/resend-otp")
    public ResponseEntity<?> resendOtp(
            @RequestBody @Valid ForgotPasswordRequest request) {
        otpService.resendOtp(request.getEmail());
        return ResponseEntity.ok(ApiResponse.success("OTP resent successfully"));
    }

    // ── 10. ACTIVE SESSIONS ──────────────────────────────────────────────────
    @GetMapping("/sessions")
    public ResponseEntity<?> getSessions(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            HttpServletRequest httpRequest) {

        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        List<SessionService.SessionMetadata> sessions = sessionService.getActiveSessions(user.getUserId());
        List<Map<String, Object>> sessionList = sessions.stream()
                .map(s -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("sessionId", s.getSessionId());
                    m.put("device", s.getUserAgent() != null ? s.getUserAgent() : "Unknown Device");
                    m.put("ipAddress", s.getIpAddress());
                    m.put("location", "Bangalore, India");
                    m.put("createdAt", s.getCreatedAt());
                    m.put("lastActiveAt", s.getCreatedAt());
                    m.put("current", s.getIpAddress().equals(getClientIp(httpRequest)));
                    return m;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("Active sessions retrieved successfully", sessionList));
    }

    // ── 11. REVOKE SESSION ───────────────────────────────────────────────────
    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<?> revokeSession(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String sessionId) {

        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        sessionService.revokeSessionById(user.getUserId(), sessionId);
        return ResponseEntity.ok(ApiResponse.success("Session revoked successfully"));
    }

    // ── 12. LOGOUT FROM ALL DEVICES ──────────────────────────────────────────
    @PostMapping("/logout-all")
    public ResponseEntity<?> logoutAll(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        sessionService.revokeAllSessions(user.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Logged out from all devices successfully"));
    }

    // ── 13. INVITE EMPLOYEE ──────────────────────────────────────────────────
    @PostMapping("/invite")
    public ResponseEntity<?> inviteEmployee(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody @Valid InviteRequest request) {

        User inviter = resolveUser(authHeader);
        if (inviter == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        String inviterRole = inviter.getRole() != null ? inviter.getRole().getName() : inviter.getRequestedRole();
        if (!"SUPER_ADMIN".equalsIgnoreCase(inviterRole) && !"ADMIN".equalsIgnoreCase(inviterRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Only Super Admin and Admin can invite employees", "AUTH_002"));
        }

        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new IllegalArgumentException("Employee not found with ID: " + request.getEmployeeId()));

        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new IllegalArgumentException("Role not found with ID: " + request.getRoleId()));

        if (userRepository.existsByWorkEmail(employee.getEmail())) {
            return ResponseEntity.badRequest().body(ErrorResponse.error("Employee email is already registered", "AUTH_006"));
        }

        if (invitationRepository.existsByEmail(employee.getEmail())) {
            return ResponseEntity.badRequest().body(ErrorResponse.error("An active invitation already exists for this employee", "AUTH_007"));
        }

        String token = UUID.randomUUID().toString();
        Invitation invitation = new Invitation();
        invitation.setName(employee.getFullName());
        invitation.setEmail(employee.getEmail());
        invitation.setRole(role.getName());
        invitation.setInvitationToken(token);
        invitation.setExpiredAt(LocalDateTime.now().plusHours(24));
        invitationRepository.save(invitation);

        emailService.sendInvitationEmail(employee.getEmail(), employee.getFullName(), role.getName(), token);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Invitation sent successfully", Map.of("expiresIn", 86400)));
    }

    // ── 14. ACCEPT INVITATION ────────────────────────────────────────────────
    @PostMapping("/accept-invitation")
    public ResponseEntity<?> acceptInvitation(@RequestBody @Valid AcceptInvitationRequest request) {
        Optional<Invitation> optInvitation = invitationRepository.findByInvitationToken(request.getInvitationToken());
        if (optInvitation.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Invalid invitation token", "AUTH_008"));
        }

        Invitation invitation = optInvitation.get();
        if (invitation.isAccepted()) {
            return ResponseEntity.badRequest().body(ErrorResponse.error("Invitation has already been accepted", "AUTH_009"));
        }

        if (invitation.getExpiredAt().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body(ErrorResponse.error("Invitation token has expired", "AUTH_010"));
        }

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            return ResponseEntity.badRequest().body(ErrorResponse.error("Passwords do not match", "AUTH_004"));
        }

        String normalizedRole = invitation.getRole().trim().toUpperCase().replace(" ", "_");
        if (normalizedRole.equals("SUPERADMIN")) {
            normalizedRole = "SUPER_ADMIN";
        }
        Optional<Role> optRole = roleRepository.findByName(normalizedRole);
        if (optRole.isEmpty()) {
            optRole = roleRepository.findByName(invitation.getRole());
        }
        if (optRole.isEmpty()) {
            return ResponseEntity.badRequest().body(ErrorResponse.error("Role '" + invitation.getRole() + "' does not exist", "AUTH_011"));
        }

        User user = new User();
        user.setFullName(invitation.getName());
        user.setWorkEmail(invitation.getEmail());
        user.setRequestedRole(invitation.getRole());
        user.setRole(optRole.get());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);

        String userId = "EMP" + String.format("%03d", user.getId());
        user.setUserId(userId);
        userRepository.save(user);

        invitation.setAccepted(true);
        invitationRepository.save(invitation);

        return ResponseEntity.ok(ApiResponse.success("Account activated successfully", Map.of(
                "employeeId", userId,
                "status", "ACTIVE"
        )));
    }
}
