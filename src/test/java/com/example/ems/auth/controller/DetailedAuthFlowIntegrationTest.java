package com.example.ems.auth.controller;

import com.example.ems.auth.dto.*;
import com.example.ems.auth.entity.Role;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.entity.UserSession;
import com.example.ems.auth.repository.RoleRepository;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.repository.UserSessionRepository;
import com.example.ems.auth.service.MockEmailService;
import com.example.ems.security.JwtAuthenticationFilter;
import com.example.ems.security.RateLimitingFilter;
import com.example.ems.config.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import org.springframework.security.core.context.SecurityContextHolder;
import java.util.Optional;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
public class DetailedAuthFlowIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private AuthController authController;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private AuthenticationEntryPoint authenticationEntryPoint;

    @Autowired
    private Environment environment;

    @Autowired
    private RateLimitingFilter rateLimitingFilter;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserSessionRepository userSessionRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Autowired
    private MockEmailService mockEmailService;

    private static final String TEST_EMAIL = "detailedaflow@company.com";
    private static final String TEST_PASSWORD = "initialpassword123";
    private static final String NEW_PASSWORD = "hardenedpassword456";
    private User testUser;

    @BeforeEach
    public void setUp() {
        SecurityContextHolder.clearContext();
        JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter(
                authenticationManager, authenticationEntryPoint, environment
        );

        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .addFilters(jwtAuthenticationFilter, rateLimitingFilter)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        cleanup();
        mockEmailService.clear();

        Role employeeRole = roleRepository.findByName("EMPLOYEE")
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setName("EMPLOYEE");
                    r.setDescription("Employee role");
                    return roleRepository.save(r);
                });

        testUser = new User();
        testUser.setFullName("Detailed Flow User");
        testUser.setWorkEmail(TEST_EMAIL);
        testUser.setMobileNumber("9998887777");
        testUser.setDepartment("Security");
        testUser.setRequestedRole("EMPLOYEE");
        testUser.setRole(employeeRole);
        testUser.setPassword(passwordEncoder.encode(TEST_PASSWORD));
        testUser.setStatus("ACTIVE");
        testUser.setLocation("Headquarters");
        testUser = userRepository.save(testUser);

        testUser.setUserId("EMP" + String.format("%03d", testUser.getId()));
        testUser = userRepository.save(testUser);
    }

    @AfterEach
    public void tearDown() {
        cleanup();
        mockEmailService.clear();
        SecurityContextHolder.clearContext();
    }

    private void cleanup() {
        Optional<User> existing = userRepository.findByWorkEmail(TEST_EMAIL);
        if (existing.isPresent()) {
            User u = existing.get();
            userSessionRepository.findAll().stream()
                    .filter(s -> u.getUserId().equals(s.getUserId()))
                    .forEach(s -> userSessionRepository.delete(s));
            userRepository.delete(u);
        }
    }

    @Test
    public void testCompleteEndToEndVerificationFlow() throws Exception {
        // === 1. Standard Login ===
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(TEST_EMAIL);
        loginRequest.setPassword(TEST_PASSWORD);

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn();

        String loginResp = loginResult.getResponse().getContentAsString();
        String accessToken = objectMapper.readTree(loginResp).path("data").path("tokens").path("accessToken").asText();
        String refreshToken = objectMapper.readTree(loginResp).path("data").path("tokens").path("refreshToken").asText();

        assertNotNull(accessToken);
        assertNotNull(refreshToken);

        // === 2. Verify Session was created in DB ===
        Optional<UserSession> activeSessionOpt = userSessionRepository.findByRefreshToken(refreshToken);
        assertTrue(activeSessionOpt.isPresent());
        UserSession activeSession = activeSessionOpt.get();
        assertEquals("ACTIVE", activeSession.getStatus());
        assertFalse(activeSession.isRevoked());

        // === 3. Access /me protected endpoint ===
        mockMvc.perform(get("/api/v1/auth/me")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value(TEST_EMAIL));

        // === 4. Access /sessions and verify current session indicator ===
        mockMvc.perform(get("/api/v1/auth/sessions")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].sessionId").value(activeSession.getSessionId()))
                .andExpect(jsonPath("$.data[0].current").value(true));

        // === 5. Forgot Password triggers OTP ===
        ForgotPasswordRequest forgotRequest = new ForgotPasswordRequest();
        forgotRequest.setEmail(TEST_EMAIL);

        mockMvc.perform(post("/api/v1/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(forgotRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // === 6. Retrieve OTP from In-Memory Mock Mailbox ===
        String otpCode = mockEmailService.getLastSentOtp(TEST_EMAIL);
        assertNotNull(otpCode);
        assertEquals(6, otpCode.length());

        // === 7. Verify OTP to get reset token ===
        VerifyOtpRequest verifyRequest = new VerifyOtpRequest();
        verifyRequest.setEmail(TEST_EMAIL);
        verifyRequest.setOtp(otpCode);

        MvcResult verifyResult = mockMvc.perform(post("/api/v1/auth/verify-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(verifyRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn();

        String verifyResp = verifyResult.getResponse().getContentAsString();
        String resetToken = objectMapper.readTree(verifyResp).path("data").path("resetToken").asText();
        assertNotNull(resetToken);

        // === 8. Reset password using reset token ===
        ResetPasswordRequest resetRequest = new ResetPasswordRequest();
        resetRequest.setResetToken(resetToken);
        resetRequest.setNewPassword(NEW_PASSWORD);
        resetRequest.setConfirmPassword(NEW_PASSWORD);

        mockMvc.perform(post("/api/v1/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(resetRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // === 9. Verify previous session was revoked during password reset ===
        Optional<UserSession> revokedSessionOpt = userSessionRepository.findById(activeSession.getSessionId());
        assertTrue(revokedSessionOpt.isPresent());
        assertTrue(revokedSessionOpt.get().isRevoked());
        assertEquals("REVOKED", revokedSessionOpt.get().getStatus());
        assertNotNull(revokedSessionOpt.get().getRevokedAt());

        // === 10. Attempt endpoint access with old token (should be rejected/fail-closed) ===
        mockMvc.perform(get("/api/v1/auth/me")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("AUTH_014"));

        // === 11. Login with new credentials ===
        loginRequest.setPassword(NEW_PASSWORD);

        MvcResult newLoginResult = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn();

        String newLoginResp = newLoginResult.getResponse().getContentAsString();
        String newAccessToken = objectMapper.readTree(newLoginResp).path("data").path("tokens").path("accessToken").asText();
        String newRefreshToken = objectMapper.readTree(newLoginResp).path("data").path("tokens").path("refreshToken").asText();

        assertNotNull(newAccessToken);
        assertNotEquals(accessToken, newAccessToken);

        // === 12. Terminate session via Logout ===
        LogoutRequest logoutRequest = new LogoutRequest();
        logoutRequest.setRefreshToken(newRefreshToken);

        mockMvc.perform(post("/api/v1/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(logoutRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // Verify final session revocation state
        Optional<UserSession> loggedOutSession = userSessionRepository.findByRefreshToken(newRefreshToken);
        assertFalse(loggedOutSession.isPresent());
    }
}
