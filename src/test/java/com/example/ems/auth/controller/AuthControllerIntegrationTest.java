package com.example.ems.auth.controller;

import com.example.ems.auth.dto.LoginRequest;
import com.example.ems.auth.dto.RefreshTokenRequest;
import com.example.ems.auth.dto.LogoutRequest;
import com.example.ems.auth.entity.Role;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.entity.UserSession;
import com.example.ems.auth.repository.RoleRepository;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.repository.UserSessionRepository;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class AuthControllerIntegrationTest {

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

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String TEST_EMAIL = "testintegration@company.com";
    private static final String TEST_PASSWORD = "testpassword123";
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

        // Clean up any leftovers
        cleanup();

        // Create test user with role EMPLOYEE
        Role employeeRole = roleRepository.findByName("EMPLOYEE")
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setName("EMPLOYEE");
                    r.setDescription("Employee role");
                    return roleRepository.save(r);
                });

        testUser = new User();
        testUser.setFullName("Test Integration User");
        testUser.setWorkEmail(TEST_EMAIL);
        testUser.setMobileNumber("1112223333");
        testUser.setDepartment("Engineering");
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
        SecurityContextHolder.clearContext();
    }

    private void cleanup() {
        Optional<User> existing = userRepository.findByWorkEmail(TEST_EMAIL);
        if (existing.isPresent()) {
            User u = existing.get();
            // Delete sessions first
            userSessionRepository.deleteAll(userSessionRepository.findByUserIdAndIsRevokedFalse(u.getUserId()));
            // Clear other potential sessions by userId in DB
            userSessionRepository.findAll().stream()
                    .filter(s -> u.getUserId().equals(s.getUserId()))
                    .forEach(s -> userSessionRepository.delete(s));
            userRepository.delete(u);
        }
    }

    @Test
    public void testFullAuthFlowAndSessionPersistence() throws Exception {
        // === STEP 1: Login ===
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(TEST_EMAIL);
        loginRequest.setPassword(TEST_PASSWORD);

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.data.tokens.accessToken").exists())
                .andExpect(jsonPath("$.data.tokens.refreshToken").exists())
                .andReturn();

        String responseStr = loginResult.getResponse().getContentAsString();
        String accessToken = objectMapper.readTree(responseStr).path("data").path("tokens").path("accessToken").asText();
        String refreshToken = objectMapper.readTree(responseStr).path("data").path("tokens").path("refreshToken").asText();

        assertNotNull(accessToken);
        assertNotNull(refreshToken);

        // Verify session was persisted in the DB
        Optional<UserSession> dbSessionOpt = userSessionRepository.findByRefreshToken(refreshToken);
        assertTrue(dbSessionOpt.isPresent(), "Session should be saved in DB");
        UserSession dbSession = dbSessionOpt.get();
        assertEquals(testUser.getUserId(), dbSession.getUserId());
        assertFalse(dbSession.isRevoked());
        assertEquals("ACTIVE", dbSession.getStatus());
        assertEquals(1, dbSession.getSessionVersion());

        // === STEP 2: Access profile (/me) with access token ===
        mockMvc.perform(get("/api/v1/auth/me")
                .header("Authorization", "Bearer " + accessToken))
                .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value(TEST_EMAIL))
                .andExpect(jsonPath("$.data.employeeId").value(testUser.getUserId()));

        // === STEP 3: Refresh Token ===
        RefreshTokenRequest refreshRequest = new RefreshTokenRequest();
        refreshRequest.setRefreshToken(refreshToken);

        MvcResult refreshResult = mockMvc.perform(post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Token refreshed successfully"))
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.refreshToken").exists())
                .andReturn();

        String refreshResponseStr = refreshResult.getResponse().getContentAsString();
        String newAccessToken = objectMapper.readTree(refreshResponseStr).path("data").path("accessToken").asText();
        String newRefreshToken = objectMapper.readTree(refreshResponseStr).path("data").path("refreshToken").asText();

        assertNotNull(newAccessToken);
        assertNotNull(newRefreshToken);
        assertNotEquals(accessToken, newAccessToken);
        assertNotEquals(refreshToken, newRefreshToken);

        // Verify session was updated/rotated in DB
        Optional<UserSession> rotatedSessionOpt = userSessionRepository.findByRefreshToken(newRefreshToken);
        assertTrue(rotatedSessionOpt.isPresent(), "Rotated session should exist in DB");
        UserSession rotatedSession = rotatedSessionOpt.get();
        assertEquals(dbSession.getSessionId(), rotatedSession.getSessionId(), "Session ID should remain constant");
        assertEquals(2, rotatedSession.getSessionVersion(), "Session version should be incremented");
        assertFalse(rotatedSession.isRevoked());

        // Verify old refresh token is no longer active in DB
        Optional<UserSession> oldSessionOpt = userSessionRepository.findByRefreshToken(refreshToken);
        assertFalse(oldSessionOpt.isPresent(), "Old refresh token should not return any active session");

        // === STEP 4: Access profile (/me) with new access token ===
        mockMvc.perform(get("/api/v1/auth/me")
                .header("Authorization", "Bearer " + newAccessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // === STEP 5: Logout using new refresh token ===
        LogoutRequest logoutRequest = new LogoutRequest();
        logoutRequest.setRefreshToken(newRefreshToken);

        mockMvc.perform(post("/api/v1/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(logoutRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Logged out successfully"));

        // Verify session is revoked in DB
        Optional<UserSession> finalSessionOpt = userSessionRepository.findById(dbSession.getSessionId());
        assertTrue(finalSessionOpt.isPresent());
        UserSession finalSession = finalSessionOpt.get();
        assertTrue(finalSession.isRevoked(), "Session should be marked revoked");
        assertEquals("REVOKED", finalSession.getStatus());

        // === STEP 6: Try access profile after logout (should fail validation because session is revoked) ===
        mockMvc.perform(get("/api/v1/auth/me")
                .header("Authorization", "Bearer " + newAccessToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("AUTH_014"));
    }
}
