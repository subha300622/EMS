package com.example.ems.auth.controller;

import com.example.ems.auth.dto.LoginRequest;
import com.example.ems.auth.dto.RegisterRequest;
import com.example.ems.auth.entity.Role;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.InvitationRepository;
import com.example.ems.auth.repository.RoleRepository;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.OtpService;
import com.example.ems.auth.service.RoleService;
import com.example.ems.auth.service.SessionService;
import com.example.ems.auth.service.UserService;
import com.example.ems.common.service.EmailService;
import com.example.ems.config.GlobalExceptionHandler;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.security.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock private OtpService otpService;
    @Mock private JwtService jwtService;
    @Mock private SessionService sessionService;
    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private EmployeeRepository employeeRepository;
    @Mock private InvitationRepository invitationRepository;
    @Mock private EmailService emailService;
    @Mock private RoleService roleService;
    @Mock private BCryptPasswordEncoder passwordEncoder;
    @Mock private UserService userService;

    @InjectMocks
    private AuthController authController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    public void testLoginSuccess() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password");

        User user = new User();
        user.setId(1L);
        user.setUserId("EMP001");
        user.setFullName("Test User");
        user.setWorkEmail("test@example.com");
        user.setPassword("hashed_password");
        Role role = new Role();
        role.setName("EMPLOYEE");
        user.setRole(role);

        when(userRepository.findByWorkEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "hashed_password")).thenReturn(true);

        SessionService.SessionMetadata sessionMetadata = new SessionService.SessionMetadata(
                "session-id", "EMP001", "test@example.com", "User-Agent", "127.0.0.1", "refresh-token"
        );
        when(sessionService.createSession(any(), any(), any(), any())).thenReturn(sessionMetadata);
        when(jwtService.generateAccessToken(any(), any(), any(), any())).thenReturn("access-token");
        when(roleService.getEffectivePermissions(user)).thenReturn(Collections.emptySet());

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Login successful"));
    }

    @Test
    public void testLoginFailure() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password");

        when(userRepository.findByWorkEmail("test@example.com")).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("AUTH_001"));
    }

    @Test
    public void testRegisterSuccess() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setFullName("Test User");
        registerRequest.setWorkEmail("test@example.com");
        registerRequest.setMobileNumber("1234567890");
        registerRequest.setDepartment("IT");
        registerRequest.setRequestedRole("EMPLOYEE");
        registerRequest.setPassword("password");
        registerRequest.setConfirmPassword("password");

        User user = new User();
        user.setWorkEmail("test@example.com");

        when(userService.register(any())).thenReturn("Registration Successful! Your User ID: EMP001 | Role ID: 2");
        when(userRepository.findByWorkEmail("test@example.com")).thenReturn(Optional.of(user));

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testRegisterPasswordMismatch() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setFullName("Test User");
        registerRequest.setWorkEmail("test@example.com");
        registerRequest.setMobileNumber("1234567890");
        registerRequest.setDepartment("IT");
        registerRequest.setRequestedRole("EMPLOYEE");
        registerRequest.setPassword("password");
        registerRequest.setConfirmPassword("mismatch");

        when(userService.register(any())).thenReturn("Passwords do not match");

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("AUTH_016"));
    }

    @Test
    public void testMalformedJsonPayload() throws Exception {
        String malformedJson = "{ \"email\": \"test@example.com\", \"password\": \"password\" "; // missing closing bracket

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(malformedJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("VAL_003"));
    }

    @Test
    public void testRefreshSuccess() throws Exception {
        com.example.ems.auth.dto.RefreshTokenRequest refreshRequest = new com.example.ems.auth.dto.RefreshTokenRequest();
        refreshRequest.setRefreshToken("valid-refresh-token");

        SessionService.SessionMetadata sessionMetadata = new SessionService.SessionMetadata(
                "session-id", "EMP001", "test@example.com", "User-Agent", "127.0.0.1", "new-refresh-token"
        );
        when(sessionService.rotateRefreshToken("valid-refresh-token")).thenReturn(sessionMetadata);

        User user = new User();
        user.setId(1L);
        user.setUserId("EMP001");
        user.setWorkEmail("test@example.com");
        Role role = new Role();
        role.setName("EMPLOYEE");
        user.setRole(role);

        when(userRepository.findByWorkEmail("test@example.com")).thenReturn(Optional.of(user));
        when(jwtService.generateAccessToken(any(), any(), any(), any())).thenReturn("new-access-token");
        when(jwtService.generateAccessToken(any(), any(), any(), any(), org.mockito.ArgumentMatchers.anyInt(), org.mockito.ArgumentMatchers.anyLong())).thenReturn("new-access-token");

        mockMvc.perform(post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Token refreshed successfully"))
                .andExpect(jsonPath("$.data.accessToken").value("new-access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("new-refresh-token"))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.accessTokenExpiresIn").value(900))
                .andExpect(jsonPath("$.data.refreshTokenExpiresIn").value(604800));
    }

    @Test
    public void testRefreshInvalidToken() throws Exception {
        com.example.ems.auth.dto.RefreshTokenRequest refreshRequest = new com.example.ems.auth.dto.RefreshTokenRequest();
        refreshRequest.setRefreshToken("invalid-refresh-token");

        when(sessionService.rotateRefreshToken("invalid-refresh-token")).thenReturn(null);

        mockMvc.perform(post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("AUTH_012"));
    }
}
